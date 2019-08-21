package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NoTickShieldBlockTileEntity extends TileEntity {

    private BlockState mimic = null;
    private int camoId = -1;
    private int camoMeta = 0;
    private int shieldColor;

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    protected int damageBits = 0;     // A 4-bit value indicating if a specific type of entity should get damage.
    private int collisionData = 0;  // A 4-bit value indicating collision detection data.

    // Coordinate of the shield block.
    protected BlockPos shieldBlock;

    protected AxisAlignedBB beamBox = null;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    public void setDamageBits(int damageBits) {
        this.damageBits = damageBits;
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            BlockState state = getWorld().getBlockState(getPos());
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

    public BlockState getMimicBlock() {
        return mimic;
    }

    public void setCamoBlock(int camoId, int meta) {
        this.camoId = camoId;
        this.camoMeta = meta;
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

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.putInt("camoId", camoId);
        tagCompound.putInt("camoMeta", camoMeta);
        tagCompound.putInt("damageBits", damageBits);
        tagCompound.putInt("collisionData", collisionData);
        tagCompound.putInt("shieldColor", shieldColor);
        tagCompound.putInt("stt", shieldRenderingMode.ordinal());
        if (shieldBlock != null) {
            tagCompound.putInt("shieldX", shieldBlock.getX());
            tagCompound.putInt("shieldY", shieldBlock.getY());
            tagCompound.putInt("shieldZ", shieldBlock.getZ());
        }
        return tagCompound;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        camoId = tagCompound.getInt("camoId");
        camoMeta = tagCompound.getInt("camoMeta");
        if (camoId == -1) {
            mimic = null;
        } else {
            mimic = Block.getBlockById(camoId).getStateFromMeta(camoMeta);
        }
        damageBits = tagCompound.getInt("damageBits");
        collisionData = tagCompound.getInt("collisionData");
        shieldColor = tagCompound.getInt("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }
        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInt("stt")];

        int sx = tagCompound.getInt("shieldX");
        int sy = tagCompound.getInt("shieldY");
        int sz = tagCompound.getInt("shieldZ");
        shieldBlock = new BlockPos(sx, sy, sz);

        if (getWorld() != null && getWorld().isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public void handleDamage(Entity entity) {
    }
}
