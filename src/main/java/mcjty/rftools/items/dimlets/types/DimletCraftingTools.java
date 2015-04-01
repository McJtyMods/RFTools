package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.items.dimlets.DimletEntry;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
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
}
