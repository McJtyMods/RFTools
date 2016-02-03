package mcjty.rftools.blocks.powercell;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PowerCellSetup {
    public static PowerCellBlock powerCellBlock;

    public static PowerCellCardItem powerCellCardItem;

    public static void init() {
        powerCellBlock = new PowerCellBlock();
        powerCellCardItem = new PowerCellCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        powerCellBlock.initModel();
        powerCellCardItem.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(powerCellBlock), "rdr", "bMb", "rer", 'M', ModBlocks.machineFrame, 'r', Blocks.redstone_block,
                'b', Items.prismarine_shard, 'e', Items.emerald, 'd', Items.diamond);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "rgr", "gPg", "rgr", 'P', Items.paper, 'r', Items.redstone, 'g', Items.gold_nugget);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "c", 'c', powerCellCardItem);
    }
}
