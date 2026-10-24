package com.github.treemanking.budgiesets.events.processors;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.events.EventProcessor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TYPE_KEY;

public class DamagedProcessor implements EventProcessor {

    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new DamagedProcessor.DamageListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class DamageListener implements Listener {

        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();
        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String eventType;
        private final String armorSetName;

        public DamageListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;

            if (event.containsKey(TYPE_KEY) && event.get(TYPE_KEY) instanceof String) {
                this.eventType = (String) event.get(TYPE_KEY);
            } else {
                this.eventType = "Null";
            }
        }

        @EventHandler
        private void onPlayerDamage(EntityDamageByEntityEvent event) {

            if (!(event.getEntity() instanceof Player)) return;

            Player player = null;

            Entity attackingEnemy = event.getDamager();
            Entity damagedEntity = event.getEntity();
            EntityDamageEvent.DamageCause damageCause = event.getCause();

            // Every damage type below is about the damaged player, so a mob being hit never applies.
            if (!(damagedEntity instanceof Player)) return;

            // TODO: Add more types and check within list instead of having to make multiple events
            switch (eventType) {
                case "DAMAGE_PLAYER":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                            && damagedEntity.getType().equals(EntityType.PLAYER)) {
                        player = (Player) damagedEntity;
                    }
                    break;
                case "DAMAGE_PLAYER_PROJECTILE":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        if (attackingEnemy.getType().equals(EntityType.ARROW)) {
                            Arrow arrow = (Arrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player)) return;
                            player = (Player) damagedEntity;
                        } else if (attackingEnemy.getType().equals(EntityType.SPECTRAL_ARROW)) {
                            SpectralArrow arrow = (SpectralArrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player)) return;
                            player = (Player) damagedEntity;
                        }
                    }
                    break;
                case "DAMAGE_MOB":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                            && attackingEnemy instanceof Mob) {
                        player = (Player) damagedEntity;
                    }
                    break;
                case "DAMAGE_MOB_PROJECTILE":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        if (attackingEnemy.getType().equals(EntityType.ARROW)) {
                            Arrow arrow = (Arrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Mob)) return;
                            player = (Player) damagedEntity;
                        } else if (attackingEnemy.getType().equals(EntityType.SPECTRAL_ARROW)) {
                            SpectralArrow arrow = (SpectralArrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Mob)) return;
                            player = (Player) damagedEntity;
                        }
                    }
                    break;
                default:
                    if (!(damagedEntity instanceof Player) && !(attackingEnemy instanceof Mob || attackingEnemy instanceof AbstractArrow)) return;
                    player = (Player) damagedEntity;
            }
            if (player == null) return;
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
