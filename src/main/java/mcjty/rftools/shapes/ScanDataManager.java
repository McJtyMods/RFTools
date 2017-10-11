package mcjty.rftools.shapes;

import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScanDataManager extends WorldSavedData {

    public static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";
    private static ScanDataManager instance = null;

    private int lastId = 0;

    private final Map<Integer, Scan> scans = new HashMap<>();

    // This data is not persisted
    private final Map<Integer, ScanExtraData> scanData = new HashMap<>();

    public ScanDataManager(String identifier) {
        super(identifier);
    }

    public void save(World world, int scanId) {
        File dataDir = new File(((WorldServer) world).getChunkSaveLocation(), "rftoolsscans");
        dataDir.mkdirs();
        File file = new File(dataDir, "scan" + scanId);
        Scan scan = getOrCreateScan(scanId);
        NBTTagCompound tc = new NBTTagCompound();
        scan.writeToNBTExternal(tc);
        DataOutputStream dataoutputstream = null;
        try {
            dataoutputstream = new DataOutputStream(new FileOutputStream(file));
            try {
                CompressedStreamTools.writeCompressed(tc, dataoutputstream);
            } catch (IOException e) {
                throw new RuntimeException("Error writing to file 'scan" + scan + "'!", e);
            } finally {
                dataoutputstream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file 'scan" + scan + "'!", e);
        }
        world.setData(SCANDATA_NETWORK_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.scans.clear();
            instance = null;
        }
    }

    public ScanExtraData getExtraData(int id) {
        ScanExtraData data = scanData.get(id);
        if (data == null) {
            data = new ScanExtraData();
            scanData.put(id, data);
        } else {
            // Longer to accomodate for delay on locator
            if (data.getBirthTime() + (ScannerConfiguration.ticksPerLocatorScan*100) < System.currentTimeMillis()) {
                data = new ScanExtraData();
                scanData.put(id, data);
            }
        }
        return data;
    }

    public static ScanDataManager getScans() {
        World world = DimensionManager.getWorld(0);
        if (instance != null) {
            return instance;
        }
        instance = (ScanDataManager) world.loadData(ScanDataManager.class, SCANDATA_NETWORK_NAME);
        if (instance == null) {
            instance = new ScanDataManager(SCANDATA_NETWORK_NAME);
        }
        return instance;
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
            File dataDir = new File(((WorldServer) world).getChunkSaveLocation(), "rftoolsscans");
            dataDir.mkdirs();
            File file = new File(dataDir, "scan" + id);
            if (file.exists()) {
                try {
                    DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
                    try {
                        NBTTagCompound tag = CompressedStreamTools.readCompressed(datainputstream);
                        scan.readFromNBTExternal(tag);
                    } catch (IOException e) {
                        Logging.log("Error reading scan file for id: " + id);
                    } finally {
                        datainputstream.close();
                    }
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        scans.clear();
        NBTTagList lst = tagCompound.getTagList("scans", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < lst.tagCount(); i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("scan");
            Scan scan = new Scan();
            scan.readFromNBT(tc);
            scans.put(id, scan);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, Scan> entry : scans.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("scan", entry.getKey());
            entry.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("scans", lst);
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }

}
