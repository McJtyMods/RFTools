package mcjty.rftools.blocks.builder;

import mcjty.lib.varia.Counter;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderTools {

    public static void returnChamberInfo(EntityPlayer player) {
        ItemStack cardItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (cardItem.isEmpty() || cardItem.getTagCompound() == null) {
            return;
        }

        int channel = cardItem.getTagCompound().getInteger("channel");
        if (channel == -1) {
            return;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(player.getEntityWorld());
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return;
        }

        int dimension = chamberChannel.getDimension();
        World world = DimensionManager.getWorld(dimension);
        if (world == null) {
            return;
        }

        Counter<IBlockState> blocks = new Counter<>();
        Counter<IBlockState> costs = new Counter<>();
        Map<IBlockState,ItemStack> stacks = new HashMap<>();

        BlockPos minCorner = chamberChannel.getMinCorner();
        BlockPos maxCorner = chamberChannel.getMaxCorner();
        findBlocks(player, world, blocks, costs, stacks, minCorner, maxCorner);

        Counter<String> entitiesWithCount = new Counter<>();
        Counter<String> entitiesWithCost = new Counter<>();
        Map<String,Entity> firstEntity = new HashMap<>();
        findEntities(world, minCorner, maxCorner, entitiesWithCount, entitiesWithCost, firstEntity);

        RFToolsMessages.INSTANCE.sendTo(new PacketChamberInfoReady(blocks, costs, stacks,
                entitiesWithCount, entitiesWithCost, firstEntity), (EntityPlayerMP) player);
    }

    private static void findEntities(World world, BlockPos minCorner, BlockPos maxCorner,
                                 Counter<String> entitiesWithCount, Counter<String> entitiesWithCost, Map<String, Entity> firstEntity) {
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(
                minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX() + 1, maxCorner.getY() + 1, maxCorner.getZ() + 1));
        for (Entity entity : entities) {
            String canonicalName = entity.getClass().getCanonicalName();
            if (entity instanceof EntityItem) {
                EntityItem entityItem = (EntityItem) entity;
                if (!entityItem.getItem().isEmpty()) {
                    String displayName = entityItem.getItem().getDisplayName();
                    canonicalName += " (" + displayName + ")";
                }
            }

            entitiesWithCount.increment(canonicalName);

            if (!firstEntity.containsKey(canonicalName)) {
                firstEntity.put(canonicalName, entity);
            }

            if (entity instanceof EntityPlayer) {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerPlayer.get());
            } else {
                entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerEntity.get());
            }
        }
    }

    private static void findBlocks(EntityPlayer harvester, World world, Counter<IBlockState> blocks, Counter<IBlockState> costs, Map<IBlockState, ItemStack> stacks, BlockPos minCorner, BlockPos maxCorner) {
        for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
            for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(p);
                    Block block = state.getBlock();
                    if (!BuilderTileEntity.isEmpty(state, block)) {
                        blocks.increment(state);

                        if (!stacks.containsKey(state)) {
                            ItemStack item = block.getItem(world, p, state);
                            if (!item.isEmpty()) {
                                stacks.put(state, item);
                            }
                        }

                        TileEntity te = world.getTileEntity(p);
                        BuilderSetup.BlockInformation info = BuilderTileEntity.getBlockInformation(harvester, world, p, block, te);
                        if (info.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                            costs.put(state, -1);
                        } else {
                            costs.increment(state, (int) (BuilderConfiguration.builderRfPerOperation.get() * info.getCostFactor()));
                        }
                    }
                }
            }
        }
    }

}
