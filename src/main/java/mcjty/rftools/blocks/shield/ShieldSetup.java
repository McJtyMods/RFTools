package mcjty.rftools.blocks.shield;

import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShieldSetup {
    public static ShieldBlock shieldBlock1;
    public static ShieldBlock shieldBlock2;
    public static ShieldBlock shieldBlock3;
    public static ShieldBlock shieldBlock4;
    public static InvisibleShieldBlock invisibleShieldBlock;
    public static NoTickInvisibleShieldBlock noTickInvisibleShieldBlock;
    public static SolidShieldBlock solidShieldBlock;
    public static NoTickSolidShieldBlock noTickSolidShieldBlock;
    public static ShieldTemplateBlock shieldTemplateBlock;

    public static void init() {
        shieldBlock1 = new ShieldBlock("shield_block1", ShieldTileEntity.class, ShieldTileEntity.MAX_SHIELD_SIZE);
        shieldBlock2 = new ShieldBlock("shield_block2", ShieldTileEntity2.class, ShieldTileEntity2.MAX_SHIELD_SIZE);
        shieldBlock3 = new ShieldBlock("shield_block3", ShieldTileEntity3.class, ShieldTileEntity3.MAX_SHIELD_SIZE);
        shieldBlock4 = new ShieldBlock("shield_block4", ShieldTileEntity4.class, ShieldTileEntity4.MAX_SHIELD_SIZE);

        invisibleShieldBlock = new InvisibleShieldBlock();
        noTickInvisibleShieldBlock = new NoTickInvisibleShieldBlock();

        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld) {
            solidShieldBlock = new SolidShieldBlock();
            noTickSolidShieldBlock = new NoTickSolidShieldBlock();
            shieldTemplateBlock = new ShieldTemplateBlock();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        shieldBlock1.initModel();
        shieldBlock2.initModel();
        shieldBlock3.initModel();
        shieldBlock4.initModel();
        shieldTemplateBlock.initModel();
        invisibleShieldBlock.initModel();
        noTickInvisibleShieldBlock.initModel();
        solidShieldBlock.initModel();
        noTickSolidShieldBlock.initModel();
    }

    public static void initCrafting() {
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        Block redstoneTorch = Blocks.redstone_torch;

        if (GeneralConfiguration.enableShieldProjectorRecipe) {
            GameRegistry.addRecipe(new ItemStack(shieldBlock1), "gTg", "rMr", "ooo", 'M', ModBlocks.machineFrame, 'o', Blocks.obsidian,
                                   'r', Items.redstone, 'T', redstoneTorch, 'g', Items.gold_ingot);

            GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                    new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.obsidian), new ItemStack(Blocks.redstone_block),
                    new ItemStack(Blocks.obsidian), new ItemStack(shieldBlock1), new ItemStack(Blocks.obsidian),
                    new ItemStack(Blocks.redstone_block), new ItemStack(Blocks.obsidian), new ItemStack(Blocks.redstone_block)
            }, new ItemStack(shieldBlock2), 4));
            GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.obsidian), new ItemStack(ModItems.dimensionalShardItem),
                    new ItemStack(Blocks.obsidian), new ItemStack(shieldBlock2), new ItemStack(Blocks.obsidian),
                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.obsidian), new ItemStack(ModItems.dimensionalShardItem)
            }, new ItemStack(shieldBlock3), 4));
            GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                    new ItemStack(Items.nether_star), new ItemStack(Blocks.obsidian), new ItemStack(ModItems.dimensionalShardItem),
                    new ItemStack(Blocks.obsidian), new ItemStack(shieldBlock3), new ItemStack(Blocks.obsidian),
                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.obsidian), new ItemStack(Items.nether_star)
            }, new ItemStack(shieldBlock4), 4));
        }

        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 8, 0), "www", "lgl", "www", 'w', Blocks.wool, 'l', lapisStack, 'g', Blocks.glass);
        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 1, 1), "s", 's', new ItemStack(shieldTemplateBlock, 1, 0));
        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 1, 2), "s", 's', new ItemStack(shieldTemplateBlock, 1, 1));
        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 1, 3), "s", 's', new ItemStack(shieldTemplateBlock, 1, 2));
        GameRegistry.addRecipe(new ItemStack(shieldTemplateBlock, 1, 0), "s", 's', new ItemStack(shieldTemplateBlock, 1, 3));
    }
}
