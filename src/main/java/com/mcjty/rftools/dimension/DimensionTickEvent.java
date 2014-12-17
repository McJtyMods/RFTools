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
    private static final int MAXTICKS = 10;
    private int counter = MAXTICKS;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent evt) {
        counter--;
        if (counter <= 0) {
            counter = MAXTICKS;
            World entityWorld = MinecraftServer.getServer().getEntityWorld();
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(entityWorld);

            if (!dimensionManager.getDimensions().isEmpty()) {
                DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(entityWorld);

                for (Map.Entry<Integer, DimensionDescriptor> entry : dimensionManager.getDimensions().entrySet()) {
                    Integer id = entry.getKey();
                    // Only drain power if the dimension is loaded (a player is there or a chunkloader)
                    if (DimensionManager.getWorld(id) != null) {
                        int cost = entry.getValue().getRfMaintainCost();
                        int power = dimensionStorage.getEnergyLevel(id);
                        power -= cost * MAXTICKS;
                        if (power < 0) {
                            power = 0;
                        }

                        handleLowPower(id, power);

                        dimensionStorage.setEnergyLevel(id, power);
                    }
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
        } else if (power < DimletConfiguration.DIMPOWER_WARN3) {
            // We are VERY low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.attackEntityFrom(DamageSource.generic, 0.1f);
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.poison.getId(), 20));
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN2) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), 20));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 20));
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN1) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), 20));
                }
            }
        }
    }

}
