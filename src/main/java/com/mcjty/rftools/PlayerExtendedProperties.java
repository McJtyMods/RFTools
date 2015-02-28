package com.mcjty.rftools;

import com.mcjty.rftools.blocks.teleporter.GlobalCoordinate;
import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.rftools.blocks.teleporter.TeleportationTools;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketSendBuffsToClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.*;

public class PlayerExtendedProperties implements IExtendedEntityProperties {
    public static final String ID = "rftoolsTimedTeleport";

    public static final int BUFF_MAXTICKS = 180;
    private int buffTimeout;

    private int target;
    private int teleportTimeout;
    private Entity entity = null;

    private final Map<PlayerBuff,Integer> buffs = new HashMap<PlayerBuff, Integer>();

    public PlayerExtendedProperties() {
        target = -1;
        teleportTimeout = -1;
        buffTimeout = 0;
    }

    public boolean isTeleporting() {
        return target != -1 && teleportTimeout >= 0;
    }

    private void syncBuffs() {
        PacketHandler.INSTANCE.sendTo(new PacketSendBuffsToClient(buffs), (EntityPlayerMP) entity);
    }

    public static PlayerExtendedProperties getProperties(EntityPlayer player) {
        IExtendedEntityProperties properties = player.getExtendedProperties(ID);
        return (PlayerExtendedProperties) properties;
    }

    public static void addBuff(EntityPlayer player, PlayerBuff buff, int ticks) {
        IExtendedEntityProperties properties = player.getExtendedProperties(ID);
        PlayerExtendedProperties playerExtendedProperties = (PlayerExtendedProperties) properties;
        playerExtendedProperties.addBuff(buff, ticks);
    }

    public void addBuff(PlayerBuff buff, int ticks) {
        //. We add a bit to the ticks to make sure we can live long enough.
        buffs.put(buff, ticks + BUFF_MAXTICKS - buffTimeout + 1);
        syncBuffs();
    }

    public Map<PlayerBuff, Integer> getBuffs() {
        return buffs;
    }

    public boolean hasBuff(PlayerBuff buff) {
        return buffs.containsKey(buff);
    }

    public void startTeleport(int target, int ticks) {
        this.target = target;
        this.teleportTimeout = ticks;
    }

    public void tick() {
        tickTeleport();
        tickBuffs();
    }

    private void tickBuffs() {
        buffTimeout--;
        if (buffTimeout <= 0) {
            buffTimeout = BUFF_MAXTICKS;

            Map<PlayerBuff,Integer> copyBuffs = new HashMap<PlayerBuff, Integer>(buffs);
            buffs.clear();

            for (Map.Entry<PlayerBuff, Integer> entry : copyBuffs.entrySet()) {
                int timeout = entry.getValue();
                timeout -= BUFF_MAXTICKS;
                if (timeout > 0) {
                    buffs.put(entry.getKey(), timeout);
                } else {
                    System.out.println("Removing buff: " + entry.getKey());
                    syncBuffs();
                }
            }
        }
    }

    private void tickTeleport() {
        if (teleportTimeout < 0) {
            return;
        }
        teleportTimeout--;
        if (teleportTimeout <= 0) {

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

            teleportTimeout = -1;
            target = -1;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("target", target);
        compound.setInteger("ticks", teleportTimeout);
        compound.setInteger("buffTicks", buffTimeout);
        int[] buffArray = new int[buffs.size()];
        int[] timeoutArray = new int[buffs.size()];
        int idx = 0;
        for (Map.Entry<PlayerBuff, Integer> entry : buffs.entrySet()) {
            PlayerBuff buff = entry.getKey();
            buffArray[idx] = buff.ordinal();
            timeoutArray[idx] = entry.getValue();
            idx++;
        }
        compound.setIntArray("buffs", buffArray);
        compound.setIntArray("buffTimeouts", timeoutArray);
    }

    @Override
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
        buffTimeout = compound.getInteger("buffTicks");
        int[] buffArray = compound.getIntArray("buffs");
        int[] timeoutArray = compound.getIntArray("buffTimeouts");
        buffs.clear();
        for (int idx : buffArray) {
            buffs.put(PlayerBuff.values()[idx], timeoutArray[idx]);
        }
    }

    @Override
    public void init(Entity entity, World world) {
        this.entity = entity;
    }
}
