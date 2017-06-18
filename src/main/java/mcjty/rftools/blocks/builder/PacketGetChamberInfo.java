package mcjty.rftools.blocks.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.BlockMeta;
import mcjty.lib.varia.Counter;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketGetChamberInfo implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetChamberInfo() {
    }

    public static class Handler implements IMessageHandler<PacketGetChamberInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketGetChamberInfo message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
            return null;
        }

        private void handle(MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            ItemStack cardItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (ItemStackTools.isEmpty(cardItem) || cardItem.getTagCompound() == null) {
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

            Counter<BlockMeta> blocks = new Counter<>();
            Counter<BlockMeta> costs = new Counter<>();
            Map<BlockMeta,ItemStack> stacks = new HashMap<>();

            BlockPos minCorner = chamberChannel.getMinCorner();
            BlockPos maxCorner = chamberChannel.getMaxCorner();
            findBlocks(world, blocks, costs, stacks, minCorner, maxCorner);

            Counter<String> entitiesWithCount = new Counter<>();
            Counter<String> entitiesWithCost = new Counter<>();
            Map<String,Entity> firstEntity = new HashMap<>();
            findEntities(world, minCorner, maxCorner, entitiesWithCount, entitiesWithCost, firstEntity);

            RFToolsMessages.INSTANCE.sendTo(new PacketChamberInfoReady(blocks, costs, stacks,
                    entitiesWithCount, entitiesWithCost, firstEntity), ctx.getServerHandler().player);
        }

        private void findEntities(World world, BlockPos minCorner, BlockPos maxCorner,
                                  Counter<String> entitiesWithCount, Counter<String> entitiesWithCost, Map<String, Entity> firstEntity) {
            List entities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(
                    minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX() + 1, maxCorner.getY() + 1, maxCorner.getZ() + 1));
            for (Object o : entities) {
                Entity entity = (Entity) o;
                String canonicalName = entity.getClass().getCanonicalName();
                if (entity instanceof EntityItem) {
                    EntityItem entityItem = (EntityItem) entity;
                    if (ItemStackTools.isValid(entityItem.getItem())) {
                        String displayName = entityItem.getItem().getDisplayName();
                        canonicalName += " (" + displayName + ")";
                    }
                }

                entitiesWithCount.increment(canonicalName);

                if (!firstEntity.containsKey(canonicalName)) {
                    firstEntity.put(canonicalName, entity);
                }

                if (entity instanceof EntityPlayer) {
                    entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerPlayer);
                } else {
                    entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerEntity);
                }
            }
        }

        private void findBlocks(World world, Counter<BlockMeta> blocks, Counter<BlockMeta> costs, Map<BlockMeta, ItemStack> stacks, BlockPos minCorner, BlockPos maxCorner) {
            for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
                for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                    for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                        BlockPos p = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(p);
                        Block block = state.getBlock();
                        if (!BuilderTileEntity.isEmpty(state, block)) {
                            int meta = block.getMetaFromState(state);
                            BlockMeta bm = new BlockMeta(block, meta);
                            blocks.increment(bm);

                            if (!stacks.containsKey(bm)) {
                                ItemStack item = block.getItem(world, p, state);
                                if (ItemStackTools.isValid(item)) {
                                    stacks.put(bm, item);
                                }
                            }

                            TileEntity te = world.getTileEntity(p);
                            BuilderSetup.BlockInformation info = BuilderTileEntity.getBlockInformation(world, p, block, te);
                            if (info.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                                costs.put(bm, -1);
                            } else {
                                costs.increment(bm, (int) (BuilderConfiguration.builderRfPerOperation * info.getCostFactor()));
                            }
                        }
                    }
                }
            }
        }

    }
}