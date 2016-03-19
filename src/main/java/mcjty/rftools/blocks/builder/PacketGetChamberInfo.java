package mcjty.rftools.blocks.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.BlockMeta;
import mcjty.lib.varia.Counter;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

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
            MinecraftServer.getServer().addScheduledTask(() -> handle(ctx));
            return null;
        }

        private void handle(MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack cardItem = player.getHeldItem();
            if (cardItem == null || cardItem.getTagCompound() == null) {
                return;
            }

            int channel = cardItem.getTagCompound().getInteger("channel");
            if (channel == -1) {
                return;
            }

            SpaceChamberRepository repository = SpaceChamberRepository.getChannels(player.worldObj);
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
            BlockPos minCorner = chamberChannel.getMinCorner();
            BlockPos maxCorner = chamberChannel.getMaxCorner();
            for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
                for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                    for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                        BlockPos p = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(p);
                        Block block = state.getBlock();
                        if (!BuilderTileEntity.isEmpty(block)) {
                            int meta = block.getMetaFromState(state);
                            BlockMeta bm = new BlockMeta(block, meta);
                            blocks.increment(bm);

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

            Counter<String> entitiesWithCount = new Counter<>();
            Counter<String> entitiesWithCost = new Counter<>();
            List entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.fromBounds(
                    minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX() + 1, maxCorner.getY() + 1, maxCorner.getZ() + 1));
            for (Object o : entities) {
                Entity entity = (Entity) o;
                String canonicalName = entity.getClass().getCanonicalName();
                entitiesWithCount.increment(canonicalName);
                if (entity instanceof EntityPlayer) {
                    entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerPlayer);
                } else {
                    entitiesWithCost.increment(canonicalName, BuilderConfiguration.builderRfPerEntity);
                }
            }

            RFToolsMessages.INSTANCE.sendTo(new PacketChamberInfoReady(blocks, costs, entitiesWithCount, entitiesWithCost), ctx.getServerHandler().playerEntity);
        }

    }
}