package mcjty.rftools.compat.wheelsupport;

import mcjty.intwheel.api.IWheelAction;
import mcjty.intwheel.api.WheelActionElement;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.blocks.storage.ModularStorageItemInventory;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storage.RemoteStorageItemInventory;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.servernet.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static mcjty.rftools.items.storage.StorageModuleTabletItem.META_FOR_SCANNER;

public class FindBlockAction implements IWheelAction {

    public static final String ACTION_FINDBLOCK = "rftools.findblock";

    @Override
    public String getId() {
        return ACTION_FINDBLOCK;
    }

    @Override
    public WheelActionElement createElement() {
        return new WheelActionElement(ACTION_FINDBLOCK).description("Find the block you look at out of storage", null).texture("rftools:textures/gui/wheel_actions.png", 96, 0, 96, 0 + 32, 128, 128);
    }

    @Override
    public boolean performClient(PlayerEntity player, World world, @Nullable BlockPos pos, boolean extended) {
        return FindBlockClient.pickBlockClient(world, pos, player);
    }

    private boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }


    @Override
    public void performServer(PlayerEntity player, World world, @Nullable BlockPos pos, boolean extended) {
        if (pos != null) {
            // If we come here we know that on client side we couldn't find a suitable block in the players inventory.


            List<ItemStack> inventory = Collections.unmodifiableList(player.inventory.mainInventory);
            BlockState state = world.getBlockState(pos);
            ItemStack result = state.getBlock().getItem(world, pos, state);
            if (result == null || result.isEmpty()) {
                return;
            }

            ItemStack storage = ItemStack.EMPTY;
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    if (stack.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                        // Found!
                        storage = stack;
                        break;
                    }
                }
            }
            if (storage.isEmpty()) {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No storage tablet in inventory!"), false);
                return;
            }

            CompoundNBT tagCompound = storage.getTag();
            if (tagCompound == null || !tagCompound.hasKey("childDamage")) {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No storage module in tablet!"), false);
                return;
            }

            int firstEmptyStack = player.inventory.getFirstEmptyStack();
            if (firstEmptyStack < 0) {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No room in inventory for block!"), false);
                return;
            }

            int moduleDamage = tagCompound.getInteger("childDamage");
            ItemStack extracted = ItemStack.EMPTY;

            if (moduleDamage == META_FOR_SCANNER) {
                if (tagCompound.hasKey("monitorx")) {
                    int monitordim = tagCompound.getInteger("monitordim");
                    int monitorx = tagCompound.getInteger("monitorx");
                    int monitory = tagCompound.getInteger("monitory");
                    int monitorz = tagCompound.getInteger("monitorz");
                    BlockPos mpos = new BlockPos(monitorx, monitory, monitorz);
                    ServerWorld w = DimensionManager.getWorld(monitordim);
                    if (w == null || !WorldTools.chunkLoaded(w, mpos)) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Storage scanner is out of range!"), false);
                    } else {
                        TileEntity te = w.getTileEntity(mpos);
                        if (te instanceof StorageScannerTileEntity) {
                            StorageScannerTileEntity scanner = (StorageScannerTileEntity) te;
                            extracted = scanner.requestItem(result, result.getMaxStackSize(), true, false);
                            if (extracted.isEmpty()) {
                                extracted = scanner.requestItem(result, result.getMaxStackSize(), true, true);
                            }
                        }
                    }
                } else {
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Storage module is not linked to a storage scanner!"), false);
                }
            } else if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
                if (!tagCompound.hasKey("id")) {
                    Logging.message(player, TextFormatting.YELLOW + "This remote storage module is not linked!");
                } else {
                    RemoteStorageItemInventory storageInv = new RemoteStorageItemInventory(player, storage);
                    for (int i = 0 ; i < storageInv.getSizeInventory() ; i++) {
                        ItemStack s = storageInv.getStackInSlot(i);
                        if (!s.isEmpty() && stackEqualExact(result, s)) {
                            extracted = s;
                            storageInv.setInventorySlotContents(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            } else {
                ModularStorageItemInventory storageInv = new ModularStorageItemInventory(player, storage);
                for (int i = 0 ; i < storageInv.getSizeInventory() ; i++) {
                    ItemStack s = storageInv.getStackInSlot(i);
                    if (!s.isEmpty() && stackEqualExact(result, s)) {
                        extracted = s;
                        storageInv.setInventorySlotContents(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }

            if (!extracted.isEmpty()) {
                int currentItem = player.inventory.currentItem;
                if (currentItem == firstEmptyStack) {
                    player.inventory.setInventorySlotContents(currentItem, extracted);
                } else {
                    player.inventory.setInventorySlotContents(firstEmptyStack, inventory.get(currentItem));
                    player.inventory.setInventorySlotContents(currentItem, extracted);
                }

                player.openContainer.detectAndSendChanges();
            }
        }
    }
}
