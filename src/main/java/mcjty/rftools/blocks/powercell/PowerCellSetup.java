package mcjty.rftools.blocks.powercell;

import mcjty.rftools.items.InfusedDiamond;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PowerCellSetup {
    public static PowerCellBlock powerCellBlock;
    public static PowerCellBlock advancedPowerCellBlock;
    public static PowerCellBlock creativePowerCellBlock;
    public static PowerCellBlock simplePowerCellBlock;

    public static PowerCellCardItem powerCellCardItem;

    public static void init() {
        powerCellBlock = new PowerCellBlock("powercell", PowerCellNormalTileEntity.class);
        advancedPowerCellBlock = new PowerCellBlock("powercell_advanced", PowerCellAdvancedTileEntity.class);
        creativePowerCellBlock = new PowerCellBlock("powercell_creative", PowerCellCreativeTileEntity.class);
        simplePowerCellBlock = new PowerCellBlock("powercell_simple", PowerCellSimpleTileEntity.class);
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

        InfusedDiamond ind = ModItems.infusedDiamond;
        // @todo recipes
//        MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
//                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(ind), new ItemStack(Blocks.REDSTONE_BLOCK),
//                new ItemStack(ind), new ItemStack(powerCellBlock), new ItemStack(ind),
//                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(ind), new ItemStack(Blocks.REDSTONE_BLOCK)
//        }, new ItemStack(advancedPowerCellBlock), 4));
//        MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[] {
//                new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND), new ItemStack(Items.REDSTONE),
//                new ItemStack(Items.PRISMARINE_SHARD), new ItemStack(simplePowerCellBlock), new ItemStack(Items.PRISMARINE_SHARD),
//                new ItemStack(Items.REDSTONE), new ItemStack(Items.EMERALD), new ItemStack(Items.REDSTONE)
//        }, new ItemStack(powerCellBlock), 4));

    }
}
