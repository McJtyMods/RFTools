package mcjty.rftools.blocks.special;

import cpw.mods.fml.common.registry.GameRegistry;

public class SpecialSetup {
    public static VolcanicCoreBlock volcanicCoreBlock;
    public static VolcanicBlock volcanicBlock;

    public static void setupBlocks() {
        volcanicCoreBlock = new VolcanicCoreBlock();
        GameRegistry.registerBlock(volcanicCoreBlock, "volcanicCoreBlock");
        GameRegistry.registerTileEntity(VolcanicCoreTileEntity.class, "VolcanicCoreTileEntity");

        volcanicBlock = new VolcanicBlock();
        GameRegistry.registerBlock(volcanicBlock, "volcanicBlock");
        GameRegistry.registerTileEntity(VolcanicTileEntity.class, "VolcanicTileEntity");
    }
}
