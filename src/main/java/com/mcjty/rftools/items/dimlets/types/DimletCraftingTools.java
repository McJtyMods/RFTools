package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class DimletCraftingTools {
    public static boolean matchDimletRecipe(DimletKey key, ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy) {
        DimletEntry dimletEntry = KnownDimletConfiguration.getEntry(key);
        int rarity = dimletEntry.getRarity();
        if (stackController.getItemDamage() != rarity) {
            return false;
        }
        int level = calculateItemLevelFromRarity(rarity);
        if (stackMemory.getItemDamage() != level) {
            return false;
        }
        if (stackEnergy.getItemDamage() != level) {
            return false;
        }

        return true;
    }

    public static int calculateItemLevelFromRarity(int rarity) {
        int level;
        if (rarity <= 1) {
            level = 0;
        } else if (rarity <= 3) {
            level = 1;
        } else {
            level = 2;
        }
        return level;
    }

    public static Block getBlock(ItemStack stackEssence) {
        if (stackEssence.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stackEssence.getItem()).field_150939_a;
        } else {
            return null;
        }
    }
}
