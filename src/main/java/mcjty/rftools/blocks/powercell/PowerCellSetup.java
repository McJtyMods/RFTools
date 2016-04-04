package mcjty.rftools.blocks.powercell;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.InfusedDiamond;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PowerCellSetup {
    public static PowerCellBlock powerCellBlock;
    public static PowerCellBlock advancedPowerCellBlock;
    public static PowerCellBlock creativePowerCellBlock;

    public static PowerCellCardItem powerCellCardItem;

    public static void init() {
        powerCellBlock = new PowerCellBlock("powercell");
        advancedPowerCellBlock = new PowerCellBlock("powercell_advanced");
        creativePowerCellBlock = new PowerCellBlock("powercell_creative");
        powerCellCardItem = new PowerCellCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        powerCellBlock.initModel();
        powerCellCardItem.initModel();
        advancedPowerCellBlock.initModel();
        creativePowerCellBlock.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(powerCellBlock), "rdr", "bMb", "rer", 'M', ModBlocks.machineFrame, 'r', Blocks.redstone_block,
                'b', Items.prismarine_shard, 'e', Items.emerald, 'd', Items.diamond);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "rgr", "gPg", "rgr", 'P', Items.paper, 'r', Items.redstone, 'g', Items.gold_nugget);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "c", 'c', powerCellCardItem);

        InfusedDiamond ind = ModItems.infusedDiamond;
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                new ItemStack(Blocks.redstone_block), new ItemStack(ind), new ItemStack(Blocks.redstone_block),
                new ItemStack(ind), new ItemStack(powerCellBlock), new ItemStack(ind),
                new ItemStack(Blocks.redstone_block), new ItemStack(ind), new ItemStack(Blocks.redstone_block)
        }, new ItemStack(advancedPowerCellBlock), 4));

    }
}
