package com.github.treemanking.budgiesets.events.processors;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.EFFECT_TARGET;

public class HookProcessor implements EventProcessor {
    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new HookProcessor.HookListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class HookListener implements Listener {

        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();

        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String armorSetName;

        public HookListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;
        }

        @EventHandler
        private void onFishingHook(PlayerFishEvent fishEvent) {
            Player player = fishEvent.getPlayer();
            Entity entity = null;

            if (playerEquipStatus.get(player.getUniqueId()) != EquipStatus.EQUIPPED) return;
            EquipStatus currentStatus = playerEquipStatus.get(player.getUniqueId());

            // Get Target
            if (Objects.equals(getConfigValue(effectsMap, EFFECT_TARGET, String.class, "Player").toLowerCase(), "Hook".toLowerCase())) {
                entity = fishEvent.getHook();
            } else {
                entity = fishEvent.getPlayer();
            }

            if (checkMap(effectsMap, player, cooldownMap)) {
                effectManager.processEffectsMap(effectsMap, entity, currentStatus, fishEvent);
            }
        }

        @Override
        public int hashCode() {
            return armorSetName.hashCode();
        }
    }
}
