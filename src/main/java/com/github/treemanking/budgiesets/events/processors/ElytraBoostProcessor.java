package com.github.treemanking.budgiesets.events.processors;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;

public class ElytraBoostProcessor implements EventProcessor {
    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new ElytraBoostProcessor.ElytraBoostListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class ElytraBoostListener implements Listener {
        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();

        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String armorSetName;

        public ElytraBoostListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;
        }

        @EventHandler
        private void onFishingHook(PlayerElytraBoostEvent event) {
            Player player = event.getPlayer();

            if (playerEquipStatus.get(player.getUniqueId()) != EquipStatus.EQUIPPED) return;
            EquipStatus currentStatus = playerEquipStatus.get(player.getUniqueId());
            if (checkMap(effectsMap, player, cooldownMap)) {
                effectManager.processEffectsMap(effectsMap, player, currentStatus, event);
            }
        }

        @Override
        public int hashCode() {
            return armorSetName.hashCode();
        }
    }
}
