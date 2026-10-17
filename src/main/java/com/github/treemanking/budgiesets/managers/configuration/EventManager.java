package com.github.treemanking.budgiesets.managers.configuration;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import com.github.treemanking.budgiesets.events.EventProcessorFactory;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.budgienet.chatutil.ChatUtil.warn;

/**
 * The EventManager class is responsible for registering events defined in the configuration file.
 */
public class EventManager {

    /**
     * Registers armor-related events specified in the configuration file.
     *
     * @param armorSetName the name of the armor set
     * @param fileConfiguration the FileConfiguration instance representing the configuration file
     * @param plugin the BudgieSets plugin instance
     * @param playerEquipStatusHashMap a map storing players' armor set equip status
     */
    public void registerArmorEvents(String armorSetName, @NotNull FileConfiguration fileConfiguration, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        @NotNull List<Map<?, ?>> eventsList = fileConfiguration.getMapList("Events");
        for (Map<?, ?> event : eventsList) {
            processEvent(armorSetName, event, plugin, playerEquipStatusHashMap);
        }
    }

    /**
     * Processes an event defined in the configuration file.
     *
     * @param armorSetName the name of the armor set
     * @param eventMap the map representing the event
     * @param plugin the BudgieSets plugin instance
     * @param playerEquipStatusHashMap a map storing players' armor set equip status
     */
    private void processEvent(String armorSetName, Map<?, ?> eventMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        if (eventMap == null) {
            return;
        }

        Set<? extends Map.Entry<?, ?>> entrySet = eventMap.entrySet();
        if (entrySet.size() != 1) {
            warn("Invalid event structure found: " + eventMap);
            return;
        }

        Map.Entry<?, ?> entry = entrySet.iterator().next();
        String eventType = (String) entry.getKey();
        EventProcessor processor = EventProcessorFactory.createProcessor(eventType);

        if (processor == null) {
            warn("Invalid event type: " + eventType);
            return;
        }

        Object value = entry.getValue();
        if (!(value instanceof Map)) {
            warn("Invalid event structure found: " + eventMap);
            return;
        }

        processor.process(armorSetName, (Map<?, ?>) value, plugin, playerEquipStatusHashMap);
    }
}
