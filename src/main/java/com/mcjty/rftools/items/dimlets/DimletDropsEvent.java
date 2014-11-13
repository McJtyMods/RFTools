package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.Random;

public class DimletDropsEvent {

    private Random random = new Random();

    @SubscribeEvent
    public void onEntityDrop(LivingDropsEvent event) {
        if (event.entityLiving instanceof EntityEnderman) {
            if (random.nextFloat() < .01f) {
                event.entityLiving.dropItem(ModItems.unknownDimlet, random.nextInt(2)+1);
            }
        }
    }
}
