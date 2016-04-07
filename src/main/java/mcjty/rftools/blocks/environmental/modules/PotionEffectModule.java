package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.playerprops.BuffProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class PotionEffectModule implements EnvironmentModule {
    public static final int MAXTICKS = 180;

    private final Potion potion;
    private final int amplifier;

    private boolean active = false;
    private int ticks = MAXTICKS;

    public PotionEffectModule(String potionname, int amplifier) {
        this.potion = Potion.potionRegistry.getObject(new ResourceLocation(potionname));
        this.amplifier = amplifier;
    }

    protected abstract PlayerBuff getBuff();

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
        List<EntityPlayer> players = new ArrayList<EntityPlayer>(world.playerEntities);
        for (EntityPlayer player : players) {
            double py = player.posY;
            if (py >= miny && py <= maxy) {
                double px = player.posX;
                double pz = player.posZ;
                double sqdist = (px-pos.getX()) * (px-pos.getX()) + (pz-pos.getZ()) * (pz-pos.getZ());
                if (sqdist < maxsqdist) {
                    if (controllerTileEntity.isPlayerAffected(player)) {
                        player.addPotionEffect(new PotionEffect(potion, MAXTICKS * 3, amplifier, true, false));
                        PlayerBuff buff = getBuff();
                        if (buff != null) {
                            BuffProperties.addBuff(player, buff, MAXTICKS);
                        }
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
        ticks = 1;
    }
}
