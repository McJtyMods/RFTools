package mcjty.rftools.blocks.blockprotector;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shield.ShieldSetup;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    public static void setupBlocks() {
        blockProtectorBlock = new BlockProtectorBlock();
        GameRegistry.registerBlock(blockProtectorBlock, GenericItemBlock.class, "blockProtectorBlock");
        GameRegistry.registerTileEntity(BlockProtectorTileEntity.class, "BlockProtectorTileEntity");
    }

    public static void setupCrafting() {
        if (GeneralConfiguration.enableBlockProtectorRecipe) {
            GameRegistry.addRecipe(new ItemStack(blockProtectorBlock), "oto", "tMt", "oto", 'M', ModBlocks.machineFrame, 'o', Blocks.obsidian, 't', ShieldSetup.shieldTemplateBlock);
        }
    }
}
