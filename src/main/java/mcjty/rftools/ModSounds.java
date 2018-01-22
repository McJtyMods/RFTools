package mcjty.rftools;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {

    public static void init(IForgeRegistry<SoundEvent> registry) {
        SoundEvent whoosh = new SoundEvent(new ResourceLocation(RFTools.MODID, "teleport_whoosh")).setRegistryName(new ResourceLocation(RFTools.MODID, "teleport_whoosh"));
        SoundEvent error = new SoundEvent(new ResourceLocation(RFTools.MODID, "teleport_error")).setRegistryName(new ResourceLocation(RFTools.MODID, "teleport_error"));
        registry.register(whoosh);
        registry.register(error);
    }

}
