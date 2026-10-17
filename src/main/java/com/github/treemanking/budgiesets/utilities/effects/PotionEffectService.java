package com.github.treemanking.budgiesets.utilities.effects;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.budgienet.chatutil.ChatUtil.warn;

/**
 * Tracks and applies the infinite-duration potion effects granted by armor sets.
 *
 * <p>The backing map is private: callers interact through the operations below rather than
 * mutating shared state directly.</p>
 */
public final class PotionEffectService {

    /**
     * Infinite-duration potion effects granted by this plugin, keyed by player UUID.
     * Timed effects are left to the server to expire and are deliberately not tracked.
     */
    private static final Map<UUID, List<PotionEffect>> TRACKED_EFFECTS = new HashMap<>();

    private PotionEffectService() {
        throw new AssertionError("PotionEffectService is a static service and must not be instantiated");
    }

    /**
     * Applies a potion effect to a player, tracking it when it is infinite so that it can be
     * removed again on unequip, quit or plugin shutdown.
     *
     * @param player     the player to whom the potion effect will be applied
     * @param duration   the duration in seconds, or {@link PotionEffect#INFINITE_DURATION}
     * @param effectName the name of the potion effect to be applied
     * @param amplifier  the strength of the potion effect
     * @param ambient    whether the potion effect is ambient
     * @param particles  whether the potion effect has particles
     */
    public static void applyPotionEffect(@NotNull Player player, int duration, @NotNull String effectName,
                                         int amplifier, boolean ambient, boolean particles) {
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());

        if (effectType == null) {
            warn("Invalid potion effect name: " + effectName);
            return;
        }

        if (duration != PotionEffect.INFINITE_DURATION) duration = duration * 20;
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier, ambient, particles);

        if (duration == PotionEffect.INFINITE_DURATION) {
            TRACKED_EFFECTS.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>()).add(effect);
        }

        player.addPotionEffect(effect);
    }

    /**
     * Removes every tracked potion effect from the specified player and stops tracking them.
     *
     * <p>Used specifically for Permanent Potion.</p>
     *
     * @param player the player from whom to remove all tracked potion effects
     */
    public static void removePotionEffects(@NotNull Player player) {
        List<PotionEffect> playerEffects = TRACKED_EFFECTS.remove(player.getUniqueId());
        if (playerEffects == null) return;

        for (PotionEffect effect : playerEffects) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Removes a specific potion effect from the specified player.
     *
     * @param player       the player from whom to remove the specified potion effect
     * @param potionEffect the name of the potion effect to be removed
     */
    public static void removePotionEffects(@NotNull Player player, @NotNull String potionEffect) {
        PotionEffectType effectType = PotionEffectType.getByName(potionEffect.toUpperCase());

        if (player.getActivePotionEffects().isEmpty()) return;
        if (effectType == null) return;

        player.removePotionEffect(effectType);
    }

    /**
     * Removes every tracked potion effect from every online player. Intended for plugin shutdown.
     */
    public static void removeAllTrackedEffects() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            removePotionEffects(player);
        }
        TRACKED_EFFECTS.clear();
    }
}

