package mcjty.rftools;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

// Only used on 1.11
public class ForgeEventHandlers11 {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers11());
    }

    @SubscribeEvent
    public void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();
        World world = event.getEntity().getEntityWorld();

        Collection<GlobalCoordinate> protectors = BlockProtectors.getProtectors(world, x, y, z);
        if (BlockProtectors.checkHarvestProtection(x, y, z, world, protectors)) {
            event.setCanceled(true);
        }
    }
}
