package mcjty.rftools.items.storage;

import mcjty.rftools.blocks.storage.sorters.ModItemSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashSet;
import java.util.Set;

public class StorageFilterCache {
    private boolean matchDamage = true;
    private boolean oredictMode = false;
    private boolean blacklistMode = true;
    private boolean nbtMode = false;
    private boolean modMode = false;
    private ItemStack stacks[];
    private Set<Integer> oredictMatches = new HashSet<Integer>();

    // Parameter is the filter item.
    StorageFilterCache(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            matchDamage = tagCompound.getBoolean("damageMode");
            oredictMode = tagCompound.getBoolean("oredictMode");
            nbtMode = tagCompound.getBoolean("nbtMode");
            modMode = tagCompound.getBoolean("modMode");
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
                    if (oredictMode) {
                        for (int id : OreDictionary.getOreIDs(s)) {
                            oredictMatches.add(id);
                        }
                    }
                }
            }
        } else {
            stacks = new ItemStack[0];
        }
    }

    public boolean match(ItemStack stack) {
        if (stack != null) {
            boolean match = false;
            String modName = "";
            if (modMode) {
                modName = ModItemSorter.getMod(stack);
            }

            if (oredictMode) {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (oredictMatches.contains(id)) {
                        match = true;
                        break;
                    }
                }
            } else if (stacks != null) {
                for (ItemStack itemStack : stacks) {
                    if (matchDamage && itemStack.getItemDamage() != stack.getItemDamage()) {
                        continue;
                    }
                    if (nbtMode && !ItemStack.areItemStackTagsEqual(itemStack, stack)) {
                        continue;
                    }
                    if (modMode) {
                        if (modName.equals(ModItemSorter.getMod(itemStack))) {
                            match = true;
                            break;
                        }
                    } else if (itemStack.getItem().equals(stack.getItem())) {
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
