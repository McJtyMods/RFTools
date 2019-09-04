package mcjty.rftools.setup;


import mcjty.lib.McJtyLib;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.Tools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.blockprotector.BlockProtectorSetup;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.blockprotector.GuiBlockProtector;
import mcjty.rftools.blocks.elevator.ElevatorSounds;
import mcjty.rftools.blocks.shaper.ProjectorSounds;
import mcjty.rftools.items.ModItems;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = RFTools.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // @todo 1.14
//        ModItems.initClient();
//        ModBlocks.initClient();
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ElevatorSounds.init(sounds.getRegistry());
        ProjectorSounds.init(sounds.getRegistry());
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent e) {
        ScreenManager.IScreenFactory<GenericContainer, GuiBlockProtector> factory = (container, inventory, title) -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(container.getPos());
            return Tools.safeMap(te, (BlockProtectorTileEntity i) -> new GuiBlockProtector(i, container, inventory), "Invalid tile entity!");
        };
        ScreenManager.registerFactory(BlockProtectorSetup.CONTAINER_PROTECTOR, factory);
    }
}
