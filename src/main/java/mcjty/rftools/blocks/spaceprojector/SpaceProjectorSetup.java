package mcjty.rftools.blocks.spaceprojector;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SpaceProjectorSetup {
    public static ProxyBlock proxyBlock;
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static SpaceProjectorBlock spaceProjectorBlock;
    public static BuilderBlock builderBlock;
    public static SupportBlock supportBlock;

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

        builderBlock = new BuilderBlock();
        GameRegistry.registerBlock(builderBlock, GenericItemBlock.class, "builderBlock");
        GameRegistry.registerTileEntity(BuilderTileEntity.class, "BuilderTileEntity");

        supportBlock = new SupportBlock();
        GameRegistry.registerBlock(supportBlock, "supportBlock");

        readBuilderBlocks();
    }

    public static void setupItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        spaceChamberCardItem.setUnlocalizedName("SpaceChamberCard");
        spaceChamberCardItem.setCreativeTab(RFTools.tabRfTools);
        spaceChamberCardItem.setTextureName(RFTools.MODID + ":spaceChamberCardItem");
        GameRegistry.registerItem(spaceChamberCardItem, "spaceChamberCardItem");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(spaceChamberBlock), "lgl", "gMg", "lgl", 'M', ModBlocks.machineFrame, 'g', Blocks.glass, 'l', lapisStack);
        GameRegistry.addRecipe(new ItemStack(spaceChamberControllerBlock), " e ", "tMt", " e ", 'M', spaceChamberBlock, 't', redstoneTorch, 'e', Items.ender_pearl);
        if (GeneralConfiguration.enableBuilderRecipe) {
            GameRegistry.addRecipe(new ItemStack(builderBlock), "beb", "rMr", "brb", 'M', ModBlocks.machineFrame, 'e', Items.ender_pearl, 'r', Items.redstone, 'b', Blocks.brick_block);
        }

        GameRegistry.addRecipe(new ItemStack(spaceChamberCardItem), " b ", "rir", " b ", 'p', Items.paper, 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.brick);
    }

    private static void readBuilderBlocks() {
        try {
            InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/builder.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if ("movables".equals(entry.getKey())) {
                    readMovablesFromJson(entry.getValue());
//                } else if ("dimlets".equals(entry.getKey())) {
//                    readDimletsFromJson(entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readMovablesFromJson(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            String blockName = entry.getAsJsonArray().get(0).getAsString();
            String warningType = entry.getAsJsonArray().get(1).getAsString();
            double costFactor = entry.getAsJsonArray().get(2).getAsDouble();
            int status;
            if ("-".equals(warningType)) {
                status = SupportBlock.STATUS_ERROR;
            } else if ("+".equals(warningType)) {
                status = SupportBlock.STATUS_OK;
            } else {
                status = SupportBlock.STATUS_WARN;
            }
            blockInformationMap.put(blockName, new BlockInformation(blockName, status, costFactor));
        }
    }

    public static Map<String,BlockInformation> blockInformationMap = new HashMap<String, BlockInformation>();

    public static class BlockInformation {
        private final String blockName;
        private final int blockLevel; // One of SupportBlock.SUPPORT_ERROR/WARN
        private final double costFactor;

        public static BlockInformation INVALID = new BlockInformation("", SupportBlock.STATUS_ERROR, 1.0);
        public static BlockInformation OK = new BlockInformation("", SupportBlock.STATUS_OK, 1.0);

        public BlockInformation(String blockName, int blockLevel, double costFactor) {
            this.blockName = blockName;
            this.blockLevel = blockLevel;
            this.costFactor = costFactor;
        }

        public int getBlockLevel() {
            return blockLevel;
        }

        public String getBlockName() {
            return blockName;
        }

        public double getCostFactor() {
            return costFactor;
        }
    }
}
