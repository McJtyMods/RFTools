package mcjty.rftools.craftinggrid;

import mcjty.lib.compat.CompatInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingGridInventory implements CompatInventory {

    public static int SLOT_GHOSTOUTPUT = 0;
    public static int SLOT_GHOSTINPUT = 1;

    public static int GRID_WIDTH = 66;
    public static int GRID_HEIGHT = 208;
    public static int GRID_XOFFSET = -GRID_WIDTH -2+7;
    public static int GRID_YOFFSET = 127;

    private List<ItemStack> stacks = new ArrayList<>(10);



    public ItemStack getResult() {
        return stacks.get(SLOT_GHOSTOUTPUT);
    }

    public ItemStack[] getIngredients() {
        ItemStack[] ing = new ItemStack[9];
        // @todo @@@@@@@@@@@@@@@@@@
        System.arraycopy(stacks, SLOT_GHOSTINPUT, ing, 0, ing.length);
        return ing;
    }

    @Override
    public int getSizeInventory() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(stacks, index, count);
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(stacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "grid";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("grid");
    }
}
