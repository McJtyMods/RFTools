package mcjty.rftools.blocks.logic.digit;


import mcjty.lib.tileentity.LogicTileEntity;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DigitTileEntity extends LogicTileEntity {

    public static PropertyInteger VALUE = PropertyInteger.create("value", 0, 15);

    public int getPowerLevel() {
        return powerLevel;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        int p = getPowerLevel();
        super.onDataPacket(net, packet);
        if (p != getPowerLevel()) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return super.getActualState(state).withProperty(VALUE, getPowerLevel());
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        EnumFacing inputSide = getFacing(world.getBlockState(pos)).getInputSide();
        int power = getInputStrength(world, pos, inputSide);
        int oldPower = getPowerLevel();
        setPowerInput(power);
        if (oldPower != power) {
            markDirtyClient();
        }
    }
}
