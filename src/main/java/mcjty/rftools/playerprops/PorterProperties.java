package mcjty.rftools.playerprops;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class PorterProperties {
    private int target;
    private int teleportTimeout;

    public PorterProperties() {
        target = -1;
        teleportTimeout = -1;
    }

    public boolean isTeleporting() {
        return target != -1 && teleportTimeout >= 0;
    }

    public void startTeleport(int target, int ticks) {
        this.target = target;
        this.teleportTimeout = ticks;
    }

    public void tickTeleport(EntityPlayer player) {
        if (teleportTimeout < 0) {
            return;
        }
        teleportTimeout--;
        if (teleportTimeout <= 0) {

            TeleportDestinations destinations = TeleportDestinations.getDestinations(player.worldObj);
            GlobalCoordinate coordinate = destinations.getCoordinateForId(target);
            if (coordinate == null) {
                Logging.message(player, TextFormatting.RED + "Something went wrong! The target has disappeared!");
                TeleportationTools.applyEffectForSeverity(player, 3, false);
                return;
            }
            TeleportDestination destination = destinations.getDestination(coordinate);
            TeleportationTools.performTeleport(player, destination, 0, 10, false);

            teleportTimeout = -1;
            target = -1;
        }
    }

    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("target", target);
        compound.setInteger("ticks", teleportTimeout);
    }

    public void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey("target")) {
            target = compound.getInteger("target");
        } else {
            target = -1;
        }
        if (compound.hasKey("ticks")) {
            teleportTimeout = compound.getInteger("ticks");
        } else {
            teleportTimeout = -1;
        }
    }
}