package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import com.github.treemanking.budgiesets.utilities.effects.PotionEffectService;

import static com.budgienet.chatutil.ChatUtil.*;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.ACTION_TYPE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TYPE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.DURATION_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMPLIFIER_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMBIENT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.PARTICLES_KEY;

/**
 * A class to process potion effects for armor set effects.
 */
public class PotionProcessor implements PlayerEffectProcessor {

    /**
     * Processes potion effects based on the provided configuration.
     *
     * @param effect      A list of potion effect configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> effect, Player player, EquipStatus equipStatus, Event event) {
        for (Object potion : effect) {
            if (potion instanceof Map<?, ?> potionMap) {
                if (validatePotionConfig(potionMap)) {
                    if (equipStatus.equals(EquipStatus.EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) {

                        String actionType = getConfigValue(potionMap, ACTION_TYPE_KEY, String.class);
                        String type = getConfigValue(potionMap, TYPE_KEY, String.class);
                        Integer duration = getConfigValue(potionMap, DURATION_KEY, Integer.class, 5);
                        Integer amplifier = getConfigValue(potionMap, AMPLIFIER_KEY, Integer.class, 0);
                        Boolean ambient = getConfigValue(potionMap, AMBIENT_KEY, Boolean.class, false);
                        Boolean particles = getConfigValue(potionMap, PARTICLES_KEY, Boolean.class, true);

                        actionPotionEffect(actionType, player, duration, type, amplifier, ambient, particles);
                    }
                } else {
                    // Log an error or inform the user about the invalid configuration
                    warn("Invalid potion configuration:" + potionMap);
                }
            }
        }
    }

    /**
     * Applies the potion effect to the player.
     *
     * @param actionType  The type of action to perform (add or remove).
     * @param player      The player to apply the effect to.
     * @param duration    The duration of the effect.
     * @param effectName  The name of the effect.
     * @param amplifier   The amplifier of the effect.
     * @param ambient     Whether the effect is ambient.
     * @param particles   Whether the effect has particles.
     */
    private void actionPotionEffect(String actionType, @NotNull Player player, int duration, @NotNull String effectName, int amplifier, boolean ambient, boolean particles) {
        if (actionType.equalsIgnoreCase("Add")) {
            PotionEffectService.applyPotionEffect(player, duration, effectName, amplifier, ambient, particles);
        } else if (actionType.equalsIgnoreCase("Remove")) {
            PotionEffectService.removePotionEffects(player, effectName);
        } else {
            warn("You must have an action type of add or remove.");
        }
    }

    /**
     * Validates the potion configuration map.
     *
     * @param potionMap The potion configuration map to validate.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validatePotionConfig(Map<?, ?> potionMap) {
        return potionMap.containsKey(TYPE_KEY)
                && potionMap.containsKey(ACTION_TYPE_KEY)
                && potionMap.get(TYPE_KEY) instanceof String
                && potionMap.get(ACTION_TYPE_KEY) instanceof String;
    }
}
