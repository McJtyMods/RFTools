package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.blocks.teleporter.RfToolsTeleporter;
import com.mcjty.rftools.dimension.world.types.EffectType;
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

import java.util.*;

public class DimensionTickEvent {
    private static final int MAXTICKS = 10;
    private int counter = MAXTICKS;

    private static final int MAXTICKS_EFFECTS = 180;
    private int counterEffects = MAXTICKS_EFFECTS;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent evt) {
        handlePower();
        handleEffects();
    }

    private void handlePower() {
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

    private void handleEffects() {
        counterEffects--;
        if (counterEffects <= 0) {
            counterEffects = MAXTICKS_EFFECTS;
            World entityWorld = MinecraftServer.getServer().getEntityWorld();
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(entityWorld);

            if (!dimensionManager.getDimensions().isEmpty()) {
                for (Map.Entry<Integer, DimensionDescriptor> entry : dimensionManager.getDimensions().entrySet()) {
                    Integer id = entry.getKey();
                    DimensionInformation information = dimensionManager.getDimensionInformation(id);
                    if (DimensionManager.getWorld(id) != null) {
                        handleEffectsForDimension(id, information);
                    }
                }
            }
        }
    }

    static Map<EffectType,Integer> effectsMap = new HashMap<EffectType, Integer>();
    static Map<EffectType,Integer> effectAmplifierMap = new HashMap<EffectType, Integer>();

    static {
        effectsMap.put(EffectType.EFFECT_POISON, Potion.poison.getId());
        effectsMap.put(EffectType.EFFECT_POISON2, Potion.poison.getId());
        effectAmplifierMap.put(EffectType.EFFECT_POISON2, 1);
        effectsMap.put(EffectType.EFFECT_POISON3, Potion.poison.getId());
        effectAmplifierMap.put(EffectType.EFFECT_POISON3, 2);

        effectsMap.put(EffectType.EFFECT_REGENERATION, Potion.regeneration.getId());
        effectsMap.put(EffectType.EFFECT_REGENERATION2, Potion.regeneration.getId());
        effectAmplifierMap.put(EffectType.EFFECT_REGENERATION2, 1);
        effectsMap.put(EffectType.EFFECT_REGENERATION3, Potion.regeneration.getId());
        effectAmplifierMap.put(EffectType.EFFECT_REGENERATION3, 2);

        effectsMap.put(EffectType.EFFECT_MOVESLOWDOWN, Potion.moveSlowdown.getId());
        effectsMap.put(EffectType.EFFECT_MOVESLOWDOWN2, Potion.moveSlowdown.getId());
        effectAmplifierMap.put(EffectType.EFFECT_MOVESLOWDOWN2, 1);
        effectsMap.put(EffectType.EFFECT_MOVESLOWDOWN3, Potion.moveSlowdown.getId());
        effectAmplifierMap.put(EffectType.EFFECT_MOVESLOWDOWN3, 2);

        effectsMap.put(EffectType.EFFECT_MOVESPEED, Potion.moveSpeed.getId());
        effectsMap.put(EffectType.EFFECT_MOVESPEED2, Potion.moveSpeed.getId());
        effectAmplifierMap.put(EffectType.EFFECT_MOVESPEED2, 1);
        effectsMap.put(EffectType.EFFECT_MOVESPEED3, Potion.moveSpeed.getId());
        effectAmplifierMap.put(EffectType.EFFECT_MOVESPEED3, 2);

        effectsMap.put(EffectType.EFFECT_DIGSLOWDOWN, Potion.digSlowdown.getId());
        effectsMap.put(EffectType.EFFECT_DIGSLOWDOWN2, Potion.digSlowdown.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DIGSLOWDOWN2, 1);
        effectsMap.put(EffectType.EFFECT_DIGSLOWDOWN3, Potion.digSlowdown.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DIGSLOWDOWN3, 2);

        effectsMap.put(EffectType.EFFECT_DIGSPEED, Potion.digSpeed.getId());
        effectsMap.put(EffectType.EFFECT_DIGSPEED2, Potion.digSpeed.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DIGSPEED2, 1);
        effectsMap.put(EffectType.EFFECT_DIGSPEED3, Potion.digSpeed.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DIGSPEED3, 2);

        effectsMap.put(EffectType.EFFECT_DAMAGEBOOST, Potion.damageBoost.getId());
        effectsMap.put(EffectType.EFFECT_DAMAGEBOOST2, Potion.damageBoost.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DAMAGEBOOST2, 1);
        effectsMap.put(EffectType.EFFECT_DAMAGEBOOST3, Potion.damageBoost.getId());
        effectAmplifierMap.put(EffectType.EFFECT_DAMAGEBOOST3, 2);

        effectsMap.put(EffectType.EFFECT_HEAL, Potion.heal.getId());
        effectsMap.put(EffectType.EFFECT_HARM, Potion.harm.getId());

        effectsMap.put(EffectType.EFFECT_JUMP, Potion.jump.getId());
        effectsMap.put(EffectType.EFFECT_JUMP2, Potion.jump.getId());
        effectAmplifierMap.put(EffectType.EFFECT_JUMP2, 1);
        effectsMap.put(EffectType.EFFECT_JUMP3, Potion.jump.getId());
        effectAmplifierMap.put(EffectType.EFFECT_JUMP3, 2);

        effectsMap.put(EffectType.EFFECT_CONFUSION, Potion.confusion.getId());

        effectsMap.put(EffectType.EFFECT_RESISTANCE, Potion.resistance.getId());
        effectsMap.put(EffectType.EFFECT_RESISTANCE2, Potion.resistance.getId());
        effectAmplifierMap.put(EffectType.EFFECT_RESISTANCE2, 1);
        effectsMap.put(EffectType.EFFECT_RESISTANCE3, Potion.resistance.getId());
        effectAmplifierMap.put(EffectType.EFFECT_RESISTANCE3, 2);

        effectsMap.put(EffectType.EFFECT_FIRERESISTANCE, Potion.fireResistance.getId());
        effectsMap.put(EffectType.EFFECT_WATERBREATHING, Potion.waterBreathing.getId());
        effectsMap.put(EffectType.EFFECT_INVISIBILITY, Potion.invisibility.getId());
        effectsMap.put(EffectType.EFFECT_BLINDNESS, Potion.blindness.getId());
        effectsMap.put(EffectType.EFFECT_NIGHTVISION, Potion.nightVision.getId());

        effectsMap.put(EffectType.EFFECT_HUNGER, Potion.hunger.getId());
        effectsMap.put(EffectType.EFFECT_HUNGER2, Potion.hunger.getId());
        effectAmplifierMap.put(EffectType.EFFECT_HUNGER2, 1);
        effectsMap.put(EffectType.EFFECT_HUNGER3, Potion.hunger.getId());
        effectAmplifierMap.put(EffectType.EFFECT_HUNGER3, 2);

        effectsMap.put(EffectType.EFFECT_WEAKNESS, Potion.weakness.getId());
        effectsMap.put(EffectType.EFFECT_WEAKNESS2, Potion.weakness.getId());
        effectAmplifierMap.put(EffectType.EFFECT_WEAKNESS2, 1);
        effectsMap.put(EffectType.EFFECT_WEAKNESS3, Potion.weakness.getId());
        effectAmplifierMap.put(EffectType.EFFECT_WEAKNESS3, 2);

        effectsMap.put(EffectType.EFFECT_WITHER, Potion.wither.getId());
        effectsMap.put(EffectType.EFFECT_WITHER2, Potion.wither.getId());
        effectAmplifierMap.put(EffectType.EFFECT_WITHER2, 1);
        effectsMap.put(EffectType.EFFECT_WITHER3, Potion.wither.getId());
        effectAmplifierMap.put(EffectType.EFFECT_WITHER3, 2);

        effectsMap.put(EffectType.EFFECT_HEALTHBOOST, Potion.field_76434_w.getId());
        effectsMap.put(EffectType.EFFECT_HEALTHBOOST2, Potion.field_76434_w.getId());
        effectAmplifierMap.put(EffectType.EFFECT_HEALTHBOOST2, 1);
        effectsMap.put(EffectType.EFFECT_HEALTHBOOST3, Potion.field_76434_w.getId());
        effectAmplifierMap.put(EffectType.EFFECT_HEALTHBOOST3, 2);

        effectsMap.put(EffectType.EFFECT_ABSORPTION, Potion.field_76444_x.getId());
        effectsMap.put(EffectType.EFFECT_ABSORPTION2, Potion.field_76444_x.getId());
        effectAmplifierMap.put(EffectType.EFFECT_ABSORPTION2, 1);
        effectsMap.put(EffectType.EFFECT_ABSORPTION3, Potion.field_76444_x.getId());
        effectAmplifierMap.put(EffectType.EFFECT_ABSORPTION3, 2);

        effectsMap.put(EffectType.EFFECT_SATURATION, Potion.field_76443_y.getId());
        effectsMap.put(EffectType.EFFECT_SATURATION2, Potion.field_76443_y.getId());
        effectAmplifierMap.put(EffectType.EFFECT_SATURATION2, 1);
        effectsMap.put(EffectType.EFFECT_SATURATION3, Potion.field_76443_y.getId());
        effectAmplifierMap.put(EffectType.EFFECT_SATURATION3, 2);
    }

    private void handleEffectsForDimension(int id, DimensionInformation information) {
        WorldServer world = DimensionManager.getWorld(id);
        if (world != null) {
            Set<EffectType> effects = information.getEffectTypes();
            List<EntityPlayer> players = new ArrayList<EntityPlayer>(world.playerEntities);
            for (EntityPlayer player : players) {
                for (EffectType effect : effects) {
                    Integer potionEffect = effectsMap.get(effect);
                    if (potionEffect != null) {
                        Integer amplifier = effectAmplifierMap.get(effect);
                        if (amplifier == null) {
                            amplifier = 0;
                        }
                        player.addPotionEffect(new PotionEffect(potionEffect, MAXTICKS_EFFECTS, amplifier, true));       // @todo increase to a longer period
                    }
                }

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
                        WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(DimletConfiguration.spawnDimension);
                        int x = random.nextInt(2000) - 1000;
                        int z = random.nextInt(2000) - 1000;
                        int y = worldServerForDimension.getTopSolidOrLiquidBlock(x, z);
                        if (y == -1) {
                            y = 63;
                        }

                        MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, DimletConfiguration.spawnDimension,
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
                    player.addPotionEffect(new PotionEffect(Potion.poison.getId(), MAXTICKS, 2));
                }
            }
        } else if (power < DimletConfiguration.DIMPOWER_WARN2) {
            // We are low on power. Start bad effects.
            WorldServer world = DimensionManager.getWorld(id);
            if (world != null) {
                for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), MAXTICKS));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), MAXTICKS, 2));
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
