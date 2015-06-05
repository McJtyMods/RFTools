package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SpaceProjectorSetup {
    public static ProxyBlock proxyBlock;
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static SpaceProjectorBlock spaceProjectorBlock;
    public static BuilderBlock builderBlock;

    public static SpaceChamberCardItem spaceChamberCardItem;

    public static void setupBlocks() {
        proxyBlock = new ProxyBlock();
        GameRegistry.registerBlock(proxyBlock, "proxyBlock");
        GameRegistry.registerTileEntity(ProxyBlockTileEntity.class, "ProxyBlockTileEntity");

        spaceChamberBlock = new SpaceChamberBlock();
        GameRegistry.registerBlock(spaceChamberBlock, "spaceChamberBlock");

        spaceChamberControllerBlock = new SpaceChamberControllerBlock();
        GameRegistry.registerBlock(spaceChamberControllerBlock, GenericItemBlock.class, "spaceChamberControllerBlock");
        GameRegistry.registerTileEntity(SpaceChamberControllerTileEntity.class, "SpaceChamberControllerTileEntity");

        spaceProjectorBlock = new SpaceProjectorBlock();
        GameRegistry.registerBlock(spaceProjectorBlock, GenericItemBlock.class, "spaceProjectorBlock");
        GameRegistry.registerTileEntity(SpaceProjectorTileEntity.class, "SpaceProjectorTileEntity");

        builderBlock = new BuilderBlock();
        GameRegistry.registerBlock(builderBlock, GenericItemBlock.class, "builderBlock");
        GameRegistry.registerTileEntity(BuilderTileEntity.class, "BuilderTileEntity");
    }

    public static void setupItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        spaceChamberCardItem.setUnlocalizedName("SpaceChamberCard");
        spaceChamberCardItem.setCreativeTab(RFTools.tabRfTools);
        spaceChamberCardItem.setTextureName(RFTools.MODID + ":spaceChamberCardItem");
        GameRegistry.registerItem(spaceChamberCardItem, "spaceChamberCardItem");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(spaceChamberBlock), "lgl", "gMg", "lgl", 'M', ModBlocks.machineFrame, 'g', Blocks.glass, 'l', lapisStack);
        GameRegistry.addRecipe(new ItemStack(spaceChamberControllerBlock), " e ", "tMt", " e ", 'M', spaceChamberBlock, 't', redstoneTorch, 'e', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(builderBlock), "beb", "rMr", "brb", 'M', ModBlocks.machineFrame, 'e', Items.ender_pearl, 'r', Items.redstone, 'b', Blocks.brick_block);

        GameRegistry.addRecipe(new ItemStack(spaceChamberCardItem), " b ", "rir", " b ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.brick);
    }

}
