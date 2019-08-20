package mcjty.rftools.craftinggrid;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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

    public CompoundNBT writeToNBT() {
        CompoundNBT tagCompound = new CompoundNBT();

        ListNBT bufferTagList = new ListNBT();
        for (int i = 0 ; i < craftingGridInventory.getSizeInventory() ; i++) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            ItemStack stack = craftingGridInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack.writeToNBT(CompoundNBT);
            }
            bufferTagList.appendTag(CompoundNBT);
        }
        tagCompound.setTag("grid", bufferTagList);

        ListNBT recipeTagList = new ListNBT();
        for (CraftingRecipe recipe : recipes) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            recipe.writeToNBT(CompoundNBT);
            recipeTagList.appendTag(CompoundNBT);
        }
        tagCompound.setTag("recipes", recipeTagList);

        return tagCompound;
    }

    public void readFromNBT(CompoundNBT tagCompound) {
        if (tagCompound == null) {
            return;
        }
        ListNBT bufferTagList = tagCompound.getTagList("grid", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < craftingGridInventory.getSizeInventory() ; i++) {
            CompoundNBT CompoundNBT = bufferTagList.getCompoundTagAt(i);
            craftingGridInventory.setInventorySlotContents(i, new ItemStack(CompoundNBT));
        }

        ListNBT recipeTagList = tagCompound.getTagList("recipes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.tagCount() ; i++) {
            recipes[i] = new CraftingRecipe();
            CompoundNBT CompoundNBT = recipeTagList.getCompoundTagAt(i);
            recipes[i].readFromNBT(CompoundNBT);
        }
    }
}
