package mcjty.rftools.shapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class Scan {

    private byte[] rledata;
    private List<BlockState> materialPalette = new ArrayList<>();
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);
    private int dirtyCounter = 0;

    public int dirtyRequestTimeout = 0;   // Client side only

    public static final byte[] EMPTY = new byte[0];

    public byte[] getRledata() {
        if (rledata == null) {
            return EMPTY;
        }
        return rledata;
    }

    public byte[] getDataInt() {
        return rledata;
    }

    public void setData(byte[] data, List<BlockState> materialPalette, BlockPos dim, BlockPos offset) {
        this.rledata = data;
        this.materialPalette = materialPalette;
        this.dataDim = dim;
        this.dataOffset = offset;
        dirtyCounter++;
    }

    public void setDirtyCounter(int dirtyCounter) {
        this.dirtyCounter = dirtyCounter;
    }

    public int getDirtyCounter() {
        return dirtyCounter;
    }

    public List<BlockState> getMaterialPalette() {
        return materialPalette;
    }

    public BlockPos getDataDim() {
        return dataDim;
    }

    public BlockPos getDataOffset() {
        return dataOffset;
    }

    public void writeToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("dirty", dirtyCounter);
    }

    public void writeToNBTExternal(CompoundNBT tagCompound) {
        tagCompound.putByteArray("data", rledata == null ? new byte[0] : rledata);
        ListNBT pal = new ListNBT();
        for (BlockState state : materialPalette) {
            CompoundNBT tc = new CompoundNBT();
            Block block = state.getBlock();
            // @todo 1.14 need to serialize blockstate now that meta is gone!
            if (block == null || block.getRegistryName() == null) {
                tc.putString("r", Blocks.STONE.getRegistryName().toString());
            } else {
                tc.putString("r", block.getRegistryName().toString());
            }
            pal.add(tc);
        }
        tagCompound.put("scanpal", pal);
        if (dataDim != null) {
            tagCompound.putInt("scandimx", dataDim.getX());
            tagCompound.putInt("scandimy", dataDim.getY());
            tagCompound.putInt("scandimz", dataDim.getZ());
        }
        if (dataOffset != null) {
            tagCompound.putInt("scanoffx", dataOffset.getX());
            tagCompound.putInt("scanoffy", dataOffset.getY());
            tagCompound.putInt("scanoffz", dataOffset.getZ());
        }
    }

    public void readFromNBT(CompoundNBT tagCompound) {
        dirtyCounter = tagCompound.getInt("dirty");
    }

    public void readFromNBTExternal(CompoundNBT tagCompound) {
        ListNBT list = tagCompound.getList("scanpal", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tc = list.getCompound(i);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tc.getString("r")));
            if (block == null) {
                block = Blocks.STONE;
            }
            // @todo 1.14 deserialize blockstate now that meta is gone
//            materialPalette.add(block.getStateFromMeta(tc.getInteger("m")));
        }
        rledata = tagCompound.getByteArray("data");
        dataDim = new BlockPos(tagCompound.getInt("scandimx"), tagCompound.getInt("scandimy"), tagCompound.getInt("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInt("scanoffx"), tagCompound.getInt("scanoffy"), tagCompound.getInt("scanoffz"));
    }
}
