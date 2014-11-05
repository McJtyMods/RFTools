package com.mcjty.rftools.blocks.shield;

import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class ShieldBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;
    private int meta = 0;

    // Coordinate of the shield block.
    private Coordinate shieldBlock;

    public int getCamoId() {
        return camoId;
    }

    public int getMeta() {
        return meta;
    }

    public void setCamoBlock(int camoId, int meta) {
        this.camoId = camoId;
        this.meta = meta;
        block = Block.getBlockById(camoId);
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setShieldBlock(Coordinate c) {
        shieldBlock = c;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public Coordinate getShieldBlock() {
        return shieldBlock;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        tagCompound.setInteger("camoMeta", meta);
        if (shieldBlock != null) {
            tagCompound.setInteger("shieldX", shieldBlock.getX());
            tagCompound.setInteger("shieldY", shieldBlock.getY());
            tagCompound.setInteger("shieldZ", shieldBlock.getZ());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        camoId = tagCompound.getInteger("camoId");
        meta = tagCompound.getInteger("camoMeta");
        block = Block.getBlockById(camoId);

        int sx = tagCompound.getInteger("shieldX");
        int sy = tagCompound.getInteger("shieldY");
        int sz = tagCompound.getInteger("shieldZ");
        shieldBlock = new Coordinate(sx, sy, sz);

        if (worldObj != null && worldObj.isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
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
