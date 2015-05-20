package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketCompact implements IMessage, IMessageHandler<PacketCompact, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketCompact() {
    }

    @Override
    public IMessage onMessage(PacketCompact message, MessageContext ctx) {
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
            remoteStorage.compact(id);
            remoteStorage.markDirty();
        } else {
            RFTools.message(playerEntity, EnumChatFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
        }

        return null;
    }

}
