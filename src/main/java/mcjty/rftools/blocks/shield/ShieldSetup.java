package mcjty.rftools.blocks.shield;

import mcjty.rftools.GeneralConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

    @SideOnly(Side.CLIENT)
    public static void initClientPost() {
        solidShieldBlock.initBlockColors();
        noTickSolidShieldBlock.initBlockColors();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        if (GeneralConfiguration.enableShieldProjectorRecipe) {

            // @todo recipes
//            MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
//                    new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Blocks.OBSIDIAN), new ItemStack(Blocks.REDSTONE_BLOCK),
//                    new ItemStack(Blocks.OBSIDIAN), new ItemStack(shieldBlock1), new ItemStack(Blocks.OBSIDIAN),
//                    new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Blocks.OBSIDIAN), new ItemStack(Blocks.REDSTONE_BLOCK)
//            }, new ItemStack(shieldBlock2), 4));
//            MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
//                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.OBSIDIAN), new ItemStack(ModItems.dimensionalShardItem),
//                    new ItemStack(Blocks.OBSIDIAN), new ItemStack(shieldBlock2), new ItemStack(Blocks.OBSIDIAN),
//                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.OBSIDIAN), new ItemStack(ModItems.dimensionalShardItem)
//            }, new ItemStack(shieldBlock3), 4));
//            MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
//                    new ItemStack(Items.NETHER_STAR), new ItemStack(Blocks.OBSIDIAN), new ItemStack(ModItems.dimensionalShardItem),
//                    new ItemStack(Blocks.OBSIDIAN), new ItemStack(shieldBlock3), new ItemStack(Blocks.OBSIDIAN),
//                    new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.NETHER_STAR)
//            }, new ItemStack(shieldBlock4), 4));
        }

    }
}
