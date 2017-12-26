package mcjty.rftools.blocks.endergen;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EndergenicEventHandlers {

    @SubscribeEvent
    public static void onPostWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            for (EndergenicTileEntity endergenic : EndergenicTileEntity.todoEndergenics) {
                endergenic.checkStateServer();
            }
            EndergenicTileEntity.todoEndergenics.clear();
            EndergenicTileEntity.endergenicsAdded.clear();
        }
    }
}
