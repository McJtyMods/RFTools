package mcjty.rftools.shapes;

import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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

    public ScanDataManager() {
        super(SCANDATA_NETWORK_NAME);
    }

    public void save(int scanId) {
        World world = WorldTools.getOverworld();
        File dataDir = null; // @todo 1.14 new File(((ServerWorld) world).getChunkSaveLocation(), "rftoolsscans");
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
        save();
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

    public static ScanDataManager get() {
        return getData(ScanDataManager::new, SCANDATA_NETWORK_NAME);
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
        World world = WorldTools.getOverworld();
        Scan scan = scans.get(id);
        if (scan == null || scan.getDataInt() == null) {
            if (scan == null) {
                scan = new Scan();
            }
            File dataDir = null; // @todo 1.14 new File(((ServerWorld) world).getChunkSaveLocation(), "rftoolsscans");
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

    public static void listScans(PlayerEntity sender) {
        ScanDataManager scans = get();
        for (Map.Entry<Integer, Scan> entry : scans.scans.entrySet()) {
            Integer scanid = entry.getKey();
            scans.loadScan(scanid);
            Scan scan = entry.getValue();
            BlockPos dim = scan.getDataDim();
            if (dim == null) {
                sender.sendMessage(new StringTextComponent(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.RED + "   Invalid"));
            } else {
                sender.sendMessage(new StringTextComponent(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.YELLOW + "   Dim: " + TextFormatting.WHITE + dim.getX() + "," + dim.getY() + "," + dim.getZ() +
                                TextFormatting.YELLOW + "   Size: " + TextFormatting.WHITE + scan.getRledata().length + " bytes"));
            }
        }
    }


    public int newScan(World world) {
        lastId++;
        save();
        return lastId;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        scans.clear();
        ListNBT lst = tagCompound.getList("scans", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < lst.size(); i++) {
            CompoundNBT tc = lst.getCompound(i);
            int id = tc.getInt("scan");
            Scan scan = new Scan();
            scan.readFromNBT(tc);
            scans.put(id, scan);
        }
        lastId = tagCompound.getInt("lastId");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (Map.Entry<Integer, Scan> entry : scans.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            tc.putInt("scan", entry.getKey());
            entry.getValue().writeToNBT(tc);
            lst.add(tc);
        }
        tagCompound.put("scans", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

}
