package mcjty.rftools.blocks.storage;

import mcjty.lib.container.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class RemoteStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int MAXSIZE_STORAGE = 300;

    private EntityPlayer entityPlayer;
    private int tabletIndex;

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
            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            stack.getTagCompound().setInteger("maxSize", maxStacks);
        }

        addInventory(CONTAINER_INVENTORY, new RemoteStorageItemInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        tabletIndex = player.inventory.currentItem;
        generateSlots();
    }

    private RemoteStorageTileEntity getRemoteStorage() {
        return RemoteStorageIdRegistry.getRemoteStorage(entityPlayer.worldObj, getStorageID());
    }

    private int getStorageID() {
        // We assume the item is right here
        return entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("id");
    }

    private boolean isServer() {
        return !entityPlayer.worldObj.isRemote;
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                if (slotFactory.getIndex() == tabletIndex) {
                    slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                        @Override
                        public boolean canTakeStack(EntityPlayer player) {
                            // We don't want to take the stack from this slot.
                            return false;
                        }
                    };
                } else {
                    slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
                }
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV) {
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
                                entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setInteger("maxSize", storage.getMaxStacks(si));
                                return storage.isItemValidForSlot(getSlotIndex(), stack);
                            } else {
                                entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setInteger("maxSize", 0);
                                return false;
                            }
                        } else {
                            int maxSize = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("maxSize");
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
