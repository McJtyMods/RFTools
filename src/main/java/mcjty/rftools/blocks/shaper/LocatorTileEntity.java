package mcjty.rftools.blocks.shaper;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.EntityTools;
import mcjty.lib.varia.Counter;
import mcjty.lib.varia.EnergyTools;
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

    private BeaconType hostileType = BeaconType.BEACON_OFF;
    private boolean hostileBeacon = false;
    private BeaconType passiveType = BeaconType.BEACON_YELLOW;
    private boolean passiveBeacon = false;
    private BeaconType playerType = BeaconType.BEACON_OFF;
    private boolean playerBeacon = false;
    private BeaconType energyType = BeaconType.BEACON_OFF;
    private boolean energyBeacon = false;

    private String filter = "";
    private Integer minEnergy = null;
    private Integer maxEnergy = null;

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
                findEntityBeacons(entities, extraData, center);
                findEnergyBeacons(scanner, extraData, dim, start, center);
            }
        }
    }

    private void findEnergyBeacons(ScannerTileEntity scanner, ScanExtraData extraData, BlockPos dim, BlockPos start, BlockPos center) {
        if (energyType != BeaconType.BEACON_OFF) {
            int dx = (dim.getX() + 15) / 16;
            int dz = (dim.getZ() + 15) / 16;

            int chunks = dx * dz;
            if (chunks <= ScannerConfiguration.locatorMaxEnergyChunks) {
                BlockPos end = scanner.getLastCorner();
                int minChunkX = start.getX() >> 4;
                int minChunkZ = start.getZ() >> 4;
                int maxChunkX = end.getX() >> 4;
                int maxChunkZ = end.getZ() >> 4;
                BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
                for (int cx = minChunkX ; cx <= maxChunkX ; cx++) {
                    for (int cz = minChunkZ ; cz <= maxChunkZ ; cz++) {
                        for (int x = 0 ; x < 16 ; x++) {
                            for (int z = 0 ; z < 16 ; z++) {
                                int rx = (cx << 4) + x;
                                int rz = (cz << 4) + z;
                                if (rx >= start.getX() && rx < end.getX() && rz >= start.getZ() && rz < end.getZ()) {
                                    for (int ry = start.getY() ; ry < end.getY() ; ry++) {
                                        mpos.setPos(rx, ry, rz);
                                        TileEntity tileEntity = getWorld().getTileEntity(mpos);
                                        if (EnergyTools.isEnergyTE(tileEntity)) {
                                            BlockPos pos = mpos.subtract(center);
                                            EnergyTools.EnergyLevelMulti el = EnergyTools.getEnergyLevelMulti(tileEntity);
                                            long max = el.getMaxEnergy();
                                            long e = el.getEnergy();
                                            int pct = max > 0 ? (int) (e * 100 / max) : 0;
                                            if (minEnergy != null && pct < minEnergy) {
                                                extraData.addBeacon(pos, energyType, energyBeacon);
                                            } else if (maxEnergy != null && pct > maxEnergy) {
                                                extraData.addBeacon(pos, energyType, energyBeacon);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void findEntityBeacons(List<Entity> entities, ScanExtraData extraData, BlockPos center) {
        Counter<BlockPos> counter = new Counter<>();
        boolean dofilter = filter != null && !filter.trim().isEmpty();
        String filt = "";
        if (dofilter) {
            filt = filter.toLowerCase();
        }

        for (Entity entity : entities) {
            BlockPos pos = entity.getPosition().subtract(center);
            if (counter.getOrDefault(pos, 0) < ScannerConfiguration.locatorEntitySafety) {
                if (dofilter) {
                    if (!checkFilter(filt, entity)) {
                        continue;
                    }
                }
                if (entity instanceof EntityAnimal) {
                    if (passiveType != BeaconType.BEACON_OFF) {
                        extraData.addBeacon(pos, passiveType, passiveBeacon);
                        counter.increment(pos);
                    }
                } else if (entity instanceof EntityPlayer) {
                    if (playerType != BeaconType.BEACON_OFF) {
                        extraData.addBeacon(pos, playerType, playerBeacon);
                        counter.increment(pos);
                    }
                } else {
                    if (hostileType != BeaconType.BEACON_OFF) {
                        extraData.addBeacon(pos, hostileType, hostileBeacon);
                        counter.increment(pos);
                    }
                }
            }
        }
    }

    private boolean checkFilter(String filt, Entity entity) {
        String name = EntityTools.getEntityName(entity).toLowerCase();
        if (name.contains(filt)) {
            return true;
        }

        // Check if the entity has a name tag
        if (entity.hasCustomName()) {
            String nameTag = entity.getCustomNameTag().toLowerCase();
            if (nameTag.contains(filt)) {
                return true;
            }
        }

        return false;
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
        int subchunks = dx * dy * dz;
        int chunks = dx * dz;

        int energy = ScannerConfiguration.LOCATOR_PERSCAN_BASE;
        energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_PERSCAN_CHUNK);
        if (hostileType != BeaconType.BEACON_OFF) {
            energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_PERSCAN_HOSTILE);
        }
        if (passiveType != BeaconType.BEACON_OFF) {
            energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_PERSCAN_PASSIVE);
        }
        if (playerType != BeaconType.BEACON_OFF) {
            energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_PERSCAN_PLAYER);
        }
        if (energyType != BeaconType.BEACON_OFF && chunks <= ScannerConfiguration.locatorMaxEnergyChunks) {
            energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_PERSCAN_ENERGY);
        }
        if (filter == null || !filter.trim().isEmpty()) {
            energy += (int) (energy + subchunks * ScannerConfiguration.LOCATOR_FILTER_COST);
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

    public String getFilter() {
        return filter;
    }

    public Integer getMinEnergy() {
        return minEnergy;
    }

    public Integer getMaxEnergy() {
        return maxEnergy;
    }

    public BeaconType getHostileType() {
        return hostileType;
    }

    public boolean isHostileBeacon() {
        return hostileBeacon;
    }

    public BeaconType getPassiveType() {
        return passiveType;
    }

    public boolean isPassiveBeacon() {
        return passiveBeacon;
    }

    public BeaconType getPlayerType() {
        return playerType;
    }

    public boolean isPlayerBeacon() {
        return playerBeacon;
    }

    public BeaconType getEnergyType() {
        return energyType;
    }

    public boolean isEnergyBeacon() {
        return energyBeacon;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
        hostileType = BeaconType.getTypeByCode(tagCompound.getString("hostile"));
        passiveType = BeaconType.getTypeByCode(tagCompound.getString("passive"));
        playerType = BeaconType.getTypeByCode(tagCompound.getString("player"));
        energyType = BeaconType.getTypeByCode(tagCompound.getString("energylow"));
        hostileBeacon = tagCompound.getBoolean("hostileBeacon");
        passiveBeacon = tagCompound.getBoolean("passiveBeacon");
        playerBeacon = tagCompound.getBoolean("playerBeacon");
        energyBeacon = tagCompound.getBoolean("energyBeacon");
        filter = tagCompound.getString("filter");
        if (tagCompound.hasKey("minEnergy")) {
            minEnergy = tagCompound.getInteger("minEnergy");
        } else {
            minEnergy = null;
        }
        if (tagCompound.hasKey("maxEnergy")) {
            maxEnergy = tagCompound.getInteger("maxEnergy");
        } else {
            maxEnergy = null;
        }
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
        tagCompound.setString("hostile", hostileType.getCode());
        tagCompound.setString("passive", passiveType.getCode());
        tagCompound.setString("player", playerType.getCode());
        tagCompound.setString("energylow", energyType.getCode());
        tagCompound.setBoolean("hostileBeacon", hostileBeacon);
        tagCompound.setBoolean("passiveBeacon", passiveBeacon);
        tagCompound.setBoolean("playerBeacon", playerBeacon);
        tagCompound.setBoolean("energyBeacon", energyBeacon);
        tagCompound.setString("filter", filter);
        if (minEnergy != null) {
            tagCompound.setInteger("minEnergy", minEnergy);
        }
        if (maxEnergy != null) {
            tagCompound.setInteger("maxEnergy", maxEnergy);
        }
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
            hostileType = BeaconType.getTypeByCode(args.get("hostile").getString());
            passiveType = BeaconType.getTypeByCode(args.get("passive").getString());
            playerType = BeaconType.getTypeByCode(args.get("player").getString());
            energyType = BeaconType.getTypeByCode(args.get("energy").getString());
            hostileBeacon = args.get("hostileBeacon").getBoolean();
            passiveBeacon = args.get("passiveBeacon").getBoolean();
            playerBeacon = args.get("playerBeacon").getBoolean();
            energyBeacon = args.get("energyBeacon").getBoolean();
            filter = args.get("filter").getString();
            if (args.containsKey("minEnergy")) {
                minEnergy = args.get("minEnergy").getInteger();
            } else {
                minEnergy = null;
            }
            if (args.containsKey("maxEnergy")) {
                maxEnergy = args.get("maxEnergy").getInteger();
            } else {
                maxEnergy = null;
            }
            markDirtyClient();
            return true;
        }
        return false;
    }

}
