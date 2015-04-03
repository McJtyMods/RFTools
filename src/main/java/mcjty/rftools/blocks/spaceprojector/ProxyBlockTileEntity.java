package mcjty.rftools.blocks.spaceprojector;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ProxyBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;

    public void setCamoBlock(int camoId) {
        this.camoId = camoId;
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public Block getBlock() {
        return block;
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        camoId = tagCompound.getInteger("camoId");
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
    }
}
