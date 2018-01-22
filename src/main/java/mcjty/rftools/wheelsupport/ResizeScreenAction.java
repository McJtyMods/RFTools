package mcjty.rftools.wheelsupport;

import mcjty.intwheel.api.IWheelAction;
import mcjty.intwheel.api.WheelActionElement;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenHitBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ResizeScreenAction implements IWheelAction {

    public static final String ACTION_RESIZESCREEN = "rftools.resizescreen";

    @Override
    public String getId() {
        return ACTION_RESIZESCREEN;
    }

    @Override
    public WheelActionElement createElement() {
        return new WheelActionElement(ACTION_RESIZESCREEN).description("Resize the screen", null).texture("rftools:textures/gui/wheel_actions.png", 32, 0, 32, 0+32, 128, 128);
    }

    @Override
    public boolean performClient(EntityPlayer player, World world, @Nullable BlockPos pos, boolean extended) {
        return true;
    }

    @Override
    public void performServer(EntityPlayer player, World world, @Nullable BlockPos pos, boolean extended) {
        if (pos != null) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof ScreenBlock) {
                ((ScreenBlock) block).cycleSizeMode(world, pos);
            } else if (block instanceof ScreenHitBlock) {
                BlockPos screenPos = ((ScreenHitBlock) block).getScreenBlockPos(world, pos);
                if (screenPos != null) {
                    block = world.getBlockState(screenPos).getBlock();
                    if (block instanceof ScreenBlock) {
                        ((ScreenBlock) block).cycleSizeMode(world, screenPos);
                    }
                }
            }
        }
    }
}
