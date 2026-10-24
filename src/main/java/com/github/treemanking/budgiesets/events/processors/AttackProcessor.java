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

public class AttackProcessor implements EventProcessor {

    @Override
    public void process(String armorSetName, Map<?, ?> effectsMap, BudgieSets plugin, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
        plugin.getServer().getPluginManager().registerEvents(new AttackProcessor.AttackListener(armorSetName, effectsMap, playerEquipStatusHashMap), plugin);
    }

    private class AttackListener implements Listener {

        private final Map<?, ?> effectsMap;
        private final Map<UUID, Long> cooldownMap = new HashMap<>();
        private final HashMap<UUID, EquipStatus> playerEquipStatus;
        private final String armorSetName;
        private String eventType = "default";

        public AttackListener(String armorSetName, Map<?, ?> event, HashMap<UUID, EquipStatus> playerEquipStatusHashMap) {
            this.effectsMap = event;
            this.playerEquipStatus = playerEquipStatusHashMap;
            this.armorSetName = armorSetName;

            if (event.containsKey(TYPE_KEY) && event.get(TYPE_KEY) instanceof String) {
                this.eventType = (String) event.get(TYPE_KEY);
            }
        }

        @EventHandler
        private void onPlayerAttack(EntityDamageByEntityEvent event) {
            Player player = null;
            Entity attackingEnemy = event.getDamager();
            Entity damagedEntity = event.getEntity();
            EntityDamageEvent.DamageCause damageCause = event.getCause();

            switch (eventType) {
                case "COMBAT_PLAYER":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                            && damagedEntity.getType().equals(EntityType.PLAYER)
                            && attackingEnemy instanceof Player attacker) {
                        player = attacker;
                    }
                    break;
                case "COMBAT_PLAYER_PROJECTILE":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        if (!damagedEntity.getType().equals(EntityType.PLAYER)) break;
                        if (attackingEnemy.getType().equals(EntityType.ARROW)) {
                            Arrow arrow = (Arrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player)) break;
                            player = (Player) arrow.getShooter();
                        } else if (attackingEnemy.getType().equals(EntityType.SPECTRAL_ARROW)) {
                            SpectralArrow arrow = (SpectralArrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player)) break;
                            player = (Player) arrow.getShooter();
                        }
                    }
                    break;
                case "COMBAT_MOB":
                    if (damageCause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                            && damagedEntity instanceof Mob
                            && attackingEnemy instanceof Player attacker) {
                        player = attacker;
                    }
                    break;
                case "COMBAT_MOB_PROJECTILE":
                    if (!(damagedEntity instanceof Mob)) break;
                    if (damageCause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        if (attackingEnemy.getType().equals(EntityType.ARROW)) {
                            Arrow arrow = (Arrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player shooter)) break;
                            player = shooter;
                            break;
                        } else if (attackingEnemy.getType().equals(EntityType.SPECTRAL_ARROW)) {
                            SpectralArrow arrow = (SpectralArrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player)) break;
                            if (!damagedEntity.getType().equals(EntityType.PLAYER)) break;
                            player = (Player) arrow.getShooter();
                        }
                    }
                    break;
                default:
                    if (damageCause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                        if (attackingEnemy.getType().equals(EntityType.ARROW)) {
                            Arrow arrow = (Arrow) attackingEnemy;
                            if (!(arrow.getShooter() instanceof Player shooter)) break;
                            player = shooter;
                            break;
                        }
                    } else if (damageCause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                        if (!(attackingEnemy instanceof Player)) return;
                        player = (Player) attackingEnemy;
                    }
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
