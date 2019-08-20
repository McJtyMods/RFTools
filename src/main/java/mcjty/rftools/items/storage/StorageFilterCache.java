package mcjty.rftools.items.storage;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.blocks.storage.sorters.ModItemSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
    private ItemStackList stacks;
    private Set<Integer> oredictMatches = new HashSet<>();

    // Parameter is the filter item.
    StorageFilterCache(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            matchDamage = tagCompound.getBoolean("damageMode");
            oredictMode = tagCompound.getBoolean("oredictMode");
            nbtMode = tagCompound.getBoolean("nbtMode");
            modMode = tagCompound.getBoolean("modMode");
            blacklistMode = "Black".equals(tagCompound.getString("blacklistMode"));
            ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            int cnt = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                CompoundNBT CompoundNBT = bufferTagList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(CompoundNBT);
                if (!s.isEmpty()) {
                    cnt++;
                }
            }
            stacks = ItemStackList.create(cnt);
            cnt = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                CompoundNBT CompoundNBT = bufferTagList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(CompoundNBT);
                if (!s.isEmpty()) {
                    stacks.set(cnt++, s);
                    if (oredictMode) {
                        for (int id : OreDictionary.getOreIDs(s)) {
                            oredictMatches.add(id);
                        }
                    }
                }
            }
        } else {
            stacks = ItemStackList.EMPTY;
        }
    }

    public boolean match(ItemStack stack) {
        if (!stack.isEmpty()) {
            boolean match = false;
            String modName = "";
            if (modMode) {
                modName = ModItemSorter.getMod(stack);
            }

            if (oredictMode) {
                int[] oreIDs = OreDictionary.getOreIDs(stack);
                if (oreIDs.length == 0) {
                    match = itemMatches(stack, modName);
                } else {
                    for (int id : oreIDs) {
                        if (oredictMatches.contains(id)) {
                            match = true;
                            break;
                        }
                    }
                }
            } else {
                match = itemMatches(stack, modName);
            }
            return match != blacklistMode;
        }
        return false;
    }

    private boolean itemMatches(ItemStack stack, String modName) {
        if (stacks != null) {
            for (ItemStack itemStack : stacks) {
                if (matchDamage && itemStack.getMetadata() != stack.getMetadata()) {
                    continue;
                }
                if (nbtMode && !ItemStack.areItemStackTagsEqual(itemStack, stack)) {
                    continue;
                }
                if (modMode) {
                    if (modName.equals(ModItemSorter.getMod(itemStack))) {
                        return true;
                    }
                } else if (itemStack.getItem().equals(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }
}
