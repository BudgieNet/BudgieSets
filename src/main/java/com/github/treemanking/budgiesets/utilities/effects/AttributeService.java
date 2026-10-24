package com.github.treemanking.budgiesets.utilities.effects;

import com.github.treemanking.budgiesets.BudgieSets;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks and applies the attribute modifiers granted by armor sets.
 *
 * <p>The backing map is private: callers interact through the operations
 * below rather than mutating shared state directly.</p>
 */
public final class AttributeService {

    /**
     * Attribute modifiers applied by this plugin, keyed by player UUID.
     */
    private static final Map<UUID, List<AttributeModifier>> TRACKED_MODIFIERS = new HashMap<>();

    private AttributeService() {
        throw new AssertionError("AttributeService is a static service and must not be instantiated");
    }

    /**
     * Applies a specified attribute modifier to a player and tracks it so it can be removed again.
     *
     * @param player    the player to whom the attribute effect will be applied
     * @param attribute the attribute to be modified
     * @param operation the operation that will modify the attribute (add, multiply, etc.)
     * @param amount    the amount to add or remove
     * @param time      how long the attribute modification will last, in seconds
     */
    public static void applyAttribute(@NotNull Player player, @NotNull Attribute attribute,
                                      AttributeModifier.Operation operation, Double amount, Integer time) {
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance == null) return;

        NamespacedKey key = new NamespacedKey("budgiesets", "modifier_" + UUID.randomUUID());
        AttributeModifier attributeModifier = new AttributeModifier(key, amount, operation, EquipmentSlotGroup.ANY);
        // Transient, so it isn't saved with the player and can't outlive the set across a relog or restart
        attributeInstance.addTransientModifier(attributeModifier);

        TRACKED_MODIFIERS.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>()).add(attributeModifier);

        if (time != null && time > 0) {
            // On the main thread, and only this modifier: the set's other bonuses stay
            Bukkit.getScheduler().runTaskLater(BudgieSets.getBudgieSets(),
                    () -> removeAttribute(player, attribute, attributeModifier), time * 20L);
        }
    }

    /**
     * Removes one tracked modifier, unless it already went with the rest of the player's set
     * (unequip, death or quit).
     */
    private static void removeAttribute(@NotNull Player player, @NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        List<AttributeModifier> modifiers = TRACKED_MODIFIERS.get(player.getUniqueId());
        if (modifiers == null || !modifiers.remove(modifier)) return;
        if (modifiers.isEmpty()) TRACKED_MODIFIERS.remove(player.getUniqueId());

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(modifier);
    }

    /**
     * Removes all tracked attribute modifiers from a player and stops tracking them.
     *
     * @param player the player from whom all attribute modifiers will be removed
     */
    public static void removeAllAttributes(@NotNull Player player) {
        List<AttributeModifier> modifiers = TRACKED_MODIFIERS.remove(player.getUniqueId());
        if (modifiers == null) return;

        for (AttributeModifier modifier : modifiers) {
            for (Attribute attribute : Registry.ATTRIBUTE) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modifier);
                }
            }
        }
    }

    /**
     * Removes every tracked attribute modifier from every online player. Intended for plugin shutdown.
     */
    public static void removeAllTrackedAttributes() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            removeAllAttributes(player);
        }
        TRACKED_MODIFIERS.clear();
    }

}

