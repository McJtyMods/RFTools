package mcjty.rftools.setup;


import mcjty.lib.blocks.BaseBlockItem;
import mcjty.rftools.ModSounds;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.security.SecuritySetup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Registration {


    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(SecuritySetup.createSecurityManagerBlock());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(RFTools.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(SecuritySetup.securityManagerBlock, properties));

//        event.getRegistry().register(new Item(properties).setRegistryName("machine_frame"));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ModSounds.init(sounds.getRegistry());
    }

}
