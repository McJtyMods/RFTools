package com.mcjty.rftools.items.teleportprobe;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.teleporter.GlobalCoordinate;
import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TimedTeleportationProperties implements IExtendedEntityProperties {
    public static final String ID = "rftoolsTimedTeleport";

    private int target;
    private int ticks;
    private Entity entity = null;

    public TimedTeleportationProperties() {
        target = -1;
        ticks = -1;
    }

    public boolean isTeleporting() {
        return target != -1 && ticks >= 0;
    }

    public void startTeleport(int target, int ticks) {
        this.target = target;
        this.ticks = ticks;
    }

    public void tick() {
        if (ticks < 0) {
            return;
        }
        ticks--;
        if (ticks <= 0) {

            EntityPlayer player = (EntityPlayer) entity;

            TeleportDestinations destinations = TeleportDestinations.getDestinations(entity.worldObj);
            GlobalCoordinate coordinate = destinations.getCoordinateForId(target);
            if (coordinate == null) {
                RFTools.message(player, EnumChatFormatting.RED + "Something went wrong! The target has disappeared!");
                TeleportationTools.applyEffectForSeverity(player, 3);
                return;
            }
            TeleportDestination destination = destinations.getDestination(coordinate);
            TeleportationTools.performTeleport((EntityPlayer) entity, destination, 0, 10);

            ticks = -1;
            target = -1;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("target", target);
        compound.setInteger("ticks", ticks);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey("target")) {
            target = compound.getInteger("target");
        } else {
            target = -1;
        }
        if (compound.hasKey("ticks")) {
            ticks = compound.getInteger("ticks");
        } else {
            ticks = -1;
        }
    }

    @Override
    public void init(Entity entity, World world) {
        this.entity = entity;
    }
}
