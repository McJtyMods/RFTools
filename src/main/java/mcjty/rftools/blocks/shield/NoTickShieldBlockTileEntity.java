package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class NoTickShieldBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;
    private int hasTe = 0;
    private int shieldColor;

    protected int damageBits = 0;     // A 4-bit value indicating if a specific type of entity should get damage.
    private int collisionData = 0;  // A 4-bit value indicating collision detection data.

    // Coordinate of the shield block.
    protected BlockPos shieldBlock;

    protected AxisAlignedBB beamBox = null;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    public void setDamageBits(int damageBits) {
        this.damageBits = damageBits;
        markDirty();
        worldObj.markBlockForUpdate(getPos());
    }

    public int getCollisionData() {
        return collisionData;
    }

    public void setCollisionData(int collisionData) {
        this.collisionData = collisionData;
        markDirty();
        worldObj.markBlockForUpdate(getPos());
    }

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        markDirty();
        worldObj.markBlockForUpdate(getPos());
    }

    public void setCamoBlock(int camoId, int hasTe) {
        this.camoId = camoId;
        this.hasTe = hasTe;
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(getPos());
        }
    }

    public void setShieldBlock(BlockPos c) {
        shieldBlock = c;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(getPos());
        }
    }

    public BlockPos getShieldBlock() {
        return shieldBlock;
    }

    public Block getBlock() {
        return block;
    }

    public boolean getHasTe() {
        return hasTe != 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        tagCompound.setInteger("hasTe", hasTe);
        tagCompound.setInteger("damageBits", damageBits);
        tagCompound.setInteger("collisionData", collisionData);
        tagCompound.setInteger("shieldColor", shieldColor);
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
        hasTe = tagCompound.getInteger("hasTe");
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        damageBits = tagCompound.getInteger("damageBits");
        collisionData = tagCompound.getInteger("collisionData");
        shieldColor = tagCompound.getInteger("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }

        int sx = tagCompound.getInteger("shieldX");
        int sy = tagCompound.getInteger("shieldY");
        int sz = tagCompound.getInteger("shieldZ");
        shieldBlock = new BlockPos(sx, sy, sz);

        if (worldObj != null && worldObj.isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
