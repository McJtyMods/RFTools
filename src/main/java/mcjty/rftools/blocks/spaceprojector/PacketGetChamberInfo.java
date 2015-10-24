package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.BlockMeta;
import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.Counter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

public class PacketGetChamberInfo implements IMessage, IMessageHandler<PacketGetChamberInfo, PacketChamberInfoReady> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetChamberInfo() {
    }

    @Override
    public PacketChamberInfoReady onMessage(PacketGetChamberInfo message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        ItemStack cardItem = player.getHeldItem();
        if (cardItem == null || cardItem.getTagCompound() == null) {
            return null;
        }

        int channel = cardItem.getTagCompound().getInteger("channel");
        if (channel == -1) {
            return null;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(player.worldObj);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return null;
        }

        int dimension = chamberChannel.getDimension();
        World world = DimensionManager.getWorld(dimension);
        if (world == null) {
            return null;
        }

        Counter<BlockMeta> blocks = new Counter<BlockMeta>();
        Counter<BlockMeta> costs = new Counter<BlockMeta>();
        Coordinate minCorner = chamberChannel.getMinCorner();
        Coordinate maxCorner = chamberChannel.getMaxCorner();
        for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
            for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (!BuilderTileEntity.isEmpty(block)) {
                        int meta = world.getBlockMetadata(x, y, z);
                        BlockMeta bm = new BlockMeta(block, meta);
                        blocks.increment(bm);

                        TileEntity te = world.getTileEntity(x, y, z);
                        SpaceProjectorSetup.BlockInformation info = BuilderTileEntity.getBlockInformation(world, x, y, z, block, te);
                        if (info.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                            costs.put(bm, -1);
                        } else {
                            costs.increment(bm, (int) (SpaceProjectorConfiguration.builderRfPerOperation * info.getCostFactor()));
                        }
                    }
                }
            }
        }

        Counter<String> entitiesWithCount = new Counter<String>();
        Counter<String> entitiesWithCost = new Counter<String>();
        List entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(
                minCorner.getX(), minCorner.getY(), minCorner.getZ(), maxCorner.getX()+1, maxCorner.getY()+1, maxCorner.getZ()+1));
        for (Object o : entities) {
            Entity entity = (Entity) o;
            String canonicalName = entity.getClass().getCanonicalName();
            entitiesWithCount.increment(canonicalName);
            if (entity instanceof EntityPlayer) {
                entitiesWithCost.increment(canonicalName, SpaceProjectorConfiguration.builderRfPerPlayer);
            } else {
                entitiesWithCost.increment(canonicalName, SpaceProjectorConfiguration.builderRfPerEntity);
            }
        }

        return new PacketChamberInfoReady(blocks, costs, entitiesWithCount, entitiesWithCost);
    }
}