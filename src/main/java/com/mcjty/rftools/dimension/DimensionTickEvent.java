package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.blocks.teleporter.RfToolsTeleporter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
                    // If there is an activity probe we only drain power if the dimension is loaded (a player is there or a chunkloader)
                    DimensionInformation information = dimensionManager.getDimensionInformation(id);
                    if (DimensionManager.getWorld(id) != null || information.getProbeCounter() == 0) {
                        int cost = information.getActualRfCost();
                        if (cost == 0) {
                            cost = entry.getValue().getRfMaintainCost();
                        }
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
                List<EntityPlayer> players = new ArrayList<EntityPlayer>(world.playerEntities);
                if (DimletConfiguration.dimensionDifficulty >= 1) {
                    for (EntityPlayer player : players) {
                        player.attackEntityFrom(DamageSource.generic, 1000.0f);
                    }
                } else {
                    Random random = new Random();
                    for (EntityPlayer player : players) {
                        WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(0);
                        int x = random.nextInt(2000) - 1000;
                        int z = random.nextInt(2000) - 1000;
                        int y = worldServerForDimension.getTopSolidOrLiquidBlock(x, z);
                        if (y == -1) {
                            y = 63;
                        }

                        MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, 0,
                                new RfToolsTeleporter(worldServerForDimension, x, y, z));
                    }
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN3) {
            // We are VERY low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.attackEntityFrom(DamageSource.generic, 0.1f);
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), MAXTICKS));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), MAXTICKS));
                    player.addPotionEffect(new PotionEffect(Potion.poison.getId(), MAXTICKS));
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN2) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), MAXTICKS));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), MAXTICKS));
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN1) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), MAXTICKS));
                }
            }
        }
    }

}
