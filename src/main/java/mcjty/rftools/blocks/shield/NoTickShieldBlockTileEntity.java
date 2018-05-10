package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NoTickShieldBlockTileEntity extends TileEntity {

    private IBlockState mimic = null;
    private int camoId = -1;
    private int camoMeta = 0;
    private int hasTe = 0;
    private int shieldColor;

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    protected int damageBits = 0;     // A 4-bit value indicating if a specific type of bindings should get damage.
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
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    public int getCollisionData() {
        return collisionData;
    }

    public void setCollisionData(int collisionData) {
        this.collisionData = collisionData;
        markDirtyClient();
    }

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        markDirtyClient();
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;
        markDirtyClient();
    }

    public IBlockState getMimicBlock() {
        return mimic;
    }

    public void setCamoBlock(int camoId, int meta, int hasTe) {
        this.camoId = camoId;
        this.camoMeta = meta;
        this.hasTe = hasTe;
        if (camoId == -1) {
            mimic = null;
        } else {
            mimic = Block.getBlockById(camoId).getStateFromMeta(meta);
        }
        markDirtyClient();
    }

    public void setShieldBlock(BlockPos c) {
        shieldBlock = c;
        markDirtyClient();
    }

    public BlockPos getShieldBlock() {
        return shieldBlock;
    }

    public boolean getHasTe() {
        return hasTe != 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        tagCompound.setInteger("camoMeta", camoMeta);
        tagCompound.setInteger("hasTe", hasTe);
        tagCompound.setInteger("damageBits", damageBits);
        tagCompound.setInteger("collisionData", collisionData);
        tagCompound.setInteger("shieldColor", shieldColor);
        tagCompound.setInteger("stt", shieldRenderingMode.ordinal());
        if (shieldBlock != null) {
            tagCompound.setInteger("shieldX", shieldBlock.getX());
            tagCompound.setInteger("shieldY", shieldBlock.getY());
            tagCompound.setInteger("shieldZ", shieldBlock.getZ());
        }
        return tagCompound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        camoId = tagCompound.getInteger("camoId");
        camoMeta = tagCompound.getInteger("camoMeta");
        hasTe = tagCompound.getInteger("hasTe");
        if (camoId == -1) {
            mimic = null;
        } else {
            mimic = Block.getBlockById(camoId).getStateFromMeta(camoMeta);
        }
        damageBits = tagCompound.getInteger("damageBits");
        collisionData = tagCompound.getInteger("collisionData");
        shieldColor = tagCompound.getInteger("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }
        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInteger("stt")];

        int sx = tagCompound.getInteger("shieldX");
        int sy = tagCompound.getInteger("shieldY");
        int sz = tagCompound.getInteger("shieldZ");
        shieldBlock = new BlockPos(sx, sy, sz);

        if (getWorld() != null && getWorld().isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }
}
