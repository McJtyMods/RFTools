package mcjty.rftools.blocks.itemfilter;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ItemFilterSetup {
    public static ItemFilterBlock itemFilterBlock;

    public static void init() {
        itemFilterBlock = new ItemFilterBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        itemFilterBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemFilterBlock), "pcp", "rMr", "pTp", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'p', Items.PAPER,
                               'r', Items.REDSTONE, 'c', "chest"));
    }
}
