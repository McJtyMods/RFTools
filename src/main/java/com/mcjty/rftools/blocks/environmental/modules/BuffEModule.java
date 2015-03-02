package com.mcjty.rftools.blocks.environmental.modules;

import com.mcjty.rftools.PlayerBuff;
import com.mcjty.rftools.PlayerExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class BuffEModule implements EnvironmentModule {
    public static final int MAXTICKS = 180;

    private boolean active = false;
    private int ticks = MAXTICKS;
    private final PlayerBuff buff;

    public BuffEModule(PlayerBuff buff) {
        this.buff = buff;
    }

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
                    PlayerExtendedProperties.addBuff(player, buff, MAXTICKS);
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
