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
    public static PowerCellBlock simplePowerCellBlock;

    public static PowerCellCardItem powerCellCardItem;

    public static void init() {
        powerCellBlock = new PowerCellBlock("powercell");
        advancedPowerCellBlock = new PowerCellBlock("powercell_advanced");
        creativePowerCellBlock = new PowerCellBlock("powercell_creative");
        simplePowerCellBlock = new PowerCellBlock("powercell_simple");
        powerCellCardItem = new PowerCellCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        powerCellBlock.initModel();
        powerCellCardItem.initModel();
        advancedPowerCellBlock.initModel();
        creativePowerCellBlock.initModel();
        simplePowerCellBlock.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(simplePowerCellBlock), "rdr", "bMb", "rer", 'M', ModBlocks.machineFrame, 'r', Blocks.REDSTONE_BLOCK,
                'b', Items.QUARTZ, 'e', Items.DIAMOND, 'd', Items.DIAMOND);
        GameRegistry.addRecipe(new ItemStack(powerCellBlock), "rdr", "bMb", "rer", 'M', ModBlocks.machineFrame, 'r', Blocks.REDSTONE_BLOCK,
                'b', Items.PRISMARINE_SHARD, 'e', Items.EMERALD, 'd', Items.DIAMOND);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "rgr", "gPg", "rgr", 'P', Items.PAPER, 'r', Items.REDSTONE, 'g', Items.GOLD_NUGGET);
        GameRegistry.addRecipe(new ItemStack(powerCellCardItem), "c", 'c', powerCellCardItem);

        InfusedDiamond ind = ModItems.infusedDiamond;
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(ind), new ItemStack(Blocks.REDSTONE_BLOCK),
                new ItemStack(ind), new ItemStack(powerCellBlock), new ItemStack(ind),
                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(ind), new ItemStack(Blocks.REDSTONE_BLOCK)
        }, new ItemStack(advancedPowerCellBlock), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
                new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND), new ItemStack(Items.REDSTONE),
                new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(simplePowerCellBlock), new ItemStack(Items.PRISMARINE_SHARD),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.EMERALD), new ItemStack(Items.REDSTONE)
        }, new ItemStack(powerCellBlock), 4));

    }
}
