package mcjty.rftools;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {
    public static final SoundEvent whoosh = new SoundEvent(new ResourceLocation(RFTools.MODID, "teleport_whoosh")).setRegistryName(new ResourceLocation(RFTools.MODID, "teleport_whoosh"));
    public static final SoundEvent error = new SoundEvent(new ResourceLocation(RFTools.MODID, "teleport_error")).setRegistryName(new ResourceLocation(RFTools.MODID, "teleport_error"));

    public static void init(IForgeRegistry<SoundEvent> registry) {
        registry.register(whoosh);
        registry.register(error);
    }

}
