package com.github.treemanking.budgiesets.managers.armorsets;

import com.github.treemanking.budgiesets.utilities.EquipStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired by {@link ArmorSetListener} after a player's equip status for an armor set has changed.
 * <p>
 * The status is evaluated a few ticks after the armor change, so OnEquip effects listen to this
 * event rather than PlayerArmorChangeEvent to see the updated status.
 */
public class ArmorSetStatusChangeEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String armorSetName;
    private final EquipStatus equipStatus;

    public ArmorSetStatusChangeEvent(@NotNull Player player, String armorSetName, EquipStatus equipStatus) {
        super(player);
        this.armorSetName = armorSetName;
        this.equipStatus = equipStatus;
    }

    public String getArmorSetName() {
        return armorSetName;
    }

    public EquipStatus getEquipStatus() {
        return equipStatus;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
