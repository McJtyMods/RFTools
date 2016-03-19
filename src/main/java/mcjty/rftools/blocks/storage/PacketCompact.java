package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.varia.Logging;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCompact implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketCompact() {
    }

    public static class Handler implements IMessageHandler<PacketCompact, IMessage> {
        @Override
        public IMessage onMessage(PacketCompact message, MessageContext ctx) {
            MinecraftServer.getServer().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketCompact message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = playerEntity.getHeldItem();
            if (heldItem == null) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            if (!tagCompound.hasKey("childDamage")) {
                // Should not be possible. Just for safety.
                return;
            }

            int moduleDamage = tagCompound.getInteger("childDamage");
            if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
                int id = tagCompound.getInteger("id");
                RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(playerEntity.worldObj, id);
                if (remoteStorage != null) {
                    remoteStorage.compact(id);
                    remoteStorage.markDirty();
                } else {
                    Logging.message(playerEntity, TextFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
                }
            } else {
                GenericContainer genericContainer = (GenericContainer) playerEntity.openContainer;
                IInventory inventory = genericContainer.getInventory(ModularStorageItemContainer.CONTAINER_INVENTORY);
                ModularStorageItemInventory modularStorageItemInventory = (ModularStorageItemInventory) inventory;
                InventoryHelper.compactStacks(modularStorageItemInventory.getStacks(), 0, inventory.getSizeInventory());
                modularStorageItemInventory.markDirty();
            }
        }

    }
}
