package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.playerprops.BuffProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
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

    public boolean isActive() {
        return active;
    }

    @Override
    public void tick(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
        if (!active) {
            return;
        }

        ticks--;
        if (ticks > 0) {
            return;
        }
        ticks = MAXTICKS;

        double maxsqdist = radius * radius;
        List<PlayerEntity> players = new ArrayList<>(world.getPlayers());
        for (PlayerEntity player : players) {
            double py = player.posY;
            if (py >= miny && py <= maxy) {
                double px = player.posX;
                double pz = player.posZ;
                double sqdist = (px-pos.getX()) * (px-pos.getX()) + (pz-pos.getZ()) * (pz-pos.getZ());
                if (sqdist < maxsqdist) {
                    if (controllerTileEntity.isPlayerAffected(player)) {
                        BuffProperties.addBuffToPlayer(player, buff, MAXTICKS);
                    }
                }
            }
        }

    }

    @Override
    public boolean apply(World world, BlockPos pos, LivingEntity entity, int duration) {
        return false;
    }

    @Override
    public void activate(boolean a) {
        if (active == a) {
            return;
        }
        active = a;
        ticks = 1;
    }
}
