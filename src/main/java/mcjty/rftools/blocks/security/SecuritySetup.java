package mcjty.rftools.blocks.security;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.rftools.RFTools;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class SecuritySetup {

    public static OrphaningCardItem orphaningCardItem;

    public static void setupBlocks() {
    }

    public static void setupItems() {
        orphaningCardItem = new OrphaningCardItem();
        orphaningCardItem.setUnlocalizedName("OrphaningCard");
        orphaningCardItem.setCreativeTab(RFTools.tabRfTools);
        orphaningCardItem.setTextureName(RFTools.MODID + ":orphaningCardItem");
        GameRegistry.registerItem(orphaningCardItem, "orphaningCardItem");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(orphaningCardItem), " b ", "rir", " p ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.book, 'p', Items.paper);
    }
}
