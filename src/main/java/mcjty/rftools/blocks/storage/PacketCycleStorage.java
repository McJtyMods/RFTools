package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.Logging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCycleStorage implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketCycleStorage() {
    }

    public static class Handler implements IMessageHandler<PacketCycleStorage, IMessage> {
        @Override
        public IMessage onMessage(PacketCycleStorage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketCycleStorage message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            int id = tagCompound.getInteger("id");
            RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(playerEntity.worldObj, id);
            if (remoteStorage != null) {
                id = remoteStorage.cycle(id);
                tagCompound.setInteger("id", id);
                int si = remoteStorage.findRemoteIndex(id);
                if (si != -1) {
                    int maxStacks = remoteStorage.getMaxStacks(si);
                    tagCompound.setInteger("maxSize", maxStacks);
                }
                remoteStorage.markDirty();
            } else {
                Logging.message(playerEntity, TextFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
            }
        }

    }

}
