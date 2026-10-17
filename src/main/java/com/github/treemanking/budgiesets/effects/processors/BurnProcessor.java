package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.EffectProcessor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.ACTION_TYPE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TIME_KEY;

/**
 * The BurnProcessor class processes burn effects for entity based on their armor equip status.
 */
public class BurnProcessor implements EffectProcessor {

    /**
     * Processes burn effects based on the provided configuration.
     *
     * @param burns       A list of burn configurations.
     * @param entity      The entity to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> burns, Entity entity, EquipStatus equipStatus, Event event) {
        for (Object burn : burns) {
            if (burn instanceof Map<?, ?> burnMap) {
                if (validateBurnConfig(burnMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    String actionType = getConfigValue(burnMap, ACTION_TYPE_KEY, String.class);
                    Integer time = getConfigValue(burnMap, TIME_KEY, Integer.class, 5);

                    if (time != null && time >= 0) {
                        actionPotionEffect(entity, actionType, time);
                    }
                } else {
                    // Log an error about the invalid configuration
                    warn("Invalid burn configuration: " + burnMap);
                }
            }
        }
    }

    /**
     * Applies the burn effect to the entity based on the action type and duration.
     *
     * @param entity     The entity to apply the effect to.
     * @param actionType The type of action to perform ("Add" or "Remove").
     * @param time       The duration of the effect in seconds.
     */
    private void actionPotionEffect(Entity entity, String actionType, int time) {
        if ("Add".equalsIgnoreCase(actionType)) {
            entity.setFireTicks(time * 20);
        } else if ("Remove".equalsIgnoreCase(actionType)) {
            entity.setFireTicks(0);
        } else {
            warn("Invalid action type: " + actionType + ". You must use 'Add' or 'Remove'.");
        }
    }

    /**
     * Validates the burn configuration.
     *
     * @param burnMap The burn configuration map.
     * @return True if the configuration is valid, false otherwise.
     */
    private boolean validateBurnConfig(Map<?, ?> burnMap) {
        return burnMap.containsKey(ACTION_TYPE_KEY) && burnMap.get(ACTION_TYPE_KEY) instanceof String;
    }
}
