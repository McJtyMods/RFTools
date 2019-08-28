package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockProtectorEventHandlers {

    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();
        IWorld world = event.getWorld();

        Collection<GlobalCoordinate> protectors = BlockProtectors.getProtectors(world.getWorld(), x, y, z);
        if (BlockProtectors.checkHarvestProtection(x, y, z, world, protectors)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Vec3d explosionVector = explosion.getPosition();
        Collection<GlobalCoordinate> protectors = BlockProtectors.getProtectors(event.getWorld(), (int) explosionVector.x, (int) explosionVector.y, (int) explosionVector.z);

        if (protectors.isEmpty()) {
            return;
        }

        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        List<BlockPos> toremove = new ArrayList<>();

        for (GlobalCoordinate protector : protectors) {
            BlockPos pos = protector.getCoordinate();
            TileEntity te = event.getWorld().getTileEntity(pos);
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                for (BlockPos block : affectedBlocks) {
                    BlockPos relative = blockProtectorTileEntity.absoluteToRelative(block);
                    boolean b = blockProtectorTileEntity.isProtected(relative);
                    if (b) {
                        Vec3d blockVector = new Vec3d(block);
                        double distanceTo = explosionVector.distanceTo(blockVector);
                        int rfneeded = blockProtectorTileEntity.attemptExplosionProtection((float) (distanceTo / explosion.size), explosion.size);
                        if (rfneeded > 0) {
                            toremove.add(block);
                        } else {
                            blockProtectorTileEntity.removeProtection(relative);
                        }
                    }
                }
            }
        }

        affectedBlocks.removeAll(toremove);
    }

    @SubscribeEvent
    public static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();
        World world = event.getEntity().getEntityWorld();

        Collection<GlobalCoordinate> protectors = BlockProtectors.getProtectors(world, x, y, z);
        if (BlockProtectors.checkHarvestProtection(x, y, z, world, protectors)) {
            event.setCanceled(true);
        }
    }
}
