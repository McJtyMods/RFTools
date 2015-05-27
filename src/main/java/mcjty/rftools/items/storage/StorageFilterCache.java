package mcjty.rftools.items.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

public class StorageFilterCache {
    private boolean ignoreDamage = true;
    private boolean oredictMode = false;
    private boolean blacklistMode = true;
    private ItemStack stacks[] = new ItemStack[StorageFilterContainer.FILTER_SLOTS];

    // Parameter is the filter item.
    StorageFilterCache(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            ignoreDamage = tagCompound.getBoolean("damageMode");
            oredictMode = tagCompound.getBoolean("oredictMode");
            blacklistMode = "Black".equals(tagCompound.getString("blacklistMode"));
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                stacks[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
            }
        }
    }

    public boolean match(ItemStack stack) {
        if (stack != null) {
            boolean match = false;
            for (ItemStack itemStack : stacks) {
                if (!ignoreDamage) {
                    if (itemStack.isItemEqual(stack)) {
                        match = true;
                        break;
                    }
                } else {
                    if (itemStack.getItem().equals(stack.getItem())) {
                        match = true;
                        break;
                    }
                }
                if (oredictMode) {
                    if (OreDictionary.itemMatches(itemStack, stack, false)) {
                        match = true;
                        break;
                    }
                }
            }
            return match != blacklistMode;
        }
        return true;
    }
}
