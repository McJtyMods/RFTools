package mcjty.rftools.blocks.spaceprojector;

import mcjty.lib.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class ProxyBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;

    private Coordinate origCoordinate;
    private int dimension = 0;

    @Override
    public boolean canUpdate() {
        return false;
    }

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

    public void setOrigCoordinate(Coordinate o, int dimension) {
        origCoordinate = o;
        this.dimension = dimension;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public Block getBlock() {
        return block;
    }

    public Coordinate getOrigCoordinate() {
        return origCoordinate;
    }

    public int getDimension() {
        return dimension;
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
        origCoordinate = Coordinate.readFromNBT(tagCompound, "oCoord");
        dimension = tagCompound.getInteger("dimension");

        if (worldObj != null && worldObj.isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        Coordinate.writeToNBT(tagCompound, "oCoord", origCoordinate);
        tagCompound.setInteger("dimension", dimension);
    }


    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
