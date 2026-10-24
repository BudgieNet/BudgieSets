package com.github.treemanking.budgiesets.events.processors;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;

public class RiptideProcessor implements EventProcessor {
    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new RiptideProcessor.RiptideListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class RiptideListener implements Listener {

        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();
        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String armorSetName;


        public RiptideListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;
        }

        @EventHandler
        private void onPlayerSprint(PlayerRiptideEvent riptideEvent) {
            Player player = riptideEvent.getPlayer();

            if (playerEquipStatus.get(player.getUniqueId()) != EquipStatus.EQUIPPED) return;
            EquipStatus currentStatus = playerEquipStatus.get(player.getUniqueId());
            if (checkMap(effectsMap, player, cooldownMap)) {
                effectManager.processEffectsMap(effectsMap, player, currentStatus, riptideEvent);
            }
        }

        @Override
        public int hashCode() {
            return armorSetName.hashCode();
        }
    }
}
