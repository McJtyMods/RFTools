package mcjty.rftools.blocks.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.ItemStackTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shaper.*;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.SpaceChamberCardItem;
import mcjty.rftools.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BuilderSetup {
    public static SpaceChamberBlock spaceChamberBlock;
    public static SpaceChamberControllerBlock spaceChamberControllerBlock;
    public static SupportBlock supportBlock;
    public static GenericBlock<ComposerTileEntity, GenericContainer> composerBlock;
    public static GenericBlock<ScannerTileEntity, GenericContainer> scannerBlock;
    public static GenericBlock<RemoteScannerTileEntity, GenericContainer> remoteScannerBlock;
    public static GenericBlock<ProjectorTileEntity, GenericContainer> projectorBlock;
    public static GenericBlock<LocatorTileEntity, GenericContainer> locatorBlock;
    public static GenericBlock<BuilderTileEntity, GenericContainer> builderBlock;

    public static SpaceChamberCardItem spaceChamberCardItem;
    public static ShapeCardItem shapeCardItem;

    private static Map<String,BlockInformation> blockInformationMap = new HashMap<>();

    public static void init() {
        spaceChamberBlock = new SpaceChamberBlock();
        spaceChamberControllerBlock = new SpaceChamberControllerBlock();
        supportBlock = new SupportBlock();

        builderBlock = ModBlocks.builderFactory.<BuilderTileEntity> builder("builder")
                .tileEntityClass(BuilderTileEntity.class)
                .container(BuilderTileEntity.CONTAINER_FACTORY)
                .flags(BlockFlags.REDSTONE_CHECK)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .moduleSupport(BuilderTileEntity.MODULE_SUPPORT)
                .guiId(RFTools.GUI_BUILDER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.builder")
                .build();

        composerBlock = ModBlocks.builderFactory.<ComposerTileEntity> builder("composer")
                .tileEntityClass(ComposerTileEntity.class)
                .container(ComposerTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_COMPOSER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.composer")
                .build();

        locatorBlock = ModBlocks.builderFactory.<LocatorTileEntity> builder("locator")
                .tileEntityClass(LocatorTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK)
                .emptyContainer()
                .guiId(RFTools.GUI_LOCATOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.locator")
                .build();

        projectorBlock = ModBlocks.builderFactory.<ProjectorTileEntity> builder("projector")
                .tileEntityClass(ProjectorTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .container(ProjectorTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_PROJECTOR)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.projector")
                .build();

        scannerBlock = ModBlocks.builderFactory.<ScannerTileEntity> builder("scanner")
                .tileEntityClass(ScannerTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK)
                .container(ScannerTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_SCANNER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.scanner")
                .infoExtendedParameter(ItemStackTools.intGetter("scanid", -1))
                .build();
        remoteScannerBlock = ModBlocks.builderFactory.<RemoteScannerTileEntity> builder("remote_scanner")
                .tileEntityClass(RemoteScannerTileEntity.class)
                .flags(BlockFlags.REDSTONE_CHECK)
                .container(ScannerTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_SCANNER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.remote_scanner")
                .infoExtendedParameter(ItemStackTools.intGetter("scanid", -1))
                .build();

        initItems();

        readBuilderBlocksInternal();
        readBuilderBlocksConfig();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spaceChamberBlock.initModel();
        spaceChamberControllerBlock.initModel();

        builderBlock.initModel();
        builderBlock.setGuiClass(GuiBuilder.class);
        ClientRegistry.bindTileEntitySpecialRenderer(BuilderTileEntity.class, new BuilderRenderer());

        supportBlock.initModel();

        composerBlock.initModel();
        composerBlock.setGuiClass(GuiComposer.class);

        scannerBlock.initModel();
        scannerBlock.setGuiClass(GuiScanner.class);

        remoteScannerBlock.initModel();
        remoteScannerBlock.setGuiClass(GuiScanner.class);

        projectorBlock.initModel();
        projectorBlock.setGuiClass(GuiProjector.class);
        ClientRegistry.bindTileEntitySpecialRenderer(ProjectorTileEntity.class, new ProjectorRenderer());

        locatorBlock.initModel();
        locatorBlock.setGuiClass(GuiLocator.class);

        spaceChamberCardItem.initModel();
        shapeCardItem.initModel();
    }

    private static void initItems() {
        spaceChamberCardItem = new SpaceChamberCardItem();
        shapeCardItem = new ShapeCardItem();
    }

    private static void readBuilderBlocksInternal() {
        try(InputStream inputstream = RFTools.class.getResourceAsStream("/assets/rftools/text/builder.json")) {
            parseBuilderJson(inputstream);
        } catch (IOException e) {
            Logging.logError("Error reading builder.json", e);
        }
    }

    private static void readBuilderBlocksConfig() {
        File modConfigDir = CommonProxy.modConfigDir;
        File file = new File(modConfigDir.getPath() + File.separator + "rftools", "userbuilder.json");
        try(FileInputStream inputstream = new FileInputStream(file)) {
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
            String modid = BlockTools.getModidForBlock(block);
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
