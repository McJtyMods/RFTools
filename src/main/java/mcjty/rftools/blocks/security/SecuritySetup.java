package mcjty.rftools.blocks.security;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class SecuritySetup {
    public static SecurityManagerBlock securityManagerBlock;

    public static SecurityCardItem securityCardItem;
    public static OrphaningCardItem orphaningCardItem;

    public static void setupBlocks() {
        securityManagerBlock = new SecurityManagerBlock();
        GameRegistry.registerBlock(securityManagerBlock, GenericItemBlock.class, "securityManagerBlock");
        GameRegistry.registerTileEntity(SecurityManagerTileEntity.class, "SecurityManagerTileEntity");
    }

    public static void setupItems() {
        orphaningCardItem = new OrphaningCardItem();
        orphaningCardItem.setUnlocalizedName("OrphaningCard");
        orphaningCardItem.setCreativeTab(RFTools.tabRfTools);
        orphaningCardItem.setTextureName(RFTools.MODID + ":orphaningCardItem");
        GameRegistry.registerItem(orphaningCardItem, "orphaningCardItem");

        securityCardItem = new SecurityCardItem();
        securityCardItem.setUnlocalizedName("SecurityCard");
        securityCardItem.setCreativeTab(RFTools.tabRfTools);
        securityCardItem.setTextureName(RFTools.MODID + ":securityCardItem");
        GameRegistry.registerItem(securityCardItem, "securityCardItem");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(orphaningCardItem), " b ", "rir", " p ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.book, 'p', Items.paper);
        GameRegistry.addRecipe(new ItemStack(securityCardItem), " f ", "rir", " p ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'f', Items.flint, 'p', Items.paper);
        GameRegistry.addRecipe(new ItemStack(securityManagerBlock), "rfr", "fMf", "rcr", 'M', ModBlocks.machineFrame, 'r', Items.redstone, 'f', Items.flint,
                'c', Blocks.chest);
    }
}
