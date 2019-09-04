package mcjty.rftools.playerprops;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;

public class BuffProperties {
    public static final int BUFF_MAXTICKS = 180;
    private int buffTimeout;
    private final Map<PlayerBuff,Integer> buffs = new HashMap<>();

    // Here we mirror the flags out of capabilities so that we can restore them.
    private boolean oldAllowFlying = false;
    private boolean allowFlying = false;

    private boolean globalSyncNeeded = true;

    private boolean onElevator = false;

    public BuffProperties() {
        buffTimeout = 0;
        globalSyncNeeded = true;
    }

    private void syncBuffs(ServerPlayerEntity player) {
        RFToolsMessages.INSTANCE.sendTo(new PacketSendBuffsToClient(buffs), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void tickBuffs(ServerPlayerEntity player) {
        buffTimeout--;
        if (buffTimeout <= 0) {
            buffTimeout = BuffProperties.BUFF_MAXTICKS;

            Map<PlayerBuff,Integer> copyBuffs = new HashMap<>(buffs);
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
                syncBuffs(player);
                performBuffs(player);
                globalSyncNeeded = false;
            }
        }

        if (globalSyncNeeded) {
            globalSyncNeeded = false;
            syncBuffs(player);
            performBuffs(player);
        }
    }

    private void performBuffs(ServerPlayerEntity player) {
        // Perform all buffs that we can perform here (not potion effects and also not
        // passive effects like feather falling.
        boolean enableFlight = false;
        if (onElevator) {
            enableFlight = true;
            player.abilities.isFlying = true;
        } else {
            for (PlayerBuff buff : buffs.keySet()) {
                if (buff == PlayerBuff.BUFF_FLIGHT) {
                    enableFlight = true;
                    break;
                }
            }
        }

        boolean oldAllow = player.abilities.allowFlying;

        if (enableFlight) {
            if (!allowFlying) {
                // We were not already allowing flying.
                oldAllowFlying = player.abilities.allowFlying;
                allowFlying = true;
            }
            player.abilities.allowFlying = true;
        } else {
            if (allowFlying) {
                // We were flying before.
                player.abilities.allowFlying = oldAllowFlying;
                if (player.abilities.isCreativeMode) {
                    player.abilities.allowFlying = true;
                }
                allowFlying = false;
            }
        }

        if (player.abilities.allowFlying != oldAllow) {
            if (!player.abilities.allowFlying) {
                player.abilities.isFlying = false;
            }
        }
        player.sendPlayerAbilities();
    }

    public static void enableElevatorMode(PlayerEntity player) {
        PlayerExtendedProperties.getBuffProperties(player).ifPresent(h -> {
            h.onElevator = true;
            h.performBuffs((ServerPlayerEntity) player);
        });
    }

    public static void disableElevatorMode(PlayerEntity player) {
        PlayerExtendedProperties.getBuffProperties(player).ifPresent(h -> {
            h.onElevator = false;
            player.abilities.isFlying = false;
            h.performBuffs((ServerPlayerEntity) player);
        });
    }

    public static void addBuffToPlayer(PlayerEntity player, PlayerBuff buff, int ticks) {
        PlayerExtendedProperties.getBuffProperties(player).ifPresent(h -> {
            h.addBuff((ServerPlayerEntity) player, buff, ticks);
        });
    }

    public void addBuff(ServerPlayerEntity player, PlayerBuff buff, int ticks) {
        //. We add a bit to the ticks to make sure we can live long enough.
        buffs.put(buff, ticks + 5);
        syncBuffs(player);
        performBuffs(player);
    }

    public Map<PlayerBuff, Integer> getBuffs() {
        return buffs;
    }

    public boolean hasBuff(PlayerBuff buff) {
        return buffs.containsKey(buff);
    }

    public void saveNBTData(CompoundNBT compound) {
        compound.putBoolean("onElevator", onElevator);
        compound.putInt("buffTicks", buffTimeout);
        compound.putBoolean("allowFlying", allowFlying);
        compound.putBoolean("oldAllowFlying", oldAllowFlying);
        int[] buffArray = new int[buffs.size()];
        int[] timeoutArray = new int[buffs.size()];
        int idx = 0;
        for (Map.Entry<PlayerBuff, Integer> entry : buffs.entrySet()) {
            PlayerBuff buff = entry.getKey();
            buffArray[idx] = buff.ordinal();
            timeoutArray[idx] = entry.getValue();
            idx++;
        }
        compound.putIntArray("buffs", buffArray);
        compound.putIntArray("buffTimeouts", timeoutArray);
    }

    public void loadNBTData(CompoundNBT compound) {
        onElevator = compound.getBoolean("onElevator");
        buffTimeout = compound.getInt("buffTicks");
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
