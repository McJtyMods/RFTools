package mcjty.rftools.blocks.builder;

import mcjty.lib.varia.Logging;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public class BuilderConfiguration {
    public static final String CATEGORY_BUILDER = "builder";

    public static ForgeConfigSpec.IntValue BUILDER_MAXENERGY;// = 1000000;
    public static ForgeConfigSpec.IntValue BUILDER_RECEIVEPERTICK;// = 20000;

    public static ForgeConfigSpec.IntValue builderRfPerOperation;// = 500;
    public static ForgeConfigSpec.IntValue builderRfPerLiquid;// = 300;
    public static ForgeConfigSpec.IntValue builderRfPerQuarry;// = 300;
    public static ForgeConfigSpec.IntValue builderRfPerSkipped;// = 50;
    public static ForgeConfigSpec.IntValue builderRfPerEntity;// = 5000;
    public static ForgeConfigSpec.IntValue builderRfPerPlayer;// = 40000;
    public static ForgeConfigSpec.DoubleValue dimensionCostFactor;

    public static ForgeConfigSpec.ConfigValue<BuilderTileEntityMode> teMode;

    public static ForgeConfigSpec.BooleanValue showProgressHud;

    public static ForgeConfigSpec.IntValue maxSpaceChamberDimension;

    public static ForgeConfigSpec.DoubleValue voidShapeCardFactor;
    public static ForgeConfigSpec.DoubleValue silkquarryShapeCardFactor;
    public static ForgeConfigSpec.DoubleValue fortunequarryShapeCardFactor;

    public static ForgeConfigSpec.ConfigValue<String> quarryReplace;
    private static BlockState quarryReplaceBlock = null;

    public static ForgeConfigSpec.BooleanValue quarryChunkloads;
    public static ForgeConfigSpec.BooleanValue shapeCardAllowed;
    public static ForgeConfigSpec.BooleanValue quarryAllowed;
    public static ForgeConfigSpec.BooleanValue clearingQuarryAllowed;

    public static ForgeConfigSpec.IntValue quarryBaseSpeed;
    public static ForgeConfigSpec.IntValue quarryInfusionSpeedFactor;
    public static ForgeConfigSpec.BooleanValue quarryTileEntities;

    public static ForgeConfigSpec.IntValue maxBuilderOffset;
    public static ForgeConfigSpec.IntValue maxBuilderDimension;

    public static ForgeConfigSpec.BooleanValue oldSphereCylinderShape;

    public static ForgeConfigSpec.IntValue collectTimer;
    public static ForgeConfigSpec.IntValue collectRFPerItem;
    public static ForgeConfigSpec.DoubleValue collectRFPerXP;
    public static ForgeConfigSpec.DoubleValue collectRFPerTickPerArea;

    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the builder").push(CATEGORY_BUILDER);
        CLIENT_BUILDER.comment("Settings for the builder").push(CATEGORY_BUILDER);

        BUILDER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the builder can hold")
                .defineInRange("builderMaxRF", 1000000, 0, Integer.MAX_VALUE);
        BUILDER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the builder can receive")
                .defineInRange("builderRFPerTick", 20000, 0, Integer.MAX_VALUE);
        builderRfPerOperation = SERVER_BUILDER
                .comment("RF per block operation for the builder when used to build")
                .defineInRange("builderRfPerOperation", 500, 0, Integer.MAX_VALUE);
        builderRfPerLiquid = SERVER_BUILDER
                .comment("Base RF per block operation for the builder when used as a pump")
                .defineInRange("builderRfPerLiquid", 300, 0, Integer.MAX_VALUE);
        builderRfPerQuarry = SERVER_BUILDER
                .comment("Base RF per block operation for the builder when used as a quarry or voider (actual cost depends on hardness of block)")
                .defineInRange("builderRfPerQuarry", 300, 0, Integer.MAX_VALUE);
        builderRfPerSkipped = SERVER_BUILDER
                .comment("RF per block that is skipped (used when a filter is added to the builder)")
                .defineInRange("builderRfPerSkipped", 50, 0, Integer.MAX_VALUE);
        builderRfPerEntity = SERVER_BUILDER
                .comment("RF per entity move operation for the builder")
                .defineInRange("builderRfPerEntity", 5000, 0, Integer.MAX_VALUE);
        builderRfPerPlayer = SERVER_BUILDER
                .comment("RF per player move operation for the builder")
                .defineInRange("builderRfPerPlayer", 40000, 0, Integer.MAX_VALUE);
        teMode = SERVER_BUILDER
                .comment("Can Tile Entities be moved? 'forbidden' means never, 'whitelist' means only whitelisted, 'blacklist' means all except blacklisted, 'allowed' means all")
                .defineEnum("tileEntityMode", BuilderTileEntityMode.MOVE_WHITELIST, BuilderTileEntityMode.values());
        maxSpaceChamberDimension = SERVER_BUILDER
                .comment("Maximum dimension for the space chamber")
                .defineInRange("maxSpaceChamberDimension", 128, 0, 100000);

        collectTimer = SERVER_BUILDER
                .comment("How many ticks we wait before collecting again (with the builder 'collect items' mode)")
                .defineInRange("collectTimer", 10, 0, Integer.MAX_VALUE);
        collectRFPerItem = SERVER_BUILDER
                .comment("The cost of collecting an item (builder 'collect items' mode))")
                .defineInRange("collectRFPerItem", 20, 0, Integer.MAX_VALUE);

        dimensionCostFactor = SERVER_BUILDER
                .comment("How much more expensive a move accross dimensions is")
                .defineInRange("dimensionCostFactor", 5.0, 0, 1000000.0);

        collectRFPerXP = SERVER_BUILDER
                .comment("The cost of collecting 1 XP level (builder 'collect items' mode))")
                .defineInRange("collectRFPerXP", 2.0, 0, 1000000.0);
        collectRFPerTickPerArea = SERVER_BUILDER
                .comment("The RF/t per area to keep checking for items in a given area (builder 'collect items' mode))")
                .defineInRange("collectRFPerTickPerArea", 0.5, 0, 1000000.0);

        voidShapeCardFactor = SERVER_BUILDER
                .comment("The RF per operation of the builder is multiplied with this factor when using the void shape card")
                .defineInRange("voidShapeCardFactor", 0.5, 0, 1000000.0);
        silkquarryShapeCardFactor = SERVER_BUILDER
                .comment("The RF per operation of the builder is multiplied with this factor when using the silk quarry shape card")
                .defineInRange("silkquarryShapeCardFactor", 3.0, 0, 1000000.0);
        fortunequarryShapeCardFactor = SERVER_BUILDER
                .comment("The RF per operation of the builder is multiplied with this factor when using the fortune quarry shape card")
                .defineInRange("fortunequarryShapeCardFactor", 2, 0, 1000000.0);

        quarryReplace = SERVER_BUILDER
                .comment("Use this block for the builder to replace with")
                .define("quarryReplacE", "minecraft:dirt");
        quarryTileEntities = SERVER_BUILDER
                .comment("If true the quarry will also quarry tile entities. Otherwise it just ignores them")
                .define("quarryTileEntities", true);
        quarryChunkloads = SERVER_BUILDER
                .comment("If true the quarry will chunkload a chunk at a time. If false the quarry will stop if a chunk is not loaded")
                .define("quarryChunkloads", true);
        shapeCardAllowed = SERVER_BUILDER
                .comment("If true we allow shape cards to be crafted. Note that in order to use the quarry system you must also enable this")
                .define("shapeCardAllowed", true);
        quarryAllowed = SERVER_BUILDER
                .comment("If true we allow quarry cards to be crafted")
                .define("quarryAllowed", true);
        clearingQuarryAllowed = SERVER_BUILDER
                .comment("If true we allow the clearing quarry cards to be crafted (these can be heavier on the server)")
                .define("clearingQuarryAllowed", true);

        quarryBaseSpeed = SERVER_BUILDER
                .comment("The base speed (number of blocks per tick) of the quarry")
                .defineInRange("quarryBaseSpeed", 8, 0, Integer.MAX_VALUE);
        quarryInfusionSpeedFactor = SERVER_BUILDER
                .comment("Multiply the infusion factor with this value and add that to the quarry base speed")
                .defineInRange("quarryInfusionSpeedFactor", 20, 0, Integer.MAX_VALUE);

        maxBuilderOffset = SERVER_BUILDER
                .comment("Maximum offset of the shape when a shape card is used in the builder")
                .defineInRange("maxBuilderOffset", 260, 0, Integer.MAX_VALUE);
        maxBuilderDimension = SERVER_BUILDER
                .comment("Maximum dimension of the shape when a shape card is used in the builder")
                .defineInRange("maxBuilderDimension", 512, 0, Integer.MAX_VALUE);

        oldSphereCylinderShape = SERVER_BUILDER
                .comment("If true we go back to the old (wrong) sphere/cylinder calculation for the builder/shield")
                .define("oldSphereCylinderShape", false);
        showProgressHud = CLIENT_BUILDER
                .comment("If true a holo hud with current progress is shown above the builder")
                .define("showProgressHud", true);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static BlockState getQuarryReplace() {
        if (quarryReplaceBlock == null) {
            int index = quarryReplace.get().indexOf(' ');
            if(index == -1) {
                quarryReplaceBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(quarryReplace.get())).getDefaultState();
            } else {
                // @todo 1.14
//                try {
//                    quarryReplaceBlock = CommandBase.convertArgToBlockState(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(quarryReplace.get().substring(0, index))), quarryReplace.get().substring(index + 1));
//                } catch (NumberInvalidException | InvalidBlockStateException e) {
//                    Logging.logError("Invalid builder quarry replace block: " + quarryReplace, e);
//                }
            }
            if (quarryReplaceBlock == null) {
                quarryReplaceBlock = Blocks.DIRT.getDefaultState();
            }
        }
        return quarryReplaceBlock;
    }
}
