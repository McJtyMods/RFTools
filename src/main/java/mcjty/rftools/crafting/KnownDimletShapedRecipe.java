package mcjty.rftools.crafting;

import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class KnownDimletShapedRecipe extends ShapedOreRecipe {
    private DimletKey destDimletKey;

    public KnownDimletShapedRecipe(DimletKey destDimletKey, Object... items) {
        super(new ItemStack(DimletSetup.knownDimlet), items);
        this.destDimletKey = destDimletKey;
    }

    @Override
    public ItemStack getRecipeOutput() {
        ItemStack stack = super.getRecipeOutput().copy();
        KnownDimletConfiguration.setDimletKey(destDimletKey, stack);
        return stack;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack stack = super.getCraftingResult(inventoryCrafting);
        KnownDimletConfiguration.setDimletKey(destDimletKey, stack);
        return stack;
    }
}
