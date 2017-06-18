package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketClearGrid implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketClearGrid() {
    }

    public static class Handler implements IMessageHandler<PacketClearGrid, IMessage> {
        @Override
        public IMessage onMessage(PacketClearGrid message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketClearGrid message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                if (player.openContainer instanceof ModularStorageItemContainer) {
                    ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                    storageItemContainer.clearGrid();
                    mainhand.getTagCompound().removeTag("grid");
                } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                    RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                    storageItemContainer.clearGrid();
                    mainhand.getTagCompound().removeTag("grid");
                } else if (player.openContainer instanceof StorageScannerContainer) {
                    StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                    storageItemContainer.clearGrid();
                    mainhand.getTagCompound().removeTag("grid");
                }
            }

        }

    }
}
