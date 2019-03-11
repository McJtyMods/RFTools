package mcjty.rftools.compat.wheelsupport;

import mcjty.intwheel.api.IWheelAction;
import mcjty.intwheel.api.WheelActionElement;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenHitBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ToggleScreenTransparentAction implements IWheelAction {

    public static final String ACTION_TOGGLESCREENTRANSPARENT = "rftools.toggletransp";

    @Override
    public String getId() {
        return ACTION_TOGGLESCREENTRANSPARENT;
    }

    @Override
    public WheelActionElement createElement() {
        return new WheelActionElement(ACTION_TOGGLESCREENTRANSPARENT).description("Toggle screen transparency", null).texture("rftools:textures/gui/wheel_actions.png", 64, 0, 64, 0+32, 128, 128);
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
                ((ScreenBlock) block).cycleTranspMode(world, pos);
            } else if (block instanceof ScreenHitBlock) {
                BlockPos screenPos = ((ScreenHitBlock) block).getScreenBlockPos(world, pos);
                if (screenPos != null) {
                    block = world.getBlockState(screenPos).getBlock();
                    if (block instanceof ScreenBlock) {
                        ((ScreenBlock) block).cycleTranspMode(world, screenPos);
                    }
                }
            }
        }
    }
}
