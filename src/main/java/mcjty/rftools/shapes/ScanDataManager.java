package mcjty.rftools.shapes;

import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.WorldTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanDataManager extends WorldSavedData {

    public static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";
    private static ScanDataManager instance = null;

    private int lastId = 0;

    private final Map<Integer, Scan> scans = new HashMap<>();

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
        WorldTools.saveData(world, SCANDATA_NETWORK_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.scans.clear();
            instance = null;
        }
    }

    public static ScanDataManager getScansClient() {
        if (instance == null) {
            instance = new ScanDataManager(SCANDATA_NETWORK_NAME);
        }
        return instance;
    }

    public int getScanDirtyCounterClient(int id) {
        Scan scan;
        if (!scans.containsKey(id)) {
            scan = new Scan();
            scans.put(id, scan);
        } else {
            scan = scans.get(id);
        }
        scan.dirtyRequestTimeout--;
        if (scan.dirtyRequestTimeout <= 0) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketRequestScanDirty(id));
            scan.dirtyRequestTimeout = 20;
        }
        return scan.getDirtyCounter();
    }

    public static ScanDataManager getScans() {
        World world = DimensionManager.getWorld(0);
        if (instance != null) {
            return instance;
        }
        instance = mcjty.lib.tools.WorldTools.loadData(world, ScanDataManager.class, SCANDATA_NETWORK_NAME);
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
        }
        return scan;
    }

    @Nonnull
    public Scan loadScan(int id) {
        World world = DimensionManager.getWorld(0);
        Scan scan = scans.get(id);
        if (scan == null || scan.data == null) {
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
                ChatTools.addChatMessage(sender, new TextComponentString(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.RED + "   Invalid"));
            } else {
                ChatTools.addChatMessage(sender, new TextComponentString(
                        TextFormatting.YELLOW + "Scan: " + TextFormatting.WHITE + scanid +
                                TextFormatting.YELLOW + "   Dim: " + TextFormatting.WHITE + dim.getX() + "," + dim.getY() + "," + dim.getZ() +
                                TextFormatting.YELLOW + "   Size: " + TextFormatting.WHITE + scan.data.length + " bytes"));
            }
        }
    }


    public int newScan(World world) {
        lastId++;
        WorldTools.saveData(world, SCANDATA_NETWORK_NAME, this);
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

    public static class Scan {
        private byte[] data;
        private List<IBlockState> materialPalette = new ArrayList<>();
        private BlockPos dataDim;
        private BlockPos dataOffset = new BlockPos(0, 0, 0);
        private int dirtyCounter = 0;
        private int dirtyRequestTimeout = 0;   // Client side only

        public static final byte[] EMPTY = new byte[0];

        public byte[] getData() {
            if (data == null) {
                return EMPTY;
            }
            return data;
        }


        public void setData(byte[] data, List<IBlockState> materialPalette, BlockPos dim, BlockPos offset) {
            this.data = data;
            this.materialPalette = materialPalette;
            this.dataDim = dim;
            this.dataOffset = offset;
            dirtyCounter++;
        }

        void setDirtyCounter(int dirtyCounter) {
            this.dirtyCounter = dirtyCounter;
        }

        public int getDirtyCounter() {
            return dirtyCounter;
        }

        public List<IBlockState> getMaterialPalette() {
            return materialPalette;
        }

        public BlockPos getDataDim() {
            return dataDim;
        }

        public BlockPos getDataOffset() {
            return dataOffset;
        }

        public void writeToNBT(NBTTagCompound tagCompound) {
            tagCompound.setInteger("dirty", dirtyCounter);
        }

        public void writeToNBTExternal(NBTTagCompound tagCompound) {
            tagCompound.setByteArray("data", data);
            NBTTagList pal = new NBTTagList();
            for (IBlockState state : materialPalette) {
                NBTTagCompound tc = new NBTTagCompound();
                Block block = state.getBlock();
                if (block == null || block.getRegistryName() == null) {
                    tc.setString("r", Blocks.STONE.getRegistryName().toString());
                    tc.setInteger("m", 0);
                } else {
                    tc.setString("r", block.getRegistryName().toString());
                    tc.setInteger("m", block.getMetaFromState(state));
                }
                pal.appendTag(tc);
            }
            tagCompound.setTag("scanpal", pal);
            if (dataDim != null) {
                tagCompound.setInteger("scandimx", dataDim.getX());
                tagCompound.setInteger("scandimy", dataDim.getY());
                tagCompound.setInteger("scandimz", dataDim.getZ());
            }
            if (dataOffset != null) {
                tagCompound.setInteger("scanoffx", dataOffset.getX());
                tagCompound.setInteger("scanoffy", dataOffset.getY());
                tagCompound.setInteger("scanoffz", dataOffset.getZ());
            }
        }

        public void readFromNBT(NBTTagCompound tagCompound) {
            dirtyCounter = tagCompound.getInteger("dirty");
        }

        public void readFromNBTExternal(NBTTagCompound tagCompound) {
            NBTTagList list = tagCompound.getTagList("scanpal", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tc = list.getCompoundTagAt(i);
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tc.getString("r")));
                if (block == null) {
                    block = Blocks.STONE;
                }
                materialPalette.add(block.getStateFromMeta(tc.getInteger("m")));
            }
            data = tagCompound.getByteArray("data");
            dataDim = new BlockPos(tagCompound.getInteger("scandimx"), tagCompound.getInteger("scandimy"), tagCompound.getInteger("scandimz"));
            dataOffset = new BlockPos(tagCompound.getInteger("scanoffx"), tagCompound.getInteger("scanoffy"), tagCompound.getInteger("scanoffz"));
        }
    }
}
