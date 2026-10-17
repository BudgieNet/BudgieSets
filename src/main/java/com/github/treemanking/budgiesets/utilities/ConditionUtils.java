package com.github.treemanking.budgiesets.utilities;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.managers.HookManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.castToListOfString;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.CHANCE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.CONDITIONS_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.COOLDOWN_KEY;

/**
 * Static helpers for evaluating the {@code Conditions}, {@code Chance} and {@code Cooldown}
 * gates that guard whether an effect should fire.
 *
 * <p>Import the helpers where they are needed:</p>
 *
 * <pre>{@code
 * import static com.github.treemanking.budgiesets.utilities.ConditionUtils.checkMap;
 * }</pre>
 */
public final class ConditionUtils {

    private ConditionUtils() {
        throw new AssertionError("ConditionUtils is a utility class and must not be instantiated");
    }

    /**
     * Evaluates the conditions, chance and cooldown gates declared on an effect map.
     *
     * @param map         the map that contains the event to effect
     * @param player      the player for whom the gates are evaluated
     * @param cooldownMap a map storing the last activation time per player
     * @return true if every gate passes, false otherwise
     */
    public static boolean checkMap(Map<?, ?> map, Player player, Map<UUID, Long> cooldownMap) {
        List<String> conditions = castToListOfString(map.get(CONDITIONS_KEY));
        Double chance = (Double) map.get(CHANCE_KEY);
        Integer cooldown = (Integer) map.get(COOLDOWN_KEY);

        // Check conditions
        if (conditions != null && !conditions.isEmpty()) {
            for (String condition : conditions) {
                if (!checkCondition(condition, player)) {
                    return false; // Condition not met
                }
            }
        }

        // Check chance
        if (chance != null) {
            double randomValue = Math.random(); // Generates a value between 0.0 and 1.0
            if (randomValue >= chance) {
                return false; // Chance not met
            }
        }

        // Check cooldown
        if (cooldown != null) {
            long currentTime = System.currentTimeMillis();
            Long lastUsedTime = cooldownMap.get(player.getUniqueId());
            if (lastUsedTime != null && (currentTime - lastUsedTime) / 1000 < cooldown.longValue()) {
                return false; // Cooldown not met
            }
            cooldownMap.put(player.getUniqueId(), currentTime); // Update cooldown map with current time
        }

        return true; // All checks passed
    }

    /**
     * Checks a single condition for a player using the PlaceholderAPI.
     *
     * @param condition the condition to check
     * @param player    the player for whom the condition is checked
     * @return true if the condition is met, false otherwise
     */
    public static boolean checkCondition(String condition, Player player) {
        if (!HookManager.isPlaceholderAPIEnabled()) {
            return true;
        }

        String trimmedCondition = condition.replaceAll("\\s+", "");
        String[] parts = trimmedCondition.split("<=|>=|<|>|==");

        if (parts.length == 2) {
            String placeholder = parts[0].trim();
            String actualValue = PlaceholderAPI.setPlaceholders(player, placeholder);
            String restOfCondition = parts[1].trim();
            String operator = condition.substring(placeholder.length(), condition.length() - restOfCondition.length()).trim();

            return performComparison(actualValue, operator, restOfCondition);
        } else {
            warn("Invalid condition format: " + condition);
            return false;
        }
    }

    /**
     * Performs a comparison between an actual value and an expected value using the specified operator.
     *
     * @param actualValue   the actual value obtained from the condition
     * @param operator      the operator used for comparison (==, &lt;, &gt;, &lt;=, &gt;=)
     * @param expectedValue the expected value to compare against
     * @return true if the comparison is successful, false otherwise
     */
    private static boolean performComparison(String actualValue, String operator, String expectedValue) {
        if (isNumeric(actualValue) && isNumeric(expectedValue)) {
            double actualNumeric = Double.parseDouble(actualValue);
            double expectedNumeric = Double.parseDouble(expectedValue);

            return switch (operator) {
                case "==" -> actualNumeric == expectedNumeric;
                case "<" -> actualNumeric < expectedNumeric;
                case ">" -> actualNumeric > expectedNumeric;
                case "<=" -> actualNumeric <= expectedNumeric;
                case ">=" -> actualNumeric >= expectedNumeric;
                default -> false; // Unsupported operator
            };
        } else if (isBoolean(actualValue) && isBoolean(expectedValue)) {
            return Boolean.parseBoolean(actualValue) == Boolean.parseBoolean(expectedValue);
        } else {
            return actualValue.equals(expectedValue);
        }
    }

    /**
     * Checks if a given string is numeric.
     *
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     */
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a given string represents a boolean value.
     *
     * @param str the string to check
     * @return true if the string is "true" or "false" (case-insensitive), false otherwise
     */
    private static boolean isBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }
}

