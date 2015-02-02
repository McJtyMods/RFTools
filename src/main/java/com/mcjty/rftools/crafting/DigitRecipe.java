package com.mcjty.rftools.crafting;

import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class DigitRecipe implements IRecipe {
    private final DimletKey destDimletKey;
    private final DimletKey sourceDimletKey;

    public DigitRecipe(String source, String dest) {
        this.destDimletKey = new DimletKey(DimletType.DIMLET_DIGIT, dest);
        this.sourceDimletKey = new DimletKey(DimletType.DIMLET_DIGIT, source);
    }

    @Override
    public ItemStack getRecipeOutput() {
        Integer id = KnownDimletConfiguration.dimletToID.get(destDimletKey);
        if (id == null) {
            id = 0;
        }
        return new ItemStack(ModItems.knownDimlet, 1, id);
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        Integer id = KnownDimletConfiguration.dimletToID.get(sourceDimletKey);
        if (id == null) {
            return false;
        }
        ItemStack source = new ItemStack(ModItems.knownDimlet, 1, id);
        int cnt = 0;
        for (int i = 0 ; i < inventoryCrafting.getSizeInventory() ; i++) {
            ItemStack slot = inventoryCrafting.getStackInSlot(i);
            if (slot != null) {
                if (slot.isItemEqual(source)) {
                    cnt++;
                } else {
                    return false;
                }
            }
        }
        return cnt == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        return getRecipeOutput();
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }
}
