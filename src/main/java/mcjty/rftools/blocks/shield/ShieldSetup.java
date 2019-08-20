package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.common.registry.GameRegistry;



public class ShieldSetup {
    public static ShieldBlock shieldBlock1;
    public static ShieldBlock shieldBlock2;
    public static ShieldBlock shieldBlock3;
    public static ShieldBlock shieldBlock4;

    public static InvisibleShieldBlock invisibleShieldBlock;
    public static NoTickInvisibleShieldBlock noTickInvisibleShieldBlock;
    public static InvisibleShieldBlock invisibleShieldBlockOpaque;
    public static NoTickInvisibleShieldBlock noTickInvisibleShieldBlockOpaque;

    public static SolidShieldBlock solidShieldBlock;
    public static NoTickSolidShieldBlock noTickSolidShieldBlock;
    public static CamoShieldBlock camoShieldBlock;
    public static NoTickCamoShieldBlock noTickCamoShieldBlock;

    public static SolidShieldBlock solidShieldBlockOpaque;
    public static NoTickSolidShieldBlock noTickSolidShieldBlockOpaque;
    public static CamoShieldBlock camoShieldBlockOpaque;
    public static NoTickCamoShieldBlock noTickCamoShieldBlockOpaque;

    public static ShieldTemplateBlock shieldTemplateBlock;

    public static void init() {
        shieldBlock1 = new ShieldBlock("shield_block1", ShieldTileEntity.class, ShieldTileEntity.MAX_SHIELD_SIZE);
        shieldBlock2 = new ShieldBlock("shield_block2", ShieldTileEntity2.class, ShieldConfiguration.maxShieldSize.get() * 4);
        shieldBlock3 = new ShieldBlock("shield_block3", ShieldTileEntity3.class, ShieldConfiguration.maxShieldSize.get() * 16);
        shieldBlock4 = new ShieldBlock("shield_block4", ShieldTileEntity4.class, ShieldConfiguration.maxShieldSize.get() * 128);

        invisibleShieldBlock = new InvisibleShieldBlock("invisible_shield_block", "rftools.invisible_shield_block", false);
        noTickInvisibleShieldBlock = new NoTickInvisibleShieldBlock("notick_invisible_shield_block", "rftools.notick_invisible_shield_block", false);
        invisibleShieldBlockOpaque = new InvisibleShieldBlock("invisible_shield_block_opaque", "rftools.invisible_shield_block", true);
        noTickInvisibleShieldBlockOpaque = new NoTickInvisibleShieldBlock("notick_invisible_shield_block_opaque", "rftools.notick_invisible_shield_block", true);

        shieldTemplateBlock = new ShieldTemplateBlock();

        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
            solidShieldBlock = new SolidShieldBlock("solid_shield_block", "rftools.solid_shield_block", false);
            noTickSolidShieldBlock = new NoTickSolidShieldBlock("notick_solid_shield_block", "rftools.notick_solid_shield_block", false);
            camoShieldBlock = new CamoShieldBlock("camo_shield_block", "rftools.camo_shield_block", false);
            noTickCamoShieldBlock = new NoTickCamoShieldBlock("notick_camo_shield_block", "rftools.notick_camo_shield_block", false);

            solidShieldBlockOpaque = new SolidShieldBlock("solid_shield_block_opaque", "rftools.solid_shield_block", true);
            noTickSolidShieldBlockOpaque = new NoTickSolidShieldBlock("notick_solid_shield_block_opaque", "rftools.notick_solid_shield_block", true);
            camoShieldBlockOpaque = new CamoShieldBlock("camo_shield_block_opaque", "rftools.camo_shield_block", true);
            noTickCamoShieldBlockOpaque = new NoTickCamoShieldBlock("notick_camo_shield_block_opaque", "rftools.notick_camo_shield_block", true);

            GameRegistry.registerTileEntity(TickShieldBlockTileEntity.class, RFTools.MODID + ":invisible_shield_block");
            GameRegistry.registerTileEntity(NoTickShieldBlockTileEntity.class, RFTools.MODID + ":notick_invisible_shield_block");
            GameRegistry.registerTileEntity(TickShieldSolidBlockTileEntity.class, RFTools.MODID + ":solid_shield_block");
            GameRegistry.registerTileEntity(NoTickShieldSolidBlockTileEntity.class, RFTools.MODID + ":notick_solid_shield_block");
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
        invisibleShieldBlockOpaque.initModel();
        noTickInvisibleShieldBlockOpaque.initModel();
        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
            solidShieldBlock.initModel();
            noTickSolidShieldBlock.initModel();
            camoShieldBlock.initModel();
            noTickCamoShieldBlock.initModel();
            solidShieldBlockOpaque.initModel();
            noTickSolidShieldBlockOpaque.initModel();
            camoShieldBlockOpaque.initModel();
            noTickCamoShieldBlockOpaque.initModel();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initClientPost() {
        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
            solidShieldBlock.initBlockColors();
            noTickSolidShieldBlock.initBlockColors();
            solidShieldBlockOpaque.initBlockColors();
            noTickSolidShieldBlockOpaque.initBlockColors();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initColorHandlers(BlockColors blockColors) {
        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
            camoShieldBlock.initColorHandler(blockColors);
            noTickCamoShieldBlock.initColorHandler(blockColors);
            camoShieldBlockOpaque.initColorHandler(blockColors);
            noTickCamoShieldBlockOpaque.initColorHandler(blockColors);
        }
    }
}
