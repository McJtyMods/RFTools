package mcjty.rftools.blocks.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shaper.LocatorBlock;
import mcjty.rftools.blocks.shaper.ProjectorBlock;
import mcjty.rftools.blocks.shaper.ScannerBlock;
import mcjty.rftools.blocks.shaper.ComposerBlock;
import mcjty.rftools.blocks.shaper.ScannerBlock;
import mcjty.rftools.blocks.shaper.*;
import mcjty.rftools.crafting.PreservingShapedOreRecipe;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.SpaceChamberCardItem;
import mcjty.rftools.proxy.CommonProxy;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public static RemoteScannerBlock remoteScannerBlock;
    public static ProjectorBlock projectorBlock;
    public static LocatorBlock locatorBlock;

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
        remoteScannerBlock = new RemoteScannerBlock();
        projectorBlock = new ProjectorBlock();
        locatorBlock = new LocatorBlock();

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
        remoteScannerBlock.initModel();
        projectorBlock.initModel();
        locatorBlock.initModel();

        spaceChamberCardItem.initModel();
        shapeCardItem.initModel();
    }

    private static void initItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        shapeCardItem = new ShapeCardItem();
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
