package mcjty.rftools.blocks.storage;

import mcjty.rftools.blocks.crafter.CraftingRecipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class StorageCraftingTools {

    static List<ItemStack> testAndConsumeCraftingItems(EntityPlayerMP player, CraftingRecipe craftingRecipe,
                                                       IInventory thisInventory, int thisInventoryOffset, boolean strictDamage) {
        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        Map<Pair<IInventory, Integer>,ItemStack> undo = new HashMap<>();
        List<ItemStack> result = new ArrayList<>();
        InventoryCrafting inventory = craftingRecipe.getInventory();

        for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                int count = stack.stackSize;
                count = findMatchingItems(workInventory, undo, i, stack, count, player.inventory, 0, strictDamage);
                if (count > 0) {
                    count = findMatchingItems(workInventory, undo, i, stack, count, thisInventory, thisInventoryOffset, strictDamage);
                }

                if (count > 0) {
                    // Couldn't find all items.
                    undo(player, undo);
                    return Collections.emptyList();
                }
            } else {
                workInventory.setInventorySlotContents(i, null);
            }
        }
        IRecipe recipe = craftingRecipe.getCachedRecipe(player.worldObj);
        if (!recipe.matches(workInventory, player.worldObj)) {
            result.clear();
            undo(player, undo);
            return result;
        }
        ItemStack stack = recipe.getCraftingResult(workInventory);
        if (stack != null) {
            result.add(stack);
            ItemStack[] remaining = recipe.getRemainingItems(workInventory);
            if (remaining != null) {
                for (ItemStack s : remaining) {
                    if (s != null) {
                        result.add(s);
                    }
                }
            }
        } else {
            result.clear();
            undo(player, undo);
        }
        return result;
    }

    private static boolean match(ItemStack target, ItemStack input, boolean strictDamage) {
        if (strictDamage) {
            return OreDictionary.itemMatches(target, input, false);
        } else {
            if ((input == null && target != null) || (input != null && target == null)) {
                return false;
            }
            return target.getItem() == input.getItem();

        }
    }

    private static int findMatchingItems(InventoryCrafting workInventory, Map<Pair<IInventory, Integer>, ItemStack> undo, int i, ItemStack stack, int count, IInventory inv, int startIndex, boolean strictDamage) {
        for (int slotIdx = startIndex; slotIdx < inv.getSizeInventory() ; slotIdx++) {
            ItemStack input = inv.getStackInSlot(slotIdx);
            if (input != null) {
                if (match(stack, input, strictDamage)) {
                    workInventory.setInventorySlotContents(i, input.copy());
                    int ss = count;
                    if (input.stackSize - ss < 0) {
                        ss = input.stackSize;
                    }
                    count -= ss;
                    Pair<IInventory, Integer> key = Pair.of(inv, slotIdx);
                    if (!undo.containsKey(key)) {
                        undo.put(key, input.copy());
                    }
                    input.splitStack(ss);        // This consumes the items
                    if (input.stackSize == 0) {
                        inv.setInventorySlotContents(slotIdx, null);
                    }
                }
            }
            if (count == 0) {
                break;
            }
        }
        return count;
    }

    private static void undo(EntityPlayerMP player, Map<Pair<IInventory, Integer>, ItemStack> undo) {
        for (Map.Entry<Pair<IInventory, Integer>, ItemStack> entry : undo.entrySet()) {
            IInventory inv = entry.getKey().getLeft();
            Integer index = entry.getKey().getRight();
            inv.setInventorySlotContents(index, entry.getValue());
            player.openContainer.detectAndSendChanges();
        }
    }

    public static void craftItems(EntityPlayerMP player, int n, CraftingRecipe craftingRecipe, IInventory thisInventory, int thisOffset) {
        IRecipe recipe = craftingRecipe.getCachedRecipe(player.worldObj);
        if (recipe == null) {
            // @todo give error?
            return;
        }

        if (craftingRecipe.getResult() != null && craftingRecipe.getResult().stackSize > 0) {
            if (n == -1) {
                n = craftingRecipe.getResult().getMaxStackSize();
            }

            int remainder = n % craftingRecipe.getResult().stackSize;
            n /= craftingRecipe.getResult().stackSize;
            if (remainder != 0) {
                n++;
            }
            if (n * craftingRecipe.getResult().stackSize > craftingRecipe.getResult().getMaxStackSize()) {
                n--;
            }

            for (int i = 0 ; i < n ; i++) {
                List<ItemStack> result = testAndConsumeCraftingItems(player, craftingRecipe, thisInventory, thisOffset, true);
                if (result.isEmpty()) {
                    result = testAndConsumeCraftingItems(player, craftingRecipe, thisInventory, thisOffset, false);
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
}
