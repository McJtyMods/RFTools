package mcjty.rftools.wheelsupport;

import mcjty.intwheel.api.IInteractionWheel;
import mcjty.intwheel.api.IWheelActionProvider;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenHitBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class WheelCompatibility {

    private static boolean registered;

    public static void register() {
        if (registered)
            return;
        registered = true;
        FMLInterModComms.sendFunctionMessage("intwheel", "getTheWheel", "mcjty.rftools.wheelsupport.WheelCompatibility$GetTheWheel");
    }


    public static class GetTheWheel implements com.google.common.base.Function<IInteractionWheel, Void> {

        public static IInteractionWheel wheel;

        @Nullable
        @Override
        public Void apply(IInteractionWheel theWheel) {
            wheel = theWheel;
            Logging.getLogger().log(Level.INFO, "Enabled support for The Interaction Wheel");
            wheel.registerProvider(new IWheelActionProvider() {
                @Override
                public String getID() {
                    return RFTools.MODID + ".wheel";
                }

                @Override
                public void updateWheelActions(@Nonnull Set<String> actions, @Nonnull EntityPlayer player, World world, @Nullable BlockPos pos) {
                    if (pos != null) {
                        Block block = world.getBlockState(pos).getBlock();
                        if (block instanceof GenericBlock) {
                            actions.add(RemoveBlockAction.ACTION_REMOVEBLOCK);
                        }
                        if (block instanceof ScreenBlock || block instanceof ScreenHitBlock) {
                            actions.add(ResizeScreenAction.ACTION_RESIZESCREEN);
                            actions.add(ToggleScreenTransparentAction.ACTION_TOGGLESCREENTRANSPARENT);
                        }
                    }
                }
            });
            wheel.getRegistry().register(new RemoveBlockAction());
            wheel.getRegistry().register(new ResizeScreenAction());
            wheel.getRegistry().register(new ToggleScreenTransparentAction());
            return null;
        }
    }
}
