package mcjty.rftools.blocks.shield;

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
    public static CamoShieldBlock camoShieldBlock;
    public static NoTickCamoShieldBlock noTickCamoShieldBlock;
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
            camoShieldBlock = new CamoShieldBlock();
            noTickCamoShieldBlock = new NoTickCamoShieldBlock();
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
        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld) {
            solidShieldBlock.initModel();
            noTickSolidShieldBlock.initModel();
            camoShieldBlock.initModel();
            noTickCamoShieldBlock.initModel();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initClientPost() {
        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld) {
            solidShieldBlock.initBlockColors();
            noTickSolidShieldBlock.initBlockColors();
        }
    }
}
