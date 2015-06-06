package mcjty.rftools.blocks.shield;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ShieldSetup {
    public static ShieldBlock shieldBlock;
    public static ShieldBlock shieldBlock2;
    public static ShieldBlock shieldBlock3;
    public static InvisibleShieldBlock invisibleShieldBlock;
    public static SolidShieldBlock solidShieldBlock;
    public static ShieldTemplateBlock shieldTemplateBlock;

    public static void setupBlocks() {
        shieldBlock = new ShieldBlock("shieldBlock", ShieldTileEntity.class);
        GameRegistry.registerBlock(shieldBlock, GenericItemBlock.class, "shieldBlock");
        GameRegistry.registerTileEntity(ShieldTileEntity.class, "ShieldTileEntity");

        shieldBlock2 = new ShieldBlock("shieldBlock2", ShieldTileEntity2.class);
        GameRegistry.registerBlock(shieldBlock2, GenericItemBlock.class, "shieldBlock2");
        GameRegistry.registerTileEntity(ShieldTileEntity2.class, "ShieldTileEntity2");

        shieldBlock3 = new ShieldBlock("shieldBlock3", ShieldTileEntity3.class);
        GameRegistry.registerBlock(shieldBlock3, GenericItemBlock.class, "shieldBlock3");
        GameRegistry.registerTileEntity(ShieldTileEntity3.class, "ShieldTileEntity3");

        invisibleShieldBlock = new InvisibleShieldBlock();
        GameRegistry.registerBlock(invisibleShieldBlock, "invisibleShieldBlock");

        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld) {
            solidShieldBlock = new SolidShieldBlock();
            GameRegistry.registerBlock(solidShieldBlock, "solidShieldBlock");
            GameRegistry.registerTileEntity(ShieldBlockTileEntity.class, "ShieldBlockTileEntity");
            shieldTemplateBlock = new ShieldTemplateBlock();
            GameRegistry.registerBlock(shieldTemplateBlock, "shieldTemplateBlock");
        }
    }

    public static void setupCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");

        if (GeneralConfiguration.enableShieldProjectorRecipe) {
            GameRegistry.addRecipe(new ItemStack(shieldBlock), "gTg", "rMr", "ooo", 'M', ModBlocks.machineFrame, 'o', Blocks.obsidian,
                    'r', Items.redstone, 'T', redstoneTorch, 'g', Items.gold_ingot);

            GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                    new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.obsidian), new ItemStack(Blocks.redstone_block),
                    new ItemStack(Blocks.obsidian), new ItemStack(shieldBlock), new ItemStack(Blocks.obsidian),
                    new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.obsidian), new ItemStack(Blocks.redstone_block)
            }, new ItemStack(shieldBlock2), 4));
            GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                    new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Blocks.obsidian), new ItemStack(DimletSetup.dimensionalShard),
                    new ItemStack(Blocks.obsidian), new ItemStack(shieldBlock2), new ItemStack(Blocks.obsidian),
                    new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Blocks.obsidian), new ItemStack(DimletSetup.dimensionalShard)
            }, new ItemStack(shieldBlock3), 4));
        }

        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 8), "www", "lgl", "www", 'w', Blocks.wool, 'l', lapisStack, 'g', Blocks.glass);
    }
}
