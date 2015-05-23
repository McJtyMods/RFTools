package mcjty.rftools.blocks.storage;

import mcjty.container.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class RemoteStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int MAXSIZE_STORAGE = 300;

    private EntityPlayer entityPlayer;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, 0, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
        }
    };

    public RemoteStorageItemContainer(EntityPlayer player) {
        super(factory);
        this.entityPlayer = player;
        if (isServer()) {
            int maxStacks = 0;
            RemoteStorageTileEntity remoteStorageTileEntity = getRemoteStorage();
            if (remoteStorageTileEntity != null) {
                int si = remoteStorageTileEntity.findRemoteIndex(getStorageID());
                if (si != -1) {
                    maxStacks = remoteStorageTileEntity.getMaxStacks(si);
                }
            }
            ItemStack stack = player.getHeldItem();
            stack.getTagCompound().setInteger("maxSize", maxStacks);
        }

        addInventory(CONTAINER_INVENTORY, new RemoteStorageItemInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    private RemoteStorageTileEntity getRemoteStorage() {
        return RemoteStorageIdRegistry.getRemoteStorage(entityPlayer.worldObj, getStorageID());
    }

    private int getStorageID() {
        // We assume the item is right here
        return entityPlayer.getHeldItem().getTagCompound().getInteger("id");
    }

    private boolean isServer() {
        return !entityPlayer.worldObj.isRemote;
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        if (isServer()) {
                            RemoteStorageTileEntity storage = getRemoteStorage();
                            int si = -1;
                            if (storage != null) {
                                si = storage.findRemoteIndex(getStorageID());
                            }
                            if (si != -1) {
                                entityPlayer.getHeldItem().getTagCompound().setInteger("maxSize", storage.getMaxStacks(si));
                                return storage.isItemValidForSlot(getSlotIndex(), stack);
                            } else {
                                entityPlayer.getHeldItem().getTagCompound().setInteger("maxSize", 0);
                                return false;
                            }
                        } else {
                            int maxSize = entityPlayer.getHeldItem().getTagCompound().getInteger("maxSize");
                            if (getSlotIndex() >= maxSize) {
                                return false;
                            }
                            return super.isItemValid(stack);
                        }
                    }
                };
            }
            addSlotToContainer(slot);
        }
    }

}
