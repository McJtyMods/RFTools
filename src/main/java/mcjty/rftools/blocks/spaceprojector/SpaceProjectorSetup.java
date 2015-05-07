package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;

public class SpaceProjectorSetup {
    public static ProxyBlock proxyBlock;
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static SpaceProjectorBlock spaceProjectorBlock;

    public static SpaceChamberCardItem spaceChamberCardItem;

    public static void setupBlocks() {
        proxyBlock = new ProxyBlock();
        GameRegistry.registerBlock(proxyBlock, "proxyBlock");
        GameRegistry.registerTileEntity(ProxyBlockTileEntity.class, "ProxyBlockTileEntity");

        spaceChamberBlock = new SpaceChamberBlock();
        GameRegistry.registerBlock(spaceChamberBlock, "spaceChamberBlock");

        spaceChamberControllerBlock = new SpaceChamberControllerBlock();
        GameRegistry.registerBlock(spaceChamberControllerBlock, GenericItemBlock.class, "spaceChamberControllerBlock");
        GameRegistry.registerTileEntity(SpaceChamberControllerTileEntity.class, "SpaceChamberControllerTileEntity");

        spaceProjectorBlock = new SpaceProjectorBlock();
        GameRegistry.registerBlock(spaceProjectorBlock, GenericItemBlock.class, "spaceProjectorBlock");
        GameRegistry.registerTileEntity(SpaceProjectorTileEntity.class, "SpaceProjectorTileEntity");
    }

    public static void setupItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        spaceChamberCardItem.setUnlocalizedName("SpaceChamberCard");
        spaceChamberCardItem.setCreativeTab(RFTools.tabRfTools);
        spaceChamberCardItem.setTextureName(RFTools.MODID + ":spaceChamberCardItem");
        GameRegistry.registerItem(spaceChamberCardItem, "spaceChamberCardItem");
    }
}
