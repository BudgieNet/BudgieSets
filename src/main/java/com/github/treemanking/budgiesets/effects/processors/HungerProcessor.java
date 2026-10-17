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
 * A class to process hunger effects for armor set effects.
 */
public class HungerProcessor implements PlayerEffectProcessor {

    /**
     * Processes hunger effects based on the provided configuration.
     *
     * @param hungers     A list of hunger configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> hungers, Player player, EquipStatus equipStatus, Event event) {
        for (Object hunger : hungers) {
            if (hunger instanceof Map<?, ?> hungerMap) {
                if (validateHungerConfig(hungerMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Integer amount = getConfigValue(hungerMap, AMOUNT_KEY, Integer.class, 1);

                    if (amount != null && amount != 0) {
                        setHunger(player, amount);
                    }

                } else {
                    // Log an error about the invalid configuration
                    warn("Invalid hunger configuration: " + hungerMap);
                }

            }
        }
    }

    /**
     * Sets the player's hunger level based on the provided amount.
     *
     * @param player The player to set the hunger level for.
     * @param amount The amount to adjust the hunger level by.
     */
    private void setHunger(@NotNull Player player, int amount) {
        int currentFoodLevel = player.getFoodLevel();
        int maxFoodLevel = 20;

        player.setFoodLevel(Math.min(currentFoodLevel + amount, maxFoodLevel));
    }

    /**
     * Validates the hunger effect configuration.
     *
     * @param hungerMap The hunger effect configuration map.
     * @return True if the configuration is valid, false otherwise.
     */
    private boolean validateHungerConfig(Map<?, ?> hungerMap) {
        return hungerMap.containsKey(AMOUNT_KEY) && hungerMap.get(AMOUNT_KEY) instanceof Integer;
    }
}
