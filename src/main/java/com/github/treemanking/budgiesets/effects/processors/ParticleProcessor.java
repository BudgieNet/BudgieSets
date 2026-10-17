package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.destroystokyo.paper.ParticleBuilder;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.EffectProcessor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.*;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ColorUtils.convertHexToRGB;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.PARTICLE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.COUNT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.OFFSET_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.DATA_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.MATERIAL_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.INT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.FLOAT_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.ARRIVAL_TIME_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.FROM_COLOR_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.TO_COLOR_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.SIZE_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.COLOR_KEY;

/**
 * A class to process particles for armor set effects.
 */
public class ParticleProcessor implements EffectProcessor {

    /**
     * Processes particle effects based on the provided configuration.
     *
     * @param particles   A list of particle configurations.
     * @param entity      The entity to apply the effects to.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> particles, Entity entity, EquipStatus equipStatus, Event event) {
        for (Object particle : particles) {
            if (particle instanceof Map<?, ?> particleMap) {
                if (validateParticleConfig(particleMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Particle particleType = Enum.valueOf(Particle.class, getConfigValue(particleMap, PARTICLE_KEY, String.class));
                    Integer count = getConfigValue(particleMap, COUNT_KEY, Integer.class, 1);
                    Double offset = getConfigValue(particleMap, OFFSET_KEY, Double.class, 0.0);
                    Map<?, ?> particleDataMap = (Map<?, ?>) particleMap.get(DATA_KEY);

                    if (count > 0) {
                        spawnParticle(entity, particleType, count, offset, particleDataMap);
                    }
                } else {
                    // Log an error or inform the user about the invalid configuration
                    warn("Invalid particle configuration: " + particleMap);
                }
            }
        }
    }

    private void spawnParticle(Entity entity, Particle particle, int count, double offset, Map<?, ?> dataMap) {
        ParticleBuilder particleBuilder = new ParticleBuilder(particle);

        if (dataMap == null || dataMap.isEmpty()) {
            particleBuilder.location(entity.getLocation())
                    .count(count)
                    .offset(offset, offset, offset)
                    .spawn();
        } else {
            particleBuilder.location(entity.getLocation())
                    .count(count)
                    .data(convertData(particle.getDataType(), dataMap, entity))
                    .offset(offset, offset, offset)
                    .spawn();
        }
    }

    /**
     * Validates the particle configuration.
     *
     * @param particleMap The particle configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateParticleConfig(Map<?, ?> particleMap) {
        return particleMap.containsKey(PARTICLE_KEY)
                && particleMap.containsKey(COUNT_KEY)
                && particleMap.containsKey(OFFSET_KEY)
                && isValidParticleEnum((String) particleMap.get(PARTICLE_KEY))
                && particleMap.get(COUNT_KEY) instanceof Integer
                && particleMap.get(OFFSET_KEY) instanceof Double;
    }

    /**
     * Checks if the provided particle type is valid.
     *
     * @param type The particle type to check.
     * @return True if the particle type is valid, otherwise false.
     */
    private boolean isValidParticleEnum(String type) {
        try {
            Enum.valueOf(Particle.class, type);
            return true;
        } catch (IllegalArgumentException exception) {
            warn(type + "Not a valid particle.");
        }
        return false;
    }

    /**
     * Converts raw configuration data to the specified particle data type.
     *
     * @param particleDataTypeClass The class type of the particle data.
     * @param dataMap               The raw configuration data map.
     * @param entity                The entity associated with the particle.
     * @return The converted particle data.
     */
    private Object convertData(Class<?> particleDataTypeClass, Map<?, ?> dataMap, Entity entity) {
        if(dataMap == null) return null;

        if (particleDataTypeClass.equals(MaterialData.class)) {
            if (validateMaterialKey(dataMap)) {
                return Material.getMaterial(getConfigValue(dataMap, MATERIAL_KEY, String.class, "STONE").toUpperCase());
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(BlockData.class)) {
            if (validateMaterialKey(dataMap)) {
                return Material.getMaterial(getConfigValue(dataMap, MATERIAL_KEY, String.class, "STONE").toUpperCase()).createBlockData();
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(Integer.class)) {
            if (validateIntKey(dataMap)) {
                return getConfigValue(dataMap, INT_KEY, Integer.class);
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(Float.class)) {
            if (validateFloatKey(dataMap)) {
                return getConfigValue(dataMap, FLOAT_KEY, Double.class).floatValue();
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(Vibration.class)) {
            if (validateArrivalTimeKey(dataMap)) {
                return new Vibration(new Vibration.Destination.EntityDestination(entity), (int) dataMap.get(ARRIVAL_TIME_KEY));
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(Particle.DustTransition.class)) {
            int[] fromColor = convertHexToRGB(getConfigValue(dataMap, FROM_COLOR_KEY, String.class));
            int[] toColor = convertHexToRGB(getConfigValue(dataMap, TO_COLOR_KEY, String.class));
            if (validateHexKey(dataMap)) {
                return new Particle.DustTransition(
                        Color.fromRGB(fromColor[0], fromColor[1], fromColor[2]),
                        Color.fromRGB(toColor[0], toColor[1], toColor[2]),
                        getConfigValue(dataMap, SIZE_KEY, Double.class).floatValue());
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(ItemStack.class)) {
            if (validateMaterialKey(dataMap)) {
                return new ItemStack(Material.getMaterial(getConfigValue(dataMap, MATERIAL_KEY, String.class, "STONE").toUpperCase()));
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        } else if (particleDataTypeClass.equals(Particle.DustOptions.class)) {
            if (validateSizeKey(dataMap) && validateColorKey(dataMap)) {
                int[] color = convertHexToRGB(getConfigValue(dataMap, COLOR_KEY, String.class));
                return new Particle.DustOptions(Color.fromRGB(color[0], color[1], color[2]), getConfigValue(dataMap, SIZE_KEY, Double.class).floatValue());
            }
            warn("Invalid configuration. Please see the wiki on particle data.");
            return null;
        }
        return null;
    }

    /**
     * Validates the material data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateMaterialKey(Map<?, ?> map) {
        return map.containsKey(MATERIAL_KEY)
                && Material.getMaterial(map.get(MATERIAL_KEY).toString().toUpperCase()) != null;
    }

    /**
     * Validates the float data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateFloatKey(Map<?, ?> map) {
        return map.containsKey(FLOAT_KEY)
                && map.get(FLOAT_KEY) instanceof Double;
    }

    /**
     * Validates the integer data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateIntKey(Map<?, ?> map) {
        return map.containsKey(INT_KEY)
                && map.get(INT_KEY) instanceof Integer;
    }

    /**
     * Validates the arrival time data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateArrivalTimeKey(Map<?, ?> map) {
        return map.containsKey(ARRIVAL_TIME_KEY)
                && map.get(ARRIVAL_TIME_KEY) instanceof Integer;
    }

    /**
     * Validates the hex data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateHexKey(Map<?, ?> map) {
        return map.containsKey(FROM_COLOR_KEY)
                && map.containsKey(TO_COLOR_KEY)
                && map.get(FROM_COLOR_KEY) instanceof String
                && map.get(TO_COLOR_KEY) instanceof String
                && validateSizeKey(map);
    }

    /**
     * Validates the size data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateSizeKey(Map<?, ?> map) {
        return map.containsKey(SIZE_KEY);
    }

    /**
     * Validates the color data configuration.
     *
     * @param map The data configuration map.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateColorKey(Map<?, ?> map) {
        return map.containsKey(COLOR_KEY)
                && map.get(COLOR_KEY) instanceof String;
    }
}
