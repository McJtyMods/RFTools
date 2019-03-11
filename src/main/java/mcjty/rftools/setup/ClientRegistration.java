package mcjty.rftools.setup;


import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.elevator.ElevatorSounds;
import mcjty.rftools.blocks.shaper.ProjectorSounds;
import mcjty.rftools.items.ModItems;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModItems.initClient();
        ModBlocks.initClient();
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ElevatorSounds.init(sounds.getRegistry());
        ProjectorSounds.init(sounds.getRegistry());
    }

}
