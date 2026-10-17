package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.EffectProcessor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.BOOLEAN_KEY;

/**
 * The EventCancelProcessor class processes event cancellation effects for entities based on their armor equip status.
 */
public class EventCancelProcessor implements EffectProcessor {

    /**
     * Processes event cancellation effects based on the provided configuration.
     *
     * @param eventCancels A list of event cancellation configurations.
     * @param entity       The entity to apply the effects to.
     * @param equipStatus  The equip status of the player's armor.
     * @param event        The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> eventCancels, Entity entity, EquipStatus equipStatus, Event event) {
        if (!(event instanceof Cancellable cancellableEvent)) {
            warn("Event not cancellable: " + event.getEventName());
            return;
        }

        for (Object eventCancel : eventCancels) {
            if (eventCancel instanceof Map<?, ?> eventCancelMap) {
                if (validateEventCancelConfig(eventCancelMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Boolean eventCancelStatus = getConfigValue(eventCancelMap, BOOLEAN_KEY, Boolean.class);

                    if (eventCancelStatus != null && eventCancelStatus) {
                        applyEventCancel(cancellableEvent);
                    }
                } else {
                    // Log an error about the invalid configuration
                    warn("Invalid Cancel Event configuration: " + eventCancelMap);
                }
            }
        }
    }

    /**
     * Applies the event cancellation effect.
     *
     * @param event The cancellable event.
     */
    private void applyEventCancel(@NotNull Cancellable event) {
        event.setCancelled(true);
    }

    /**
     * Validates the event cancellation configuration.
     *
     * @param eventCancelMap The event cancellation configuration map.
     * @return True if the configuration is valid, false otherwise.
     */
    private boolean validateEventCancelConfig(Map<?, ?> eventCancelMap) {
        return eventCancelMap.containsKey(BOOLEAN_KEY) && eventCancelMap.get(BOOLEAN_KEY) instanceof Boolean;
    }

}
