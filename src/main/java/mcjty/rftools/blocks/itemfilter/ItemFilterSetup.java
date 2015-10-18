package mcjty.rftools.blocks.itemfilter;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemFilterSetup {
    public static ItemFilterBlock itemFilterBlock;
    public static DimletFilterBlock dimletFilterBlock;

    public static void setupBlocks() {
        itemFilterBlock = new ItemFilterBlock();
        GameRegistry.registerBlock(itemFilterBlock, GenericItemBlock.class, "itemFilterBlock");
        GameRegistry.registerTileEntity(ItemFilterTileEntity.class, "ItemFilterTileEntity");

        dimletFilterBlock = new DimletFilterBlock();
        GameRegistry.registerBlock(dimletFilterBlock, GenericItemBlock.class, "dimletFilterBlock");
        GameRegistry.registerTileEntity(DimletFilterTileEntity.class, "DimletFilterTileEntity");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");

        GameRegistry.addRecipe(new ItemStack(itemFilterBlock), "pcp", "rMr", "pTp", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'p', Items.paper,
                'r', Items.redstone, 'c', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(dimletFilterBlock), " u ", "rMr", " r ", 'M', itemFilterBlock, 'u', DimletSetup.unknownDimlet,
                'r', Items.redstone);
    }
}
