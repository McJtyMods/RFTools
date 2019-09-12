package mcjty.rftools.blocks.environmental.modules;

import mcjty.rftools.PlayerBuff;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public abstract class PotionEffectModule implements EnvironmentModule {
    public static final int MAXTICKS = 180;

    private final Potion potion;
    private final int amplifier;

    private boolean active = false;
    private int ticks = MAXTICKS;

    public PotionEffectModule(String potionname, int amplifier) {
        this.potion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation(potionname));
        this.amplifier = amplifier;
    }

    protected abstract PlayerBuff getBuff();

    protected boolean allowedForPlayers() {
        return true;
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

        EnvironmentalControllerTileEntity.EnvironmentalMode mode = controllerTileEntity.getMode();
        switch (mode) {
            case MODE_BLACKLIST:
            case MODE_WHITELIST:
                if (allowedForPlayers()) {
                    processPlayers(world, pos, radius, miny, maxy, controllerTileEntity);
                }
                break;
            case MODE_HOSTILE:
            case MODE_PASSIVE:
            case MODE_MOBS:
            case MODE_ALL:
                processEntities(world, pos, radius, miny, maxy, controllerTileEntity);
                break;
        }
    }

    private void processPlayers(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
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
                        // @todo 1.14
//                        player.addPotionEffect(new Effect(potion, MAXTICKS * 3, amplifier, true, false));
                        PlayerBuff buff = getBuff();
                        if (buff != null) {
                            // @todo 1.14 when env controller is ported
//                            BuffProperties.addBuffToPlayer(player, buff, MAXTICKS);
                        }
                    }
                }
            }
        }
    }

    private void processEntities(World world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
        double maxsqdist = radius * radius;
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius));
        for (LivingEntity entity : entities) {
            double py = entity.posY;
            if (py >= miny && py <= maxy) {
                double px = entity.posX;
                double pz = entity.posZ;
                double sqdist = (px-pos.getX()) * (px-pos.getX()) + (pz-pos.getZ()) * (pz-pos.getZ());
                if (sqdist < maxsqdist) {
                    if (controllerTileEntity.isEntityAffected(entity)) {
                        if (!(entity instanceof PlayerEntity) || allowedForPlayers()) {
                            // @todo 1.14
//                            entity.addPotionEffect(new Effect(potion, MAXTICKS * 3, amplifier, true, false));
                            PlayerBuff buff = getBuff();
                            if (buff != null) {
                                if (entity instanceof PlayerEntity) {
                                    // @todo 1.14 when env controller is ported
//                                    BuffProperties.addBuffToPlayer((PlayerEntity) entity, buff, MAXTICKS);
                                }
                            }
                        }
                    } else if (entity instanceof PlayerEntity) {
                        PlayerBuff buff = getBuff();
                        if (buff != null) {
                            // @todo 1.14 when env controller is ported
//                            BuffProperties.addBuffToPlayer((PlayerEntity) entity, buff, MAXTICKS);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean apply(World world, BlockPos pos, LivingEntity entity, int duration) {
// @todo 1.14 is that for loop the good replacement?
//        entity.addPotionEffect(new Effect(potion, duration, amplifier, true, false));
        for (EffectInstance effect : potion.getEffects()) {
            entity.addPotionEffect(effect);
        }

        return true;
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
