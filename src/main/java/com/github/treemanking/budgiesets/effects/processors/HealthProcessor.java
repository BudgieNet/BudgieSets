package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMOUNT_KEY;

/**
 * A class to process health effects for armor set effects.
 */
public class HealthProcessor implements PlayerEffectProcessor {

    /**
     * Processes potion effects based on the provided configuration.
     *
     * @param healths     A list of health configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> healths, Player player, EquipStatus equipStatus, Event event) {
        for (Object health : healths) {
            if (health instanceof Map<?, ?> healthMap) {
                if (validateHealthConfig(healthMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Double amount = getConfigValue(healthMap, AMOUNT_KEY, Double.class, 1.0);

                    if (amount != 0) {
                        setHealth(player, amount);
                    }
                } else {
                    // Log an error about the invalid configuration
                    warn("Invalid health configuration: " + healthMap);
                }
            }
        }
    }

    /**
     * Sets the player's health based on the provided amount.
     *
     * @param player The player to set the health for.
     * @param amount The amount to adjust the health by.
     */
    private void setHealth(@NotNull Player player, double amount) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getMaxHealth();

        if (amount < 0) {
            // Handle case where amount is negative (health is being subtracted)
            player.setHealth(Math.max(currentHealth + amount, 0));
        } else if (amount > 0) {
            // Handle case where amount is positive (health is being added)
            double newHealth = Math.min(currentHealth + amount, maxHealth);
            player.setHealth(newHealth);
        }
    }

    /**
     * Validates the health effect configuration.
     *
     * @param healthMap The health effect configuration map.
     * @return True if the configuration is valid, false otherwise.
     */
    private boolean validateHealthConfig(Map<?, ?> healthMap) {
        return healthMap.containsKey(AMOUNT_KEY) && healthMap.get(AMOUNT_KEY) instanceof Double;
    }
}
