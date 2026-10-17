package com.github.treemanking.budgiesets.effects.processors;

import com.budgienet.chatutil.ChatUtil;
import com.github.treemanking.budgiesets.utilities.EquipStatus;
import com.github.treemanking.budgiesets.BudgieSets;
import com.github.treemanking.budgiesets.effects.PlayerEffectProcessor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Map;

import static com.budgienet.chatutil.ChatUtil.warn;
import static com.github.treemanking.budgiesets.utilities.ConfigUtils.getConfigValue;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.SOUND_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.VOLUME_KEY;
import static com.github.treemanking.budgiesets.utilities.ProcessorKeys.PITCH_KEY;

/**
 * A class to process sound effects for armor set effects.
 */
public class PlaySoundProcessor implements PlayerEffectProcessor {

    /**
     * Processes sound effects based on the provided configuration.
     *
     * @param sounds      A list of sound configurations.
     * @param player      The player to hear the sounds.
     * @param equipStatus The equip status of the player's armor.
     * @param event       The event triggering the effect.
     */
    @Override
    public void processEffect(List<?> sounds, Player player, EquipStatus equipStatus, Event event) {
        for (Object sound : sounds) {
            if (sound instanceof Map<?, ?> soundMap) {
                if (validateSoundConfig(soundMap)) {
                    if (equipStatus.equals(EquipStatus.NOT_EQUIPPED)
                            || equipStatus.equals(EquipStatus.NULL)) return;

                    Sound soundType = getSoundFromName(getConfigValue(soundMap, SOUND_KEY, String.class));
                    float volume = getConfigValue(soundMap, VOLUME_KEY, Double.class, 1.0).floatValue();
                    float pitch = getConfigValue(soundMap, PITCH_KEY, Double.class, 1.0).floatValue();

                    if (volume > 0 && pitch >= 0 && soundType != null) {
                        playSound(player, soundType, volume, pitch);
                    }
                } else {
                    warn("Invalid Sound configuration:" + soundMap);
                }
            }
        }
    }

    /**
     * Plays the specified sound to the player.
     *
     * @param player The player to hear the sound.
     * @param sound  The type of sound to play.
     * @param volume The volume of the sound.
     * @param pitch  The pitch of the sound.
     */
    private void playSound(Player player, Sound sound, float volume, float pitch) {
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Validates the sound configuration map.
     *
     * @param soundMap The sound configuration map to validate.
     * @return True if the configuration is valid, otherwise false.
     */
    private boolean validateSoundConfig(Map<?, ?> soundMap) {
        return soundMap.containsKey(SOUND_KEY)
                && soundMap.containsKey(VOLUME_KEY)
                && soundMap.containsKey(PITCH_KEY)
                && isValidSoundEnum(getConfigValue(soundMap, SOUND_KEY, String.class, ""));
    }

    /**
     * Checks if the provided sound type is a valid Sound enum constant.
     *
     * @param type The name of the sound type.
     * @return True if the sound type is valid, otherwise false.
     */
    private boolean isValidSoundEnum(String type) {
        return getSoundFromName(type) != null;
    }

    private Sound getSoundFromName(String name) {
        try {
            // For Paper 1.21+, prefer Sound.named
            try {
                return Sound.class.getMethod("named", String.class).invoke(null, name) instanceof Sound s ? s : null;
            } catch (NoSuchMethodException e) {
                // Fallback for older versions
                return Sound.valueOf(name);
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
