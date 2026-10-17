package com.github.treemanking.budgiesets.utilities;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.BudgieSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;

/**
 * Static helpers for reading values out of the raw configuration maps supplied by SnakeYAML.
 *
 * <p>Import the helpers where they are needed:</p>
 *
 * <pre>{@code
 * import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
 * }</pre>
 */
public final class ConfigUtils {

    private ConfigUtils() {
        throw new AssertionError("ConfigUtils is a utility class and must not be instantiated");
    }

    /**
     * Retrieves a configuration value from the map, with logging when the value is of the wrong type.
     *
     * @param map  the configuration map
     * @param key  the key to retrieve
     * @param type the expected type of the value
     * @param <T>  the type parameter
     * @return the value if present and of the correct type, null otherwise
     */
    public static <T> T getConfigValue(Map<?, ?> map, String key, Class<T> type) {
        return getConfigValue(map, key, type, null);
    }

    /**
     * Retrieves a configuration value from the map, falling back to a default when the key is
     * absent or the value is of the wrong type. A warning is logged only for type mismatches;
     * a missing key is treated as "use the default" and is not logged.
     *
     * @param map          the configuration map
     * @param key          the key to retrieve
     * @param type         the expected type of the value
     * @param defaultValue the value to use when the key is missing or of the wrong type
     * @param <T>          the type parameter
     * @return the value if present and of the correct type, otherwise the default value
     */
    public static <T> T getConfigValue(Map<?, ?> map, String key, Class<T> type, T defaultValue) {
        if (!map.containsKey(key)) {
            return defaultValue;
        }

        Object value = map.get(key);
        if (!type.isInstance(value)) {
            warn("Incorrect type for key: " + key
                    + ". Expected: " + type.getSimpleName()
                    + ", but got: " + (value == null ? "null" : value.getClass().getSimpleName()));
            return defaultValue;
        }

        return type.cast(value);
    }

    /**
     * Casts an object to a list of strings if every element in the list is a string.
     *
     * @param obj the object to cast
     * @return the cast list of strings, or an empty list when {@code obj} is null
     * @throws IllegalArgumentException if the object is not a list, or contains a non-string element
     */
    public static List<String> castToListOfString(Object obj) {
        if (obj == null) return new ArrayList<>();
        if (!(obj instanceof List<?> list)) {
            throw new IllegalArgumentException("The provided object is not a List");
        }

        for (Object element : list) {
            if (!(element instanceof String)) {
                throw new IllegalArgumentException("The list contains non-string elements");
            }
        }

        @SuppressWarnings("unchecked")
        List<String> stringList = (List<String>) list;
        return stringList;
    }
}

