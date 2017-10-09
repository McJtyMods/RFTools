package mcjty.rftools.blocks.shaper;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.shapes.BeaconType;
import mcjty.rftools.shapes.ScanDataManager;
import mcjty.rftools.shapes.ScanExtraData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class LocatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    public static final String CMD_MODE = "setMode";
    public static final String CMD_SETTINGS = "setSettings";

    private int counter = 0;

    private BeaconType hostile = BeaconType.BEACON_OFF;
    private boolean hostileBeacon = false;
    private BeaconType passive = BeaconType.BEACON_YELLOW;
    private boolean passiveBeacon = false;
    private BeaconType player = BeaconType.BEACON_OFF;
    private boolean playerBeacon = false;

    public LocatorTileEntity() {
        super(ScannerConfiguration.LOCATOR_MAXENERGY, ScannerConfiguration.LOCATOR_RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && isMachineEnabled()) {
            counter--;
            markDirtyQuick();
            if (counter <= 0) {
                counter = ScannerConfiguration.ticksPerLocatorScan;

                ScannerTileEntity scanner = getScanner();
                if (scanner == null || scanner.getDataDim() == null) {
                    // No scanner, do nothing
                    return;
                }

                int energy = getEnergyPerScan(scanner);
                if (getEnergyStored() < energy) {
                    // Do nothing
                    return;
                }
                // @todo energy consumption per area?
                consumeEnergy(energy);
                BlockPos dim = scanner.getDataDim();
                BlockPos start = scanner.getFirstCorner();
                AxisAlignedBB bb = new AxisAlignedBB(start, start.add(dim));

                List<Entity> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bb);
                int scanId = scanner.getScanId();
                ScanExtraData extraData = ScanDataManager.getScans().getExtraData(scanId);
                extraData.touch();
                extraData.clear();

                BlockPos center = scanner.getScanCenter();
                for (Entity entity : entities) {
                    if (entity instanceof EntityAnimal) {
                        if (passive != BeaconType.BEACON_OFF) {
                            extraData.addBeacon(entity.getPosition().subtract(center), passive, passiveBeacon);
                        }
                    } else if (entity instanceof EntityPlayer) {
                        if (player != BeaconType.BEACON_OFF) {
                            extraData.addBeacon(entity.getPosition().subtract(center), player, playerBeacon);
                        }
                    } else {
                        if (hostile != BeaconType.BEACON_OFF) {
                            extraData.addBeacon(entity.getPosition().subtract(center), hostile, hostileBeacon);
                        }
                    }
                }
            }
        }
    }

    public int getEnergyPerScan() {
        ScannerTileEntity scanner = getScanner();
        if (scanner == null) {
            return Integer.MAX_VALUE;
        }
        return getEnergyPerScan(scanner);
    }

    public int getEnergyPerScan(ScannerTileEntity scanner) {
        BlockPos dim = scanner.getDataDim();
        if (dim == null) {
            return Integer.MAX_VALUE;
        }
        int dx = (dim.getX() + 15) / 16;
        int dy = (dim.getY() + 15) / 16;
        int dz = (dim.getZ() + 15) / 16;
        int chunks = dx * dy * dz;

        int energy = ScannerConfiguration.LOCATOR_PERSCAN_BASE;
        energy += (int) (energy + chunks * ScannerConfiguration.LOCATOR_PERSCAN_CHUNK);
        if (hostile != BeaconType.BEACON_OFF) {
            energy += (int) (energy + chunks * ScannerConfiguration.LOCATOR_PERSCAN_HOSTILE);
        }
        if (passive != BeaconType.BEACON_OFF) {
            energy += (int) (energy + chunks * ScannerConfiguration.LOCATOR_PERSCAN_PASSIVE);
        }
        if (player != BeaconType.BEACON_OFF) {
            energy += (int) (energy + chunks * ScannerConfiguration.LOCATOR_PERSCAN_PLAYER);
        }
        return energy;
    }

    private ScannerTileEntity getScanner() {
        TileEntity te = getWorld().getTileEntity(getPos().down());
        if (te instanceof ScannerTileEntity) {
            return (ScannerTileEntity) te;
        }
        return null;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    public BeaconType getHostile() {
        return hostile;
    }

    public boolean isHostileBeacon() {
        return hostileBeacon;
    }

    public BeaconType getPassive() {
        return passive;
    }

    public boolean isPassiveBeacon() {
        return passiveBeacon;
    }

    public BeaconType getPlayer() {
        return player;
    }

    public boolean isPlayerBeacon() {
        return playerBeacon;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
        hostile = BeaconType.getTypeByCode(tagCompound.getString("hostile"));
        passive = BeaconType.getTypeByCode(tagCompound.getString("passive"));
        player = BeaconType.getTypeByCode(tagCompound.getString("player"));
        hostileBeacon = tagCompound.getBoolean("hostileBeacon");
        passiveBeacon = tagCompound.getBoolean("passiveBeacon");
        playerBeacon = tagCompound.getBoolean("playerBeacon");
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
        tagCompound.setString("hostile", hostile.getCode());
        tagCompound.setString("passive", passive.getCode());
        tagCompound.setString("player", player.getCode());
        tagCompound.setBoolean("hostileBeacon", hostileBeacon);
        tagCompound.setBoolean("passiveBeacon", passiveBeacon);
        tagCompound.setBoolean("playerBeacon", playerBeacon);
    }


    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        } else if (CMD_SETTINGS.equals(command)) {
            hostile = BeaconType.getTypeByCode(args.get("hostile").getString());
            passive = BeaconType.getTypeByCode(args.get("passive").getString());
            player = BeaconType.getTypeByCode(args.get("player").getString());
            hostileBeacon = args.get("hostileBeacon").getBoolean();
            passiveBeacon = args.get("passiveBeacon").getBoolean();
            playerBeacon = args.get("playerBeacon").getBoolean();
            markDirtyClient();
            return true;
        }
        return false;
    }

}
