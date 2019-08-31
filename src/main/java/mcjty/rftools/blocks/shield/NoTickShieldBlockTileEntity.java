package mcjty.rftools.blocks.shield;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class NoTickShieldBlockTileEntity extends TileEntity {

    private BlockState mimic = null;
    private int shieldColor;

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    protected int damageBits = 0;     // A 4-bit value indicating if a specific type of entity should get damage.
    private int collisionData = 0;  // A 4-bit value indicating collision detection data.

    // Coordinate of the shield block.
    protected BlockPos shieldBlock;

    protected AxisAlignedBB beamBox = null;

    public NoTickShieldBlockTileEntity(TileEntityType<?> type) {
        super(type);
    }

    // @todo 1.14
//    @Override
//    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
//        return oldState.getBlock() != newSate.getBlock();
//    }

    public void setDamageBits(int damageBits) {
        this.damageBits = damageBits;
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(getPos());
            world.notifyBlockUpdate(getPos(), state, state, 3);
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

    public void setCamoBlock(BlockState state) {
        mimic = state;
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
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        if (mimic != null) {
            CompoundNBT stateNbt = NBTUtil.writeBlockState(mimic);
            tagCompound.put("mimic", stateNbt);
        }
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
        write(updateTag);
        return updateTag;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if (tagCompound.contains("mimic")) {
            mimic = NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
        } else {
            mimic = null;
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

        if (world != null && world.isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            world.func_225319_b(getPos(), null, null);
//            world.markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        this.write(nbtTag);
        return new SUpdateTileEntityPacket(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        read(packet.getNbtCompound());
    }

    public void handleDamage(Entity entity) {
    }
}
