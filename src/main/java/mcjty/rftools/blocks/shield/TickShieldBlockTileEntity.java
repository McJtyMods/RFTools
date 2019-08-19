package mcjty.rftools.blocks.shield;

import mcjty.rftools.blocks.shield.filters.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class TickShieldBlockTileEntity extends NoTickShieldBlockTileEntity {

    @Override
    public void handleDamage(Entity entity) {
        if (damageBits == 0 || getWorld().isRemote || getWorld().getTotalWorldTime() % 10 != 0) {
            return;
        }
        if (beamBox == null) {
            int xCoord = getPos().getX();
            int yCoord = getPos().getY();
            int zCoord = getPos().getZ();
            beamBox = new AxisAlignedBB(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);
        }

        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) getWorld().getTileEntity(shieldBlock);
            if (shieldTileEntity != null) {
                if (entity.getEntityBoundingBox().intersects(beamBox)) {
                    if ((damageBits & AbstractShieldBlock.META_HOSTILE) != 0 && entity instanceof IMob) {
                        if (checkEntityDamage(shieldTileEntity, HostileFilter.HOSTILE)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((damageBits & AbstractShieldBlock.META_PASSIVE) != 0 && entity instanceof IAnimals) {
                        if (checkEntityDamage(shieldTileEntity, AnimalFilter.ANIMAL)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((damageBits & AbstractShieldBlock.META_PLAYERS) != 0 && entity instanceof PlayerEntity) {
                        if (checkPlayerDamage(shieldTileEntity, (PlayerEntity) entity)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private boolean checkEntityDamage(ShieldTEBase shieldTileEntity, String filterName) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (filterName.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            }
        }
        return false;
    }

    private boolean checkPlayerDamage(ShieldTEBase shieldTileEntity, PlayerEntity entity) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                String name = playerFilter.getName();
                if ((name == null || name.isEmpty())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                } else if (name.equals(entity.getName())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                }
            }
        }
        return false;
    }

}
