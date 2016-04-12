package mcjty.rftools.blocks.itemfilter;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        Block redstoneTorch = Blocks.redstone_torch;

        GameRegistry.addRecipe(new ItemStack(itemFilterBlock), "pcp", "rMr", "pTp", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'p', Items.paper,
                               'r', Items.redstone, 'c', Blocks.chest);
    }
}
