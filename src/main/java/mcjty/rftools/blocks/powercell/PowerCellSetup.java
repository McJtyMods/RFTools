package mcjty.rftools.blocks.powercell;

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
}
