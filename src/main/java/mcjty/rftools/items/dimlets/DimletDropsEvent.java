package mcjty.rftools.items.dimlets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.Random;

public class DimletDropsEvent {

    private Random random = new Random();

    @SubscribeEvent
    public void onEntityDrop(LivingDropsEvent event) {
        if (event.entityLiving instanceof EntityEnderman) {
            if (random.nextFloat() < DimletConfiguration.endermanUnknownDimletDrop) {
                event.entityLiving.dropItem(DimletSetup.unknownDimlet, random.nextInt(2)+1);
            }
        }
    }
}
