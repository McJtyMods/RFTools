package mcjty.rftools.blocks.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shaper.ProjectorBlock;
import mcjty.rftools.blocks.shaper.ScannerBlock;
import mcjty.rftools.blocks.shaper.ComposerBlock;
import mcjty.rftools.crafting.PreservingShapedOreRecipe;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.SpaceChamberCardItem;
import mcjty.rftools.proxy.CommonProxy;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BuilderSetup {
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static BuilderBlock builderBlock;
    public static SupportBlock supportBlock;
    public static ComposerBlock composerBlock;
    public static ScannerBlock scannerBlock;
    public static ProjectorBlock projectorBlock;

    public static SpaceChamberCardItem spaceChamberCardItem;
    public static ShapeCardItem shapeCardItem;

    private static Map<String,BlockInformation> blockInformationMap = new HashMap<>();

    public static void init() {
        spaceChamberBlock = new SpaceChamberBlock();
        spaceChamberControllerBlock = new SpaceChamberControllerBlock();
        builderBlock = new BuilderBlock();
        supportBlock = new SupportBlock();
        composerBlock = new ComposerBlock();
        scannerBlock = new ScannerBlock();
        projectorBlock = new ProjectorBlock();

        initItems();

        readBuilderBlocksInternal();
        readBuilderBlocksConfig();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spaceChamberBlock.initModel();
        spaceChamberControllerBlock.initModel();
        builderBlock.initModel();
        supportBlock.initModel();
        composerBlock.initModel();
        scannerBlock.initModel();
        projectorBlock.initModel();

        spaceChamberCardItem.initModel();
        shapeCardItem.initModel();
    }

    private static void initItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        shapeCardItem = new ShapeCardItem();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(spaceChamberBlock), "lgl", "gMg", "lgl", 'M', ModBlocks.machineFrame, 'g', Blocks.GLASS, 'l', "dyeBlue"));
        GameRegistry.addRecipe(new ItemStack(spaceChamberControllerBlock), " e ", "tMt", " e ", 'M', spaceChamberBlock, 't', redstoneTorch, 'e', Items.ENDER_PEARL);
        if (GeneralConfiguration.enableBuilderRecipe) {
            GameRegistry.addRecipe(new ItemStack(builderBlock), "beb", "rMr", "brb", 'M', ModBlocks.machineFrame, 'e', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'b', Blocks.BRICK_BLOCK);
        }

        GameRegistry.addRecipe(new ItemStack(composerBlock), "pbp", "bMb", "pbp", 'M', ModBlocks.machineFrame, 'p', Items.PAPER, 'b', Items.BRICK);
        GameRegistry.addRecipe(new ItemStack(scannerBlock), "beb", "qMq", "brb", 'M', ModBlocks.machineFrame, 'r', Items.REDSTONE, 'b', ModItems.infusedDiamond, 'e', Items.ENDER_PEARL, 'q', Items.QUARTZ);
        GameRegistry.addRecipe(new ItemStack(projectorBlock), "beb", "qMq", "brb", 'M', ModBlocks.machineFrame, 'r', Items.REDSTONE, 'b', ModItems.infusedDiamond, 'e', Blocks.GLASS, 'q', Items.GLOWSTONE_DUST);

        GameRegistry.addRecipe(new ItemStack(spaceChamberCardItem), " b ", "rir", " b ", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                'b', Items.BRICK);

        if (BuilderConfiguration.shapeCardAllowed) {
            GameRegistry.addRecipe(new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), "pbp", "rir", "pbp", 'r', Items.REDSTONE, 'i', Items.IRON_INGOT,
                    'b', Items.BRICK, 'p', Items.PAPER);

            if (BuilderConfiguration.quarryAllowed) {
                GameRegistry.addRecipe(new PreservingShapedOreRecipe(
                                new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_VOID), 4,
                        "ioi", "omo", "ioi",
                        'i', "dyeBlack", 'o', Blocks.OBSIDIAN, 'm', new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_SHAPE)));
                GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.REDSTONE),
                        new ItemStack(Items.BUCKET), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), new ItemStack(Items.BUCKET),
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.REDSTONE)
                }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP), 4));
                GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.REDSTONE),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.REDSTONE)
                }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP_LIQUID), 4));
                GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.REDSTONE),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_SHAPE), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack(Items.REDSTONE)
                }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), 4));
                GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                        new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Items.NETHER_STAR), new ItemStack(ModItems.dimensionalShardItem),
                        new ItemStack(Items.DIAMOND), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), new ItemStack(Items.DIAMOND),
                        new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Items.DIAMOND), new ItemStack(ModItems.dimensionalShardItem)
                }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_SILK), 4));
                GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                        new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Items.GHAST_TEAR), new ItemStack(ModItems.dimensionalShardItem),
                        new ItemStack(Items.EMERALD), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), new ItemStack(Items.DIAMOND),
                        new ItemStack(ModItems.dimensionalShardItem), new ItemStack(Items.REDSTONE), new ItemStack(ModItems.dimensionalShardItem)
                }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_FORTUNE), 4));

                if (BuilderConfiguration.clearingQuarryAllowed) {
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_SILK), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR_SILK), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_FORTUNE), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR_FORTUNE), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP), new ItemStack(Blocks.GLASS),
                            new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.GLASS)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP_CLEAR), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR_SILK), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_SILK), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_CLEAR_FORTUNE), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_QUARRY_FORTUNE), 4));
                    GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP_CLEAR), new ItemStack(Blocks.DIRT),
                            new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.DIRT)
                    }, new ItemStack(shapeCardItem, 1, ShapeCardItem.CARD_PUMP), 4));
                }
            }
        }

    }

    private static void readBuilderBlocksInternal() {
        try {
            InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/builder.json");
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            Logging.logError("Error reading builder.json", e);
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
        BlockInformation information = blockInformationMap.get(block.getRegistryName().toString());
        if (information == null) {
            String modid = RFToolsTools.getModidForBlock(block);
            information = blockInformationMap.get("modid:" + modid);
        }
        return information;
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
