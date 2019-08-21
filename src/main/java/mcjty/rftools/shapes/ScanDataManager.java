package mcjty.rftools.shapes;

import mcjty.lib.varia.Logging;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.servernet.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScanDataManager extends AbstractWorldData<ScanDataManager> {

    private static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";

    private int lastId = 0;

    private final Map<Integer, Scan> scans = new HashMap<>();

    // This data is not persisted
    private final Map<Integer, ScanExtraData> scanData = new HashMap<>();

    public ScanDataManager(String name) {
        super(name);
    }

    @Override
    public void clear() {
        scans.clear();
        scanData.clear();
        lastId = 0;
    }

    public void save(int scanId) {
        World world = DimensionManager.getWorld(0);
        File dataDir = new File(((ServerWorld) world).getChunkSaveLocation(), "rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, "scan" + scanId);
        Scan scan = getOrCreateScan(scanId);
        CompoundNBT tc = new CompoundNBT();
        scan.writeToNBTExternal(tc);
        try(DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file))) {
            CompressedStreamTools.writeCompressed(tc, dataoutputstream);
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing to file 'scan" + scan + "'!", e);
        }
        world.setData(SCANDATA_NETWORK_NAME, this);
        markDirty();
    }

    public ScanExtraData getExtraData(int id) {
        ScanExtraData data = scanData.get(id);
        if (data == null) {
            data = new ScanExtraData();
            scanData.put(id, data);
        } else {
            // Longer to accomodate for delay on locator
            if (data.getBirthTime() + (ScannerConfiguration.ticksPerLocatorScan.get()*100) < System.currentTimeMillis()) {
                data = new ScanExtraData();
                scanData.put(id, data);
            }
        }
        return data;
    }

    public static ScanDataManager getScans() {
        return getData(ScanDataManager.class, SCANDATA_NETWORK_NAME);
    }

    @Nonnull
    public Scan getOrCreateScan(int id) {
        Scan scan = scans.get(id);
        if (scan == null) {
            scan = new Scan();
            scans.put(id, scan);
        }
        return scan;
    }

    @Nonnull
    public Scan loadScan(int id) {
        World world = DimensionManager.getWorld(0);
        Scan scan = scans.get(id);
        if (scan == null || scan.getDataInt() == null) {
            if (scan == null) {
                scan = new Scan();
            }
            File dataDir = new File(((ServerWorld) world).getChunkSaveLocation(), "rftoolsscans");
            dataDir.mkdirs();
            File file = new File(dataDir, "scan" + id);
            if (file.exists()) {
                try(DataInputStream datainputstream = new DataInputStream(new FileInputStream(file))) {
                    CompoundNBT tag = CompressedStreamTools.readCompressed(datainputstream);
                    scan.readFromNBTExternal(tag);
                } catch (IOException e) {
                    Logging.log("Error reading scan file for id: " + id);
                }
            }
        }
        return scan;
    }

    public static void listScans(ICommandSender sender) {
        ScanDataManager scans = getScans();
        for (Map.Entry<Integer, Scan> entry : scans.scans.entrySet()) {
            Integer scanid = entry.getKey();
            scans.loadScan(scanid);
            Scan scan = entry.getValue();
            BlockPos dim = scan.getDataDim();
            if (dim == null) {
                sender.sendMessage(new TextComponentString(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.RED + "   Invalid"));
            } else {
                sender.sendMessage(new TextComponentString(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.YELLOW + "   Dim: " + TextFormatting.WHITE + dim.getX() + "," + dim.getY() + "," + dim.getZ() +
                                TextFormatting.YELLOW + "   Size: " + TextFormatting.WHITE + scan.getRledata().length + " bytes"));
            }
        }
    }


    public int newScan(World world) {
        lastId++;
        world.setData(SCANDATA_NETWORK_NAME, this);
        markDirty();
        return lastId;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        scans.clear();
        ListNBT lst = tagCompound.getTagList("scans", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < lst.tagCount(); i++) {
            CompoundNBT tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("scan");
            Scan scan = new Scan();
            scan.readFromNBT(tc);
            scans.put(id, scan);
        }
        lastId = tagCompound.getInt("lastId");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (Map.Entry<Integer, Scan> entry : scans.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            tc.setInteger("scan", entry.getKey());
            entry.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("scans", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

}
