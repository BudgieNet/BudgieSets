package com.github.treemanking.budgiesets.hook;

import com.budgienet.budgieEnchants.config.Parse;
import com.budgienet.budgieEnchants.config.Problems;
import com.budgienet.budgieEnchants.config.Y;
import com.budgienet.budgieEnchants.core.Budgie;
import com.budgienet.budgieEnchants.engine.*;
import com.budgienet.budgieEnchants.expr.Expr;
import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetStatusChangeEvent;
import com.github.treemanking.budgiesets.managers.armorsets.utilities.ArmorSetUtilities;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The only class that uses BudgieCore's API. A set bonus is a file in budgie/sets/ that names an armour set
 * ({@code set:}) and holds BudgieCore effects, active while this plugin reports that set equipped and all four worn
 * pieces still carry its {@code armorSet} tag, so a missed unequip (a bulk inventory swap) can't leave a bonus on.
 */
final class BudgieCoreHook {
    private BudgieCoreHook() {}

    static final String NS = "set";
    static final Set<String> KEYS = Set.of("set", "display-name", "description", "effects", "conditions", "mutators", "charge");
    private static final ArmorSetUtilities TAGS = new ArmorSetUtilities() {};

    /** BudgieCore effects that run while their armour set is worn, keyed {@code set:<file id>} as when BudgieCore read them. */
    record SetBonus(String key, String displayName, String armorSet, List<EffectNode> effects, List<EffectNode.Condition> conditions,
                    List<EffectNode.Mutator> mutators, Charge charge, List<String> description) implements Holder {
        public String storageId() { return Holder.storageIdOf(key); }
        public Map<String, Expr> placeholders() { return Map.of(); }
        public boolean hasItem() { return false; }
    }

    /** Player -> the armour sets this plugin last reported equipped. */
    private static final Map<UUID, Set<String>> EQUIPPED = new ConcurrentHashMap<>();

    static void init(BudgieSets plugin) {
        Budgie.require(1, 4);
        Path folder = plugin.getDataPath().resolve("budgie").resolve("sets");
        try {
            int n = copyOldFiles(plugin.getDataPath().resolveSibling("BudgieCore").resolve("sets"), folder);
            if (n > 0) ChatUtil.log("Copied " + n + " set bonus files from plugins/BudgieCore/sets/ to budgie/sets/. Edit them here now; the originals stay there as a backup.");
        } catch (IOException e) {
            ChatUtil.warn("Couldn't copy the set bonus files from plugins/BudgieCore/sets/: " + e);
        }
        // BudgieCore declares it (enchant files are read before plugins enable); this replaces its stand-in
        Budgie.registerCondition("set_bonus_active", "The player wears all four pieces of this BudgieSets armour set.",
                (a, c) -> c.player() instanceof Player p && TAGS.isWearingFullSet(p, a.get("set")), Arg.of("set", Arg.Kind.STATIC_STRING));
        Budgie.registerContent(plugin, new ContentType(NS, folder, false, BudgieCoreHook::parse));
        Budgie.registerProvider(plugin, new HolderProvider() {
            public String namespace() { return NS; }

            public void collect(Player p, Sink out) {
                Map<String, Holder> bonuses = Budgie.content(NS);
                Set<String> reported = EQUIPPED.get(p.getUniqueId());
                if (bonuses.isEmpty() || reported == null || reported.isEmpty()) return;
                List<String> tags = new ArrayList<>(4);
                for (ItemStack piece : p.getInventory().getArmorContents()) tags.add(armorSetOf(piece));
                for (Holder h : bonuses.values()) if (h instanceof SetBonus s && active(reported, s.armorSet(), tags)) out.add(h, 1, NS);
            }
        });
        Budgie.registerTooltipAbilities(plugin, (item, viewer) -> abilities(item));
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onStatus(ArmorSetStatusChangeEvent e) {
                Set<String> mine = EQUIPPED.computeIfAbsent(e.getPlayer().getUniqueId(), k -> ConcurrentHashMap.newKeySet());
                if (e.getEquipStatus() == EquipStatus.EQUIPPED) mine.add(e.getArmorSetName());
                else mine.remove(e.getArmorSetName());
                Budgie.markDirty(e.getPlayer());
            }

            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                EQUIPPED.remove(e.getPlayer().getUniqueId());
            }
        }, plugin);
    }

    static Holder parse(String id, Y y, Parse ctx) {
        for (var e : y.map().entrySet()) {
            if (!KEYS.contains(e.getKey())) ctx.error(e.getValue(), "unknown set key '" + e.getKey() + "'" + Problems.suggest(e.getKey(), KEYS));
        }
        String set = y.str("set", null);
        if (set == null || set.isBlank()) {
            ctx.error(y, "a set bonus needs set: (the armour set's name, e.g. IncursionStormwing)");
            return null;
        }
        Y d = y.get("description");
        List<String> description = d.isNull() ? List.of() : d.isList() ? List.copyOf(d.strings()) : List.of(String.valueOf(d.str()));
        return new SetBonus(ctx.key(), y.str("display-name", id), set, ctx.effects(y.get("effects")), ctx.conditions(y.get("conditions")),
                ctx.mutators(y.get("mutators")), y.has("charge") ? ctx.charge(y.get("charge")) : null, description);
    }

    /** On only if this plugin reported the set and every worn piece agrees (an empty slot doesn't). */
    static boolean active(Set<String> reported, String armorSet, List<String> pieceTags) {
        if (reported == null || !reported.contains(armorSet) || pieceTags.size() != 4) return false;
        for (String tag : pieceTags) if (!armorSet.equalsIgnoreCase(tag)) return false;
        return true;
    }

    /** The piece's armorSet tag, or null. */
    static String armorSetOf(ItemStack piece) {
        if (piece == null || piece.isEmpty()) return null;
        NBTItem nbt = new NBTItem(piece);
        return nbt.hasTag("armorSet") ? nbt.getString("armorSet") : null;
    }

    /** On a set's piece: each of its bonuses, by name, with its description. */
    static List<Budgie.TooltipAbility> abilities(ItemStack item) {
        if (item == null || item.isEmpty() || !item.getType().getEquipmentSlot().isArmor()) return List.of();
        Map<String, Holder> bonuses = Budgie.content(NS);
        if (bonuses.isEmpty()) return List.of();
        String tag = armorSetOf(item);
        if (tag == null) return List.of();
        List<Budgie.TooltipAbility> out = new ArrayList<>();
        for (Holder h : bonuses.values()) {
            if (h instanceof SetBonus s && s.armorSet().equalsIgnoreCase(tag))
                out.add(new Budgie.TooltipAbility(s.displayName(), s.description().stream().map(line -> "&7" + line).toList()));
        }
        return out;
    }

    /**
     * Set bonus files were BudgieCore's sets/ folder until 2026-11-02. The first start with this hook copies them here
     * (the originals stay there as a backup) and returns how many; once {@code to} exists it does nothing.
     */
    static int copyOldFiles(Path from, Path to) throws IOException {
        if (Files.exists(to) || !Files.isDirectory(from)) return 0;
        int n = 0;
        try (Stream<Path> files = Files.walk(from)) {
            for (Path f : files.filter(Files::isRegularFile).toList()) {
                Path dest = to.resolve(from.relativize(f).toString());
                Files.createDirectories(dest.getParent());
                Files.copy(f, dest);
                n++;
            }
        }
        return n;
    }
}
