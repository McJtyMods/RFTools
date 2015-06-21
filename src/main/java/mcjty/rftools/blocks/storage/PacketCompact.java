package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.container.GenericContainer;
import mcjty.container.InventoryHelper;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
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
        if (!tagCompound.hasKey("childDamage")) {
            // Should not be possible. Just for safety.
            return null;
        }

        int moduleDamage = tagCompound.getInteger("childDamage");
        if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
            int id = tagCompound.getInteger("id");
            RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(playerEntity.worldObj, id);
            if (remoteStorage != null) {
                remoteStorage.compact(id);
                remoteStorage.markDirty();
            } else {
                RFTools.message(playerEntity, EnumChatFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
            }
        } else {
            GenericContainer genericContainer = (GenericContainer) playerEntity.openContainer;
            IInventory inventory = genericContainer.getInventory(ModularStorageItemContainer.CONTAINER_INVENTORY);
            ModularStorageItemInventory modularStorageItemInventory = (ModularStorageItemInventory) inventory;
            InventoryHelper.compactStacks(modularStorageItemInventory.getStacks(), 0, inventory.getSizeInventory());
            modularStorageItemInventory.markDirty();
        }

        return null;
    }

}
