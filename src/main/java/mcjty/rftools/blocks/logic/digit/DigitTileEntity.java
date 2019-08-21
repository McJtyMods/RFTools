package mcjty.rftools.blocks.logic.digit;


import mcjty.lib.tileentity.LogicTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DigitTileEntity extends LogicTileEntity {

    public static IntegerProperty VALUE = IntegerProperty.create("value", 0, 15);

    public int getPowerLevel() {
        return powerLevel;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        int p = getPowerLevel();
        super.onDataPacket(net, packet);
        if (p != getPowerLevel()) {
            world.func_225319_b(getPos(), null, null);
//            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public BlockState getActualState(BlockState state) {
        return super.getActualState(state).with(VALUE, getPowerLevel());
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        Direction inputSide = getFacing(world.getBlockState(pos)).getInputSide();
        int power = getInputStrength(world, pos, inputSide);
        int oldPower = getPowerLevel();
        setPowerInput(power);
        if (oldPower != power) {
            markDirtyClient();
        }
    }
}
