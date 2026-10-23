package com.github.treemanking.budgiesets.managers.armorsets;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.managers.armorsets.utilities.ArmorSetUtilities;
import com.github.treemanking.budgiesets.managers.configuration.EventManager;
import com.github.treemanking.budgiesets.utilities.effects.AttributeService;
import com.github.treemanking.budgiesets.utilities.effects.PotionEffectService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ArmorSetListener class handles events related to player armor set equipping and unequipping.
 * It also manages the registration of events for specific armor sets.
 */
public class ArmorSetListener implements Listener, ArmorSetUtilities {

    private final String armorSetName;
    private final HashMap<UUID, EquipStatus> playerEquipStatusHashMap = new HashMap<>();
    private final BudgieSets plugin;
    /** Tracks players who already have a deferred equip-status check scheduled. */
    private final Set<UUID> pendingEquipChecks = ConcurrentHashMap.newKeySet();

    /**
     * Constructs an ArmorSetListener for a specific armor set.
     *
     * @param armorSetName the name of the armor set
     * @param armorSetConfig the configuration for the armor set
     * @param plugin the BudgieSets plugin instance
     */
    public ArmorSetListener(String armorSetName, FileConfiguration armorSetConfig, BudgieSets plugin) {
        this.armorSetName = armorSetName;
        this.plugin = plugin;

        // Registers a new EventManager which registers all events for the specific armor set
        new EventManager().registerArmorEvents(armorSetName, armorSetConfig, plugin, this.playerEquipStatusHashMap);
    }

    /**
     * Handles the event of a player equipping or unequipping armor.
     * <p>
     * Paper fires the PlayerArmorChangeEvent multiple times onPlayerJoin,
     * so we schedule a single check to run after a short delay to ensure
     * we evaluate the player's final equip status.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEquip(PlayerArmorChangeEvent event) {
        scheduleEquipCheck(event.getPlayer());
    }

    /**
     * Handles the death of a player wearing the set.
     * <p>
     * Death clears the player's potion effects, but with keepInventory the armor never leaves
     * the player, so no PlayerArmorChangeEvent fires. Silently drop the set here so the check
     * after respawn activates it again and re-applies its effects.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (playerEquipStatusHashMap.get(player.getUniqueId()) != EquipStatus.EQUIPPED) return;

        playerEquipStatusHashMap.put(player.getUniqueId(), EquipStatus.NOT_EQUIPPED);
        AttributeService.removeAllAttributes(player);
        PotionEffectService.removePotionEffects(player);
    }

    /**
     * Re-evaluates the set after a respawn, re-activating it if the player kept their armor.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        scheduleEquipCheck(event.getPlayer());
    }

    /** Schedules a single deferred equip-status check for the player, unless one is already pending. */
    private void scheduleEquipCheck(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if check is already scheduled
        if (!pendingEquipChecks.add(uuid)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingEquipChecks.remove(uuid);
            if (!player.isOnline()) return;
            evaluateEquipStatus(player);
        }, 10L);
    }

    /**
     * Evaluates a player's current equip status against the armor they are actually
     * wearing right now, and activates/deactivates the set as needed.
     *
     * @param player the player to evaluate
     */
    private void evaluateEquipStatus(Player player) {
        // TODO: add messages to a lang file
        EquipStatus currentStatus = playerEquipStatusHashMap.getOrDefault(player.getUniqueId(), EquipStatus.NULL);
        boolean fullSet = isWearingFullSet(player, armorSetName);

        //log("[" + armorSetName + "] Evaluating equip status for player " + player.getName() + ": currentStatus=" + currentStatus + ", fullSet=" + fullSet);

        // Player is already wearing the full set
        if (fullSet && currentStatus.equals(EquipStatus.EQUIPPED)) return;

        // Player is not wearing the full set, but was previously wearing it
        if (!fullSet && currentStatus.equals(EquipStatus.EQUIPPED)) {
            deactivateSet(player);
            return;
        }

        // Player is now wearing the full set, but was not previously wearing it
        if (fullSet) {
            activateSet(player);
        }
    }

    /** Activates the armor set for the player, updating their equip status and sending a message. */
    private void activateSet(Player player) {
        playerEquipStatusHashMap.put(player.getUniqueId(), EquipStatus.EQUIPPED);
        ChatUtil.sendMsg(player, ChatColor.GREEN + "You are now wearing the " + armorSetName + " set.", false);
        Bukkit.getPluginManager().callEvent(new ArmorSetStatusChangeEvent(player, armorSetName, EquipStatus.EQUIPPED));
    }

    /** Deactivates the armor set for the player, updating their equip status, removing bonuses, and sending a message. */
    private void deactivateSet(Player player) {
        playerEquipStatusHashMap.put(player.getUniqueId(), EquipStatus.NOT_EQUIPPED);
        ChatUtil.sendMsg(player, ChatColor.RED + "You are now not wearing the " + armorSetName + " set and will lose all bonuses.", false);
        AttributeService.removeAllAttributes(player);
        PotionEffectService.removePotionEffects(player);
        Bukkit.getPluginManager().callEvent(new ArmorSetStatusChangeEvent(player, armorSetName, EquipStatus.NOT_EQUIPPED));
    }

    /**
     * Handles the event of a player leaving the server.
     *
     * @param event the PlayerQuitEvent representing the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // Remove the player from the map when the player leaves the server
        playerEquipStatusHashMap.remove(event.getPlayer().getUniqueId());
        pendingEquipChecks.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Returns the name of the armor set this listener was registered for.
     *
     * @return the armor set name
     */
    public String getArmorSetName() {
        return armorSetName;
    }

    /**
     * Handles the hashcode of the event
     *
     * @return the hashcode of the new armor set
     */
    @Override
    public int hashCode() {
        return armorSetName.hashCode();
    }
}
