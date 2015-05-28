package mcjty.rftools.items.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

public class StorageFilterCache {
    private boolean matchDamage = true;
    private boolean oredictMode = false;
    private boolean blacklistMode = true;
    private ItemStack stacks[];

    // Parameter is the filter item.
    StorageFilterCache(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            matchDamage = tagCompound.getBoolean("damageMode");
            oredictMode = tagCompound.getBoolean("oredictMode");
            blacklistMode = "Black".equals(tagCompound.getString("blacklistMode"));
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            int cnt = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                if (s != null && s.stackSize > 0) {
                    cnt++;
                }
            }
            stacks = new ItemStack[cnt];
            cnt = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                if (s != null && s.stackSize > 0) {
                    stacks[cnt++] = s;
                }
            }
        }
    }

    public boolean match(ItemStack stack) {
        if (stack != null) {
            boolean match = false;
            for (ItemStack itemStack : stacks) {
                if (matchDamage && itemStack.getItemDamage() != stack.getItemDamage()) {
                    continue;
                }
                if (oredictMode) {
                    if (OreDictionary.itemMatches(itemStack, stack, false)) {
                        match = true;
                        break;
                    }
                } else {
                    if (itemStack.getItem().equals(stack.getItem())) {
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
