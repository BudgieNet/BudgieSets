package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import com.github.treemanking.budgiesets.utilities.effects.PotionEffectService;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TYPE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMPLIFIER_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMBIENT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.PARTICLES_KEY;

/**
 * A class to process potion effects for armor set effects.
 */
public class PermPotionProcessor implements PlayerEffectProcessor {

    /**
     * Processes potion effects based on the provided configuration.
     *
     * @param potions     A list of potion configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> potions, Player player, EquipStatus equipStatus, Event event) {
        for (Object potion : potions) {
            if (potion instanceof Map<?, ?> potionMap) {
                if (validatePotionConfig(potionMap)) {
                    if (equipStatus.equals(EquipStatus.EQUIPPED)) {
                        String type = getConfigValue(potionMap, TYPE_KEY, String.class);
                        Integer amplifier = getConfigValue(potionMap, AMPLIFIER_KEY, Integer.class, 0);
                        Boolean ambient = getConfigValue(potionMap, AMBIENT_KEY, Boolean.class, false);
                        Boolean particles = getConfigValue(potionMap, PARTICLES_KEY, Boolean.class, true);
                        // Assuming you have a method to apply PermPotion effects
                        PotionEffectService.applyPotionEffect(player, PotionEffect.INFINITE_DURATION, type, amplifier, ambient, particles);
                    } else {
                        PotionEffectService.removePotionEffects(player);
                    }
                } else {
                    // Log an error or inform the user about the invalid configuration
                    warn("Invalid potion configuration: " + potionMap);
                }
            }
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
                && potionMap.containsKey(AMPLIFIER_KEY)
                && potionMap.containsKey(AMBIENT_KEY)
                && potionMap.containsKey(PARTICLES_KEY)
                && potionMap.get(TYPE_KEY) instanceof String
                && potionMap.get(AMPLIFIER_KEY) instanceof Integer
                && potionMap.get(AMBIENT_KEY) instanceof Boolean
                && potionMap.get(PARTICLES_KEY) instanceof Boolean;
    }
}
