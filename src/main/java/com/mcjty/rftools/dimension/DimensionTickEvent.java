package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.List;
import java.util.Map;

public class DimensionTickEvent {
    private int counter = 20;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent evt) {
        counter--;
        if (counter <= 0) {
            counter = 20;
            World entityWorld = MinecraftServer.getServer().getEntityWorld();
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(entityWorld);

            if (!dimensionManager.getDimensions().isEmpty()) {
                DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(entityWorld);

                for (Map.Entry<Integer, DimensionDescriptor> entry : dimensionManager.getDimensions().entrySet()) {
                    Integer id = entry.getKey();

                    int cost = entry.getValue().getRfMaintainCost();
                    int power = dimensionStorage.getEnergyLevel(id);
                    power -= cost * 20;
                    if (power < 0) {
                        power = 0;
                    }
                    System.out.println("Consume energy for dimension, id=" + id + ", power=" + power);

                    handleLowPower(id, power);

                    dimensionStorage.setEnergyLevel(id, power);
                }

                dimensionStorage.save(entityWorld);
            }
        }
    }

    private void handleLowPower(Integer id, int power) {
        if (power <= 0) {
            // We ran out of power!
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.attackEntityFrom(DamageSource.generic, 1000.0f);
                }
            }
        } else if (power < (DimletConfiguration.MAX_DIMENSION_POWER / 30)) {
            // We are VERY low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.attackEntityFrom(DamageSource.generic, 0.1f);
                    player.addPotionEffect(new PotionEffect(Potion.confusion.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.wither.getId(), 20));
                }
            }
        } else if (power < (DimletConfiguration.MAX_DIMENSION_POWER / 20)) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.confusion.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 20));
                }
            }
        } else if (power < (DimletConfiguration.MAX_DIMENSION_POWER / 10)) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.confusion.getId(), 20));
                }
            }
        }
    }

}
