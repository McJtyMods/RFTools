package mcjty.rftools.playerprops;

import mcjty.rftools.PlayerBuff;
import mcjty.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class BuffProperties {
    public static final int BUFF_MAXTICKS = 180;
    private int buffTimeout;
    private final Map<PlayerBuff,Integer> buffs = new HashMap<PlayerBuff, Integer>();

    // Here we mirror the flags out of capabilities so that we can restore them.
    private boolean oldAllowFlying = false;
    private boolean allowFlying = false;

    private Entity entity = null;
    private boolean globalSyncNeeded = true;

    public BuffProperties() {
        buffTimeout = 0;
        globalSyncNeeded = true;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        globalSyncNeeded = true;
    }

    private void syncBuffs() {
        PacketHandler.INSTANCE.sendTo(new PacketSendBuffsToClient(buffs), (EntityPlayerMP) entity);
    }

    public void tickBuffs() {
        buffTimeout--;
        if (buffTimeout <= 0) {
            buffTimeout = BuffProperties.BUFF_MAXTICKS;

            Map<PlayerBuff,Integer> copyBuffs = new HashMap<PlayerBuff, Integer>(buffs);
            buffs.clear();

            boolean syncNeeded = false;
            for (Map.Entry<PlayerBuff, Integer> entry : copyBuffs.entrySet()) {
                int timeout = entry.getValue();
                timeout -= BuffProperties.BUFF_MAXTICKS;
                if (timeout > 0) {
                    buffs.put(entry.getKey(), timeout);
                } else {
                    syncNeeded = true;
                }
            }
            if (syncNeeded) {
                syncBuffs();
                performBuffs();
                globalSyncNeeded = false;
            }
        }

        if (globalSyncNeeded) {
            globalSyncNeeded = false;
            syncBuffs();
            performBuffs();
        }
    }

    private void performBuffs() {
        // Perform all buffs that we can perform here (not potion effects and also not
        // passive effects like feather falling.
        EntityPlayer player = (EntityPlayer) entity;
        boolean enableFlight = false;
        for (PlayerBuff buff : buffs.keySet()) {
            if (buff == PlayerBuff.BUFF_FLIGHT) {
                enableFlight = true;
                break;
            }
        }

        boolean oldAllow = player.capabilities.allowFlying;

        if (enableFlight) {
            if (!allowFlying) {
                // We were not already allowing flying.
                oldAllowFlying = player.capabilities.allowFlying;
                allowFlying = true;
            }
            player.capabilities.allowFlying = true;
        } else {
            if (allowFlying) {
                // We were flying before.
                player.capabilities.allowFlying = oldAllowFlying;
                if (player.capabilities.isCreativeMode) {
                    player.capabilities.allowFlying = true;
                }
                allowFlying = false;
            }
        }

        if (player.capabilities.allowFlying != oldAllow) {
            if (!player.capabilities.allowFlying) {
                player.capabilities.isFlying = false;
            }
        }
        player.sendPlayerAbilities();
    }


    public static void addBuff(EntityPlayer player, PlayerBuff buff, int ticks) {
        PlayerExtendedProperties playerExtendedProperties = PlayerExtendedProperties.getProperties(player);
        playerExtendedProperties.getBuffProperties().addBuff(buff, ticks);
    }

    public void addBuff(PlayerBuff buff, int ticks) {
        //. We add a bit to the ticks to make sure we can live long enough.
        buffs.put(buff, ticks + 5);
        syncBuffs();
        performBuffs();
    }

    public Map<PlayerBuff, Integer> getBuffs() {
        return buffs;
    }

    public boolean hasBuff(PlayerBuff buff) {
        return buffs.containsKey(buff);
    }

    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("buffTicks", buffTimeout);
        compound.setBoolean("allowFlying", allowFlying);
        compound.setBoolean("oldAllowFlying", oldAllowFlying);
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

    public void loadNBTData(NBTTagCompound compound) {
        buffTimeout = compound.getInteger("buffTicks");
        int[] buffArray = compound.getIntArray("buffs");
        int[] timeoutArray = compound.getIntArray("buffTimeouts");
        buffs.clear();
        for (int i = 0; i < buffArray.length; i++) {
            int buffIdx = buffArray[i];
            buffs.put(PlayerBuff.values()[buffIdx], timeoutArray[i]);
        }
        allowFlying = compound.getBoolean("allowFlying");
        oldAllowFlying = compound.getBoolean("oldAllowFlying");
        globalSyncNeeded = true;
    }


}
