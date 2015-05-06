package mcjty.rftools;

import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketSendBuffsToClient;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.*;

public class PlayerExtendedProperties implements IExtendedEntityProperties {
    public static final String ID = "RFToolsPlayerProperties";

    public static final int BUFF_MAXTICKS = 180;
    private int buffTimeout;

    private int target;
    private int teleportTimeout;
    private Entity entity = null;
    private boolean globalSyncNeeded = true;

    private Set<GlobalCoordinate> favoriteDestinations = new HashSet<GlobalCoordinate>();

    // Here we mirror the flags out of capabilities so that we can restore them.
    private boolean oldAllowFlying = false;
    private boolean allowFlying = false;

    private final Map<PlayerBuff,Integer> buffs = new HashMap<PlayerBuff, Integer>();

    public PlayerExtendedProperties() {
        target = -1;
        teleportTimeout = -1;
        buffTimeout = 0;
        globalSyncNeeded = true;
    }

    public boolean isTeleporting() {
        return target != -1 && teleportTimeout >= 0;
    }

    private void syncBuffs() {
        PacketHandler.INSTANCE.sendTo(new PacketSendBuffsToClient(buffs), (EntityPlayerMP) entity);
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

    public static PlayerExtendedProperties getProperties(EntityPlayer player) {
        IExtendedEntityProperties properties = player.getExtendedProperties(ID);
        return (PlayerExtendedProperties) properties;
    }

    public static void addBuff(EntityPlayer player, PlayerBuff buff, int ticks) {
        PlayerExtendedProperties playerExtendedProperties = getProperties(player);
        playerExtendedProperties.addBuff(buff, ticks);
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

    public boolean isDestinationFavorite(GlobalCoordinate coordinate) {
        return favoriteDestinations.contains(coordinate);
    }

    public void setDestinationFavorite(GlobalCoordinate coordinate, boolean favorite) {
        if (favorite) {
            favoriteDestinations.add(coordinate);
        } else {
            favoriteDestinations.remove(coordinate);
        }
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

            boolean syncNeeded = false;
            for (Map.Entry<PlayerBuff, Integer> entry : copyBuffs.entrySet()) {
                int timeout = entry.getValue();
                timeout -= BUFF_MAXTICKS;
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
                TeleportationTools.applyEffectForSeverity(player, 3, false);
                return;
            }
            TeleportDestination destination = destinations.getDestination(coordinate);
            TeleportationTools.performTeleport((EntityPlayer) entity, destination, 0, 10, false);

            teleportTimeout = -1;
            target = -1;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("target", target);
        compound.setInteger("ticks", teleportTimeout);
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
        writeFavoritesToNBT(compound, favoriteDestinations);
    }

    private static void writeFavoritesToNBT(NBTTagCompound tagCompound, Collection<GlobalCoordinate> destinations) {
        NBTTagList lst = new NBTTagList();
        for (GlobalCoordinate destination : destinations) {
            NBTTagCompound tc = new NBTTagCompound();
            Coordinate c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
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
        for (int i = 0 ; i < buffArray.length ; i++) {
            int buffIdx = buffArray[i];
            buffs.put(PlayerBuff.values()[buffIdx], timeoutArray[i]);
        }
        allowFlying = compound.getBoolean("allowFlying");
        oldAllowFlying = compound.getBoolean("oldAllowFlying");
        globalSyncNeeded = true;

        favoriteDestinations.clear();
        readCoordinatesFromNBT(compound, favoriteDestinations);
    }

    private static  void readCoordinatesFromNBT(NBTTagCompound tagCompound, Set<GlobalCoordinate> destinations) {
        NBTTagList lst = tagCompound.getTagList("destinations", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            Coordinate c = new Coordinate(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            destinations.add(new GlobalCoordinate(c, tc.getInteger("dim")));
        }
    }


    @Override
    public void init(Entity entity, World world) {
        this.entity = entity;
        globalSyncNeeded = true;
    }
}
