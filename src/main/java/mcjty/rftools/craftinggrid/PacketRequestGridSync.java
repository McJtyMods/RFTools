package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.monitor.PacketAdjacentBlocksReady;
import mcjty.rftools.blocks.monitor.RFMonitorBlockTileEntity;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestGridSync implements IMessage {

    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            pos = NetworkTools.readPos(buf);
        } else {
            pos = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            NetworkTools.writePos(buf, pos);
        } else {
            buf.writeBoolean(false);
        }
    }

    public PacketRequestGridSync() {
    }

    public PacketRequestGridSync(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketRequestGridSync, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestGridSync message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestGridSync message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            CraftingGridProvider provider = null;
            if (message.pos == null) {
                // Handle tablet version
                ItemStack mainhand = player.getHeldItemMainhand();
                if (mainhand != null && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                    if (player.openContainer instanceof ModularStorageItemContainer) {
                        ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                        provider = storageItemContainer.getCraftingGridProvider();
                    }
                }
            } else {
                TileEntity te = world.getTileEntity(message.pos);
                if (te instanceof CraftingGridProvider) {
                    provider = ((CraftingGridProvider) te);
                }
            }
            if (provider != null) {
                RFToolsMessages.INSTANCE.sendTo(new PacketGridToClient(message.pos, provider.getCraftingGrid()), player);
            }
        }
    }
}
