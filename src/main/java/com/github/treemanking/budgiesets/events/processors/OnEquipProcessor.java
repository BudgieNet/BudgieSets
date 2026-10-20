package com.github.treemanking.budgiesets.events.processors;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.managers.armorsets.ArmorSetStatusChangeEvent;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;

public class OnEquipProcessor implements EventProcessor {
    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new EffectStaticListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class EffectStaticListener implements Listener {

        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();
        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String armorSetName;

        public EffectStaticListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onStatusChange(ArmorSetStatusChangeEvent statusChangeEvent) {
            if (!statusChangeEvent.getArmorSetName().equals(armorSetName)) return;
            Player player = statusChangeEvent.getPlayer();
            if (checkMap(effectsMap, player, cooldownMap)) {
                effectManager.processEffectsMap(effectsMap, player, statusChangeEvent.getEquipStatus(), statusChangeEvent);
            }
        }

        @Override
        public int hashCode() {
            return armorSetName.hashCode();
        }
    }
}



