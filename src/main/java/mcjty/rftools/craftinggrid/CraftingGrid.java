package mcjty.rftools.craftinggrid;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.crafter.CraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class CraftingGrid {

    private CraftingGridInventory craftingGridInventory = new CraftingGridInventory();
    private CraftingRecipe recipes[] = new CraftingRecipe[6];

    public CraftingGrid() {
        for (int i = 0 ; i < 6 ; i++) {
            recipes[i] = new CraftingRecipe();
        }
    }

    public CraftingGridInventory getCraftingGridInventory() {
        return craftingGridInventory;
    }

    public CraftingRecipe getRecipe(int index) {
        return recipes[index];
    }

    public CraftingRecipe getActiveRecipe() {
        CraftingRecipe recipe = new CraftingRecipe();
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
        return recipe;
    }

    public void setRecipe(int index, ItemStack[] stacks) {
        CraftingRecipe recipe = recipes[index];
        recipe.setResult(stacks[0]);
        for (int i = 0 ; i < 9 ; i++) {
            recipe.getInventory().setInventorySlotContents(i, stacks[i+1]);
        }
    }

    public void storeRecipe(int index) {
        CraftingRecipe recipe = getRecipe(index);
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
    }

    public void selectRecipe(int index) {
        CraftingRecipe recipe = getRecipe(index);
        craftingGridInventory.setInventorySlotContents(CraftingGridInventory.SLOT_GHOSTOUTPUT, recipe.getResult());
        for (int i = 0 ; i < 9 ; i++) {
            craftingGridInventory.setInventorySlotContents(i+CraftingGridInventory.SLOT_GHOSTINPUT, recipe.getInventory().getStackInSlot(i));
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < craftingGridInventory.getSizeInventory() ; i++) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            ItemStack stack = craftingGridInventory.getStackInSlot(i);
            if (ItemStackTools.isValid(stack)) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("grid", bufferTagList);

        NBTTagList recipeTagList = new NBTTagList();
        for (CraftingRecipe recipe : recipes) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            recipe.writeToNBT(nbtTagCompound);
            recipeTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("recipes", recipeTagList);

        return tagCompound;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            return;
        }
        NBTTagList bufferTagList = tagCompound.getTagList("grid", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < craftingGridInventory.getSizeInventory() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            craftingGridInventory.setInventorySlotContents(i, ItemStackTools.loadFromNBT(nbtTagCompound));
        }

        NBTTagList recipeTagList = tagCompound.getTagList("recipes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.tagCount() ; i++) {
            recipes[i] = new CraftingRecipe();
            NBTTagCompound nbtTagCompound = recipeTagList.getCompoundTagAt(i);
            recipes[i].readFromNBT(nbtTagCompound);
        }
    }
}
