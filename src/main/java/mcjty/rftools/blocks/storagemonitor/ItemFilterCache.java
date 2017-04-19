package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.tools.ItemStackList;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ItemFilterCache {
    private boolean matchDamage = true;
    private boolean oredictMode = false;
    private boolean blacklistMode = true;
    private boolean nbtMode = false;
    private ItemStackList stacks;
    private Set<Integer> oredictMatches = new HashSet<>();

    public ItemFilterCache(boolean matchDamage, boolean oredictMode, boolean blacklistMode, boolean nbtMode, @Nonnull ItemStackList stacks) {
        this.matchDamage = matchDamage;
        this.oredictMode = oredictMode;
        this.blacklistMode = blacklistMode;
        this.nbtMode = nbtMode;
        this.stacks = stacks;
        for (ItemStack s : stacks) {
            for (int id : OreDictionary.getOreIDs(s)) {
                oredictMatches.add(id);
            }
        }
    }

    public boolean match(ItemStack stack) {
        if (ItemStackTools.isValid(stack)) {
            boolean match = false;

            if (oredictMode) {
                int[] oreIDs = OreDictionary.getOreIDs(stack);
                if (oreIDs.length == 0) {
                    match = itemMatches(stack);
                } else {
                    for (int id : oreIDs) {
                        if (oredictMatches.contains(id)) {
                            match = true;
                            break;
                        }
                    }
                }
            } else {
                match = itemMatches(stack);
            }
            return match != blacklistMode;
        }
        return false;
    }

    private boolean itemMatches(ItemStack stack) {
        if (stacks != null) {
            for (ItemStack itemStack : stacks) {
                if (matchDamage && itemStack.getMetadata() != stack.getMetadata()) {
                    continue;
                }
                if (nbtMode && !ItemStack.areItemStackTagsEqual(itemStack, stack)) {
                    continue;
                }
                if (itemStack.getItem().equals(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }
}