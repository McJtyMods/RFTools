package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.varia.BlockMeta;
import mcjty.varia.Coordinate;
import mcjty.varia.Counter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

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
                    }
                }
            }
        }

        return new PacketChamberInfoReady(blocks);
    }
}