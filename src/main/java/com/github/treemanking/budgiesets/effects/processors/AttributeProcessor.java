package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import com.google.j2objc.annotations.Property;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import com.github.treemanking.budgiesets.utilities.effects.AttributeService;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.ATTRIBUTE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.OPERATION_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.AMOUNT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TIME_KEY;

/**
 * A class to process attributes for armor set effects
 */
public class AttributeProcessor implements PlayerEffectProcessor {

    /**
     * Processes attribute effects based on the provided configuration.
     *
     * @param attributes  A list of attribute configurations.
     * @param player      The player to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> attributes, Player player, EquipStatus equipStatus, Event event) {
        for (Object attribute : attributes) {
            if (attribute instanceof Map<?, ?> attributeMap) {
                if (validateAttributeMap(attributeMap)) {
                    if (equipStatus.equals(EquipStatus.EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) {

                        Attribute attribut3 = getAttributeFromName(getConfigValue(attributeMap, ATTRIBUTE_KEY, String.class));
                        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(getConfigValue(attributeMap, OPERATION_KEY, String.class, "ADD_NUMBER"));
                        Double amount = getConfigValue(attributeMap, AMOUNT_KEY, Double.class);
                        Integer time = getConfigValue(attributeMap, TIME_KEY, Integer.class);

                        AttributeService.applyAttribute(player, attribut3, operation, amount, time);
                    } else {
                        AttributeService.removeAllAttributes(player);
                    }
                } else {
                    // Log an error or inform the user about the invalid configuration
                    warn("Invalid attribute configuration: " + attributeMap);
                }
            }
        }
    }

    private boolean validateAttributeMap(Map<?,?> map) {
        return map.containsKey(ATTRIBUTE_KEY) && isValidAttributeEnum((String) map.get(ATTRIBUTE_KEY))
                && map.containsKey(OPERATION_KEY) && isValidOperationEnum((String) map.get(OPERATION_KEY))
                && map.containsKey(AMOUNT_KEY) && map.get(AMOUNT_KEY) instanceof Double;
    }

    /**
     * Checks if the provided attribute type is a valid Attribute enum constant.
     *
     * @param type The name of the attribute type.
     * @return True if the attribute type is valid, otherwise false.
     */
    private boolean isValidAttributeEnum(String type) {
        return getAttributeFromName(type) != null;
    }

    /**
     * Checks if the provided operation type is a valid Operation enum constant.
     *
     * @param type The name of the operation type.
     * @return True if the operation type is valid, otherwise false.
     */
    private boolean isValidOperationEnum(String type) {
        try {
            AttributeModifier.Operation.valueOf(type);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Gets an Attribute by name, using Attribute.named(String) if available, otherwise falling back to Attribute.valueOf(String).
     *
     * @param name The attribute name.
     * @return The Attribute, or null if not found.
     */
    private Attribute getAttributeFromName(String name) {
        try {
            Method namedMethod = Attribute.class.getMethod("named", String.class);
            Object result = namedMethod.invoke(null, name);
            return result instanceof Attribute ? (Attribute) result : null;
        } catch (NoSuchMethodException e) {
            // Fallback for older versions
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
