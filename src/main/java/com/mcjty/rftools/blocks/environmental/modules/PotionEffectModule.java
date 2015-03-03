package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import com.mcjty.rftools.PlayerExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class PotionEffectModule implements EnvironmentModule {
    public static final int MAXTICKS = 180;

    private final int potionEffect;
    private final int amplifier;

    private boolean active = false;
    private int ticks = MAXTICKS;

    public PotionEffectModule(int potionEffect, int amplifier) {
        this.potionEffect = potionEffect;
        this.amplifier = amplifier;
    }

    protected abstract PlayerBuff getBuff();

    @Override
    public void tick(World world, int x, int y, int z, int radius, int miny, int maxy) {
        if (!active) {
            return;
        }

        ticks--;
        if (ticks > 0) {
            return;
        }
        ticks = MAXTICKS;

        double maxsqdist = radius * radius;
        List<EntityPlayer> players = new ArrayList<EntityPlayer>(world.playerEntities);
        for (EntityPlayer player : players) {
            double py = player.posY;
            if (py >= miny && py <= maxy) {
                double px = player.posX;
                double pz = player.posZ;
                double sqdist = (px-x) * (px-x) + (pz-z) * (pz-z);
                if (sqdist < maxsqdist) {
                    player.addPotionEffect(new PotionEffect(potionEffect, MAXTICKS, amplifier, true));
                    PlayerBuff buff = getBuff();
                    if (buff != null) {
                        PlayerExtendedProperties.addBuff(player, buff, MAXTICKS);
                    }
                }
            }
        }
    }

    @Override
    public void activate(boolean a) {
        if (active == a) {
            return;
        }
        active = a;
        ticks = MAXTICKS;
    }
}
