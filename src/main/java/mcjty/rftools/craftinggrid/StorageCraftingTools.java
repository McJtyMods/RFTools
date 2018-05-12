package mcjty.rftools.craftinggrid;

import gnu.trove.set.hash.TIntHashSet;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageCraftingTools {

    @Nonnull
    private static int[] tryRecipe(EntityPlayer player, CraftingRecipe craftingRecipe, int n, IItemSource itemSource, boolean strictDamage) {
        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        InventoryCrafting inventory = craftingRecipe.getInventory();

        int[] missingCount = new int[10];
        TIntHashSet[] hashSets = new TIntHashSet[9];
        for (int i = 0 ; i < 10 ; i++) {
            if (i < 9) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    missingCount[i] = stack.getCount() * n;
                    hashSets[i] = new TIntHashSet(OreDictionary.getOreIDs(stack));
                } else {
                    missingCount[i] = 0;
                }
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            } else {
                missingCount[i] = 0;
            }
        }

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            int size = input.getCount();
            if (!input.isEmpty()) {
                for (int i = 0; i < 9; i++) {
                    if (missingCount[i] > 0) {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (match(stack, hashSets[i], input, strictDamage)) {
                            if (size > missingCount[i]) {
                                size -= missingCount[i];
                                missingCount[i] = 0;
                            } else {
                                missingCount[i] -= size;
                                size = 0;
                            }
                            workInventory.setInventorySlotContents(i, input.copy());
                        }
                    }
                }
            }
        }

        IRecipe recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (!recipe.matches(workInventory, player.getEntityWorld())) {
            missingCount[9] = 1;
        } else {
            missingCount[9] = 0;
        }

        if (missingCount[9] == 0) {
            for (int i = 0 ; i < 9 ; i++) {
                if (missingCount[i] > 0) {
                    missingCount[9] = 1;
                    break;
                }
            }
        }

        return missingCount;
    }

    private static List<ItemStack> testAndConsumeCraftingItems(EntityPlayer player, CraftingRecipe craftingRecipe,
                                                               IItemSource itemSource, boolean strictDamage) {
        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        List<Pair<IItemKey, ItemStack>> undo = new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();
        InventoryCrafting inventory = craftingRecipe.getInventory();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int count = stack.getCount();
                count = findMatchingItems(workInventory, undo, i, stack, count, itemSource, strictDamage);

                if (count > 0) {
                    // Couldn't find all items.
                    undo(player, itemSource, undo);
                    return Collections.emptyList();
                }
            } else {
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
        IRecipe recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (!recipe.matches(workInventory, player.getEntityWorld())) {
            result.clear();
            undo(player, itemSource, undo);
            return result;
        }
        ItemStack stack = recipe.getCraftingResult(workInventory);
        if (!stack.isEmpty()) {
            result.add(stack);
            List<ItemStack> remaining = recipe.getRemainingItems(workInventory);
            for (ItemStack s : remaining) {
                if (!s.isEmpty()) {
                    result.add(s);
                }
            }
        } else {
            result.clear();
            undo(player, itemSource, undo);
        }
        return result;
    }

    private static boolean match(@Nonnull ItemStack target, @Nonnull TIntHashSet targetIDs, @Nonnull ItemStack input, boolean strictDamage) {
        if (strictDamage) {
            return (target.getItem() == input.getItem() && ((target.getMetadata() == OreDictionary.WILDCARD_VALUE) || target.getMetadata() == input.getMetadata()));
        } else {
            if (target.getItem() == input.getItem()) {
                return true;
            }

            if (targetIDs.isEmpty()) {
                return false;
            }

            // Try OreDictionary
            int[] inputIDs = OreDictionary.getOreIDs(input);
            for (int id : inputIDs) {
                if (targetIDs.contains(id)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static int findMatchingItems(InventoryCrafting workInventory, List<Pair<IItemKey, ItemStack>> undo, int i,
                                         @Nonnull ItemStack stack,
                                         int count, IItemSource itemSource, boolean strictDamage) {
        TIntHashSet stackIDs = new TIntHashSet(OreDictionary.getOreIDs(stack));

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            if (!input.isEmpty()) {
                if (match(stack, stackIDs, input, strictDamage)) {
                    workInventory.setInventorySlotContents(i, input.copy());
                    int ss = count;
                    if (input.getCount() - ss < 0) {
                        ss = input.getCount();
                    }
                    count -= ss;
                    IItemKey key = pair.getKey();
                    ItemStack actuallyExtracted = itemSource.decrStackSize(key, ss);
                    undo.add(Pair.of(key, actuallyExtracted));
                }
            }
            if (count == 0) {
                break;
            }
        }
        return count;
    }

    private static void undo(EntityPlayer player, IItemSource itemSource, List<Pair<IItemKey, ItemStack>> undo) {
        for (Pair<IItemKey, ItemStack> pair : undo) {
            ItemStack stack = pair.getValue();
            if (!itemSource.insertStack(pair.getKey(), stack)) {
                // Insertion in original slot failed. Let's just try to insert it in any slot
                int amountLeft = itemSource.insertStackAnySlot(pair.getKey(), stack);
                if (amountLeft > 0) {
                    // We still have left-overs. Spawn them in the player inventory
                    ItemStack copy = stack.copy();
                    copy.setCount(amountLeft);
                    ItemHandlerHelper.giveItemToPlayer(player, copy);
                }
            }
        }
        player.openContainer.detectAndSendChanges();
    }

    public static void craftItems(EntityPlayer player, int n, CraftingRecipe craftingRecipe, IItemSource itemSource) {
        IRecipe recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (recipe == null) {
            // @todo give error?
            return;
        }

        ItemStack recipeResult = recipe.getRecipeOutput();
        if (!recipeResult.isEmpty() && recipeResult.getCount() > 0) {
            if (n == -1) {
                n = recipeResult.getMaxStackSize();
            }

            int remainder = n % recipeResult.getCount();
            n /= recipeResult.getCount();
            if (remainder != 0) {
                n++;
            }
            if (n * recipeResult.getCount() > recipeResult.getMaxStackSize()) {
                n--;
            }

            for (int i = 0; i < n; i++) {
                List<ItemStack> result = testAndConsumeCraftingItems(player, craftingRecipe, itemSource, true);
                if (result.isEmpty()) {
                    result = testAndConsumeCraftingItems(player, craftingRecipe, itemSource, false);
                    if (result.isEmpty()) {
                        return;
                    }
                }
                for (ItemStack stack : result) {
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        player.entityDropItem(stack, 1.05f);
                    }
                }
            }
        }
    }


    @Nonnull
    public static int[] testCraftItems(EntityPlayer player, int n, CraftingRecipe craftingRecipe, IItemSource itemSource) {
        IRecipe recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (recipe == null) {
            // @todo give error?
            return new int[0];
        }

        ItemStack recipeResult = recipe.getRecipeOutput();
        if (!recipeResult.isEmpty() && recipeResult.getCount() > 0) {
            if (n == -1) {
                n = recipeResult.getMaxStackSize();
            }

            int remainder = n % recipeResult.getCount();
            n /= recipeResult.getCount();
            if (remainder != 0) {
                n++;
            }
            if (n * recipeResult.getCount() > recipeResult.getMaxStackSize()) {
                n--;
            }

            // First we try the recipe with exact damage. If that works then that's perfect
            // already. Otherwise we try again with non-exact damage. If that turns out
            // not to work then we return the missing items from the exact damage crafting
            // test because that one has more information about what items are really
            // missing
            int[] result = tryRecipe(player, craftingRecipe, n, itemSource, true);
            for (int i = 0; i < 10; i++) {
                if (result[i] > 0) {
                    // Failed
                    int[] result2 = tryRecipe(player, craftingRecipe, n, itemSource, false);
                    if (result2[9] == 0) {
                        return result2;
                    } else {
                        return result;
                    }
                }
            }
            return result;
        }
        return new int[0];
    }

    public static void craftFromGrid(EntityPlayer player, int count, boolean test, BlockPos pos) {
        player.addStat(StatList.CRAFTING_TABLE_INTERACTION);
        int[] testResult = new int[0];
        if (pos == null) {
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                if (player.openContainer instanceof ModularStorageItemContainer) {
                    ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                    testResult = storageItemContainer.getCraftingGridProvider().craft(player, count, test);
                } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                    RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                    testResult = storageItemContainer.getCraftingGridProvider().craft(player, count, test);
                } else if (player.openContainer instanceof StorageScannerContainer) {
                    StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                    testResult = storageItemContainer.getStorageScannerTileEntity().craft(player, count, test);
                }
            }
        } else {
            TileEntity te = player.getEntityWorld().getTileEntity(pos);
            if (te instanceof CraftingGridProvider) {
                testResult = ((CraftingGridProvider) te).craft(player, count, test);
            }
        }
        if (testResult.length > 0) {
            RFToolsMessages.INSTANCE.sendTo(new PacketCraftTestResultToClient(testResult), (EntityPlayerMP) player);
        }
    }

    public static void requestGridSync(EntityPlayer player, BlockPos pos) {
        World world = player.getEntityWorld();
        CraftingGridProvider provider = null;
        if (pos == null) {
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
                if (player.openContainer instanceof ModularStorageItemContainer) {
                    ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                    provider = storageItemContainer.getCraftingGridProvider();
                } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                    RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                    provider = storageItemContainer.getCraftingGridProvider();
                } else if (player.openContainer instanceof StorageScannerContainer) {
                    StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                    provider = storageItemContainer.getStorageScannerTileEntity();
                }
            }
        } else {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof CraftingGridProvider) {
                provider = ((CraftingGridProvider) te);
            }
        }
        if (provider != null) {
            RFToolsMessages.INSTANCE.sendTo(new PacketGridToClient(pos, provider.getCraftingGrid()), (EntityPlayerMP) player);
        }
    }
}
