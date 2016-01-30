package mcjty.rftools.blocks.spaceprojector;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.container.GenericItemBlock;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.RFToolsTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.SpaceChamberCardItem;
import mcjty.rftools.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SpaceProjectorSetup {
    public static ProxyBlock proxyBlock;
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static BuilderBlock builderBlock;
    public static SupportBlock supportBlock;

    public static SpaceChamberCardItem spaceChamberCardItem;
    public static ShapeCardItem shapeCardItem;

    private static Map<String,BlockInformation> blockInformationMap = new HashMap<String, BlockInformation>();

    public static void setupBlocks() {
        proxyBlock = new ProxyBlock();
//        GameRegistry.registerBlock(proxyBlock, "proxyBlock");
//        GameRegistry.registerTileEntity(ProxyBlockTileEntity.class, "ProxyBlockTileEntity");

        spaceChamberBlock = new SpaceChamberBlock();
//        GameRegistry.registerBlock(spaceChamberBlock, "spaceChamberBlock");

        spaceChamberControllerBlock = new SpaceChamberControllerBlock();
//        GameRegistry.registerBlock(spaceChamberControllerBlock, GenericItemBlock.class, "spaceChamberControllerBlock");
//        GameRegistry.registerTileEntity(SpaceChamberControllerTileEntity.class, "SpaceChamberControllerTileEntity");

        builderBlock = new BuilderBlock();
//        GameRegistry.registerBlock(builderBlock, GenericItemBlock.class, "builderBlock");
//        GameRegistry.registerTileEntity(BuilderTileEntity.class, "BuilderTileEntity");

        supportBlock = new SupportBlock();
//        GameRegistry.registerBlock(supportBlock, "supportBlock");

        readBuilderBlocksInternal();
        readBuilderBlocksConfig();
    }

    public static void setupItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
//        spaceChamberCardItem.setUnlocalizedName("SpaceChamberCard");
//        spaceChamberCardItem.setCreativeTab(RFTools.tabRfTools);
//        spaceChamberCardItem.setTextureName(RFTools.MODID + ":spaceChamberCardItem");
//        GameRegistry.registerItem(spaceChamberCardItem, "spaceChamberCardItem");

        shapeCardItem = new ShapeCardItem();
    }

    public static void setupCrafting() {
        Block redstoneTorch = Blocks.redstone_torch;
        ItemStack lapisStack = new ItemStack(Items.dye, 1, 4);
        GameRegistry.addRecipe(new ItemStack(spaceChamberBlock), "lgl", "gMg", "lgl", 'M', ModBlocks.machineFrame, 'g', Blocks.glass, 'l', lapisStack);
        GameRegistry.addRecipe(new ItemStack(spaceChamberControllerBlock), " e ", "tMt", " e ", 'M', spaceChamberBlock, 't', redstoneTorch, 'e', Items.ender_pearl);
//@todo
//        if (GeneralConfiguration.enableBuilderRecipe) {
            GameRegistry.addRecipe(new ItemStack(builderBlock), "beb", "rMr", "brb", 'M', ModBlocks.machineFrame, 'e', Items.ender_pearl, 'r', Items.redstone, 'b', Blocks.brick_block);
//        }

        GameRegistry.addRecipe(new ItemStack(spaceChamberCardItem), " b ", "rir", " b ", 'r', Items.redstone, 'i', Items.iron_ingot,
                'b', Items.brick);
    }

    private static void readBuilderBlocksInternal() {
        try {
            InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/builder.json");
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readBuilderBlocksConfig() {
        File modConfigDir = CommonProxy.modConfigDir;
        try {
            File file = new File(modConfigDir.getPath() + File.separator + "rftools", "userbuilder.json");
            FileInputStream inputstream = new FileInputStream(file);
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            Logging.log("Could not read 'userbuilder.json', this is not an error!");
        }
    }

    private static void parseBuilderJson(InputStream inputstream) throws UnsupportedEncodingException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if ("movables".equals(entry.getKey())) {
                readMovablesFromJson(entry.getValue());
            } else if ("rotatables".equals(entry.getKey())) {
                readRotatablesFromJson(entry.getValue());
            }
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
            BlockInformation old = blockInformationMap.get(blockName);
            if (old == null) {
                old = BlockInformation.OK;
            }

            blockInformationMap.put(blockName, new BlockInformation(old, blockName, status, costFactor));
        }
    }

    private static void readRotatablesFromJson(JsonElement element) {
        for (JsonElement entry : element.getAsJsonArray()) {
            String blockName = entry.getAsJsonArray().get(0).getAsString();
            String rotatable = entry.getAsJsonArray().get(1).getAsString();
            BlockInformation old = blockInformationMap.get(blockName);
            if (old == null) {
                old = BlockInformation.OK;
            }
            blockInformationMap.put(blockName, new BlockInformation(old, rotatable));
        }
    }

    public static BlockInformation getBlockInformation(Block block) {
        BlockInformation blockInformation = blockInformationMap.get(block.getUnlocalizedName());
        if (blockInformation == null) {
            String modid = RFToolsTools.getModidForBlock(block);
            blockInformation = blockInformationMap.get("modid:" + modid);
        }
        return blockInformation;
    }

    public static class BlockInformation {
        private final String blockName;
        private final int blockLevel; // One of SupportBlock.SUPPORT_ERROR/WARN
        private final double costFactor;
        private final int rotateInfo;

        public static final int ROTATE_invalid = -1;
        public static final int ROTATE_mmmm = 0;
        public static final int ROTATE_mfff = 1;

        public static final BlockInformation INVALID = new BlockInformation("", SupportBlock.STATUS_ERROR, 1.0);
        public static final BlockInformation OK = new BlockInformation("", SupportBlock.STATUS_OK, 1.0, ROTATE_mmmm);
        public static final BlockInformation FREE = new BlockInformation("", SupportBlock.STATUS_OK, 0.0, ROTATE_mmmm);

        private static int rotateStringToId(String rotateString) {
            if ("mmmm".equals(rotateString)) {
                return ROTATE_mmmm;
            } else if ("mfff".equals(rotateString)) {
                return ROTATE_mfff;
            } else {
                return ROTATE_invalid;
            }
        }

        public BlockInformation(String blockName, int blockLevel, double costFactor) {
            this.blockName = blockName;
            this.blockLevel = blockLevel;
            this.costFactor = costFactor;
            this.rotateInfo = ROTATE_mmmm;
        }

        public BlockInformation(String blockName, int blockLevel, double costFactor, int rotateInfo) {
            this.blockName = blockName;
            this.blockLevel = blockLevel;
            this.costFactor = costFactor;
            this.rotateInfo = rotateInfo;
        }

        public BlockInformation(BlockInformation other, String rotateInfo) {
            this(other.blockName, other.blockLevel, other.costFactor, rotateStringToId(rotateInfo));
        }

        public BlockInformation(BlockInformation other, String blockName, int blockLevel, double costFactor) {
            this(blockName, blockLevel, costFactor, other.rotateInfo);
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

        public int getRotateInfo() {
            return rotateInfo;
        }
    }
}
