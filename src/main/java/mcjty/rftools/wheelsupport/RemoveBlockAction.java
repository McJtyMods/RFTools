package mcjty.rftools.wheelsupport;

import mcjty.intwheel.api.IWheelAction;
import mcjty.intwheel.api.WheelActionElement;
import mcjty.lib.container.GenericBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RemoveBlockAction implements IWheelAction {

    public static final String ACTION_REMOVEBLOCK = "rftools.removeblock";

    @Override
    public String getId() {
        return ACTION_REMOVEBLOCK;
    }

    @Override
    public WheelActionElement createElement() {
        return new WheelActionElement(ACTION_REMOVEBLOCK).description("Remove a block", null).texture("rftools:textures/gui/wheel_actions.png", 0, 0, 0, 0+32, 128, 128);
    }

    @Override
    public boolean performClient(EntityPlayer player, World world, @Nullable BlockPos pos, boolean extended) {
        return true;
    }

    @Override
    public void performServer(EntityPlayer player, World world, @Nullable BlockPos pos, boolean extended) {
        if (pos != null) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof GenericBlock) {
                block.harvestBlock(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos), ItemStack.EMPTY);
            }
        }
    }
}
