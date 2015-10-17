package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.Logging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketCycleStorage implements IMessage, IMessageHandler<PacketCycleStorage, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketCycleStorage() {
    }

    @Override
    public IMessage onMessage(PacketCycleStorage message, MessageContext ctx) {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        ItemStack heldItem = playerEntity.getHeldItem();
        if (heldItem == null) {
            return null;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        int id = tagCompound.getInteger("id");
        RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(playerEntity.worldObj, id);
        if (remoteStorage != null) {
            id = remoteStorage.cycle(id);
            tagCompound.setInteger("id", id);
            remoteStorage.markDirty();
        } else {
            Logging.message(playerEntity, EnumChatFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
        }

        return null;
    }

}
