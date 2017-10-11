package mcjty.rftools.shapes;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class Scan {

    private byte[] rledata;
    private List<IBlockState> materialPalette = new ArrayList<>();
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

    public void setData(byte[] data, List<IBlockState> materialPalette, BlockPos dim, BlockPos offset) {
        this.rledata = data;
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
        tagCompound.setByteArray("data", rledata == null ? new byte[0] : rledata);
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
        rledata = tagCompound.getByteArray("data");
        dataDim = new BlockPos(tagCompound.getInteger("scandimx"), tagCompound.getInteger("scandimy"), tagCompound.getInteger("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInteger("scanoffx"), tagCompound.getInteger("scanoffy"), tagCompound.getInteger("scanoffz"));
    }
}
