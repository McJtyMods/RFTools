package mcjty.rftools.blocks.shaper;

import net.minecraftforge.common.ForgeConfigSpec;

public class ScannerConfiguration {
    public static final String CATEGORY_SCANNER = "scanner";

    public static ForgeConfigSpec.IntValue SCANNER_MAXENERGY; // TODO change these to longs once Configuration supports them
    public static ForgeConfigSpec.IntValue SCANNER_RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue SCANNER_PERTICK;
    public static ForgeConfigSpec.IntValue REMOTE_SCANNER_PERTICK;

    public static ForgeConfigSpec.IntValue LOCATOR_MAXENERGY;
    public static ForgeConfigSpec.IntValue LOCATOR_RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue LOCATOR_PERSCAN_BASE;
    public static ForgeConfigSpec.DoubleValue LOCATOR_PERSCAN_CHUNK;
    public static ForgeConfigSpec.DoubleValue LOCATOR_PERSCAN_HOSTILE;
    public static ForgeConfigSpec.DoubleValue LOCATOR_PERSCAN_PASSIVE;
    public static ForgeConfigSpec.DoubleValue LOCATOR_PERSCAN_PLAYER;
    public static ForgeConfigSpec.DoubleValue LOCATOR_PERSCAN_ENERGY;
    public static ForgeConfigSpec.DoubleValue LOCATOR_FILTER_COST;

    public static ForgeConfigSpec.IntValue ticksPerLocatorScan;
    public static ForgeConfigSpec.IntValue locatorBeaconHeight;
    public static ForgeConfigSpec.IntValue locatorEntitySafety;
    public static ForgeConfigSpec.IntValue locatorMaxEnergyChunks;

    public static ForgeConfigSpec.IntValue PROJECTOR_MAXENERGY;
    public static ForgeConfigSpec.IntValue PROJECTOR_RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue PROJECTOR_USEPERTICK;

    public static ForgeConfigSpec.BooleanValue useVBO;

    // Maximum dimension when the shape card is used for projection/scanner
    public static ForgeConfigSpec.IntValue maxScannerDimension;
    public static ForgeConfigSpec.IntValue maxScannerOffset;

    public static ForgeConfigSpec.IntValue surfaceAreaPerTick;
    public static ForgeConfigSpec.IntValue planeSurfacePerTick;
    public static ForgeConfigSpec.IntValue clientRenderDataTimeout;

    public static ForgeConfigSpec.IntValue projectorFlashTimeout;

    public static ForgeConfigSpec.DoubleValue baseProjectorVolume;      // Use 0 to turn off projector sounds


    public static void init(ForgeConfigSpec.Builder SERVER_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the scanner, composer, and projector").push(CATEGORY_SCANNER);
        CLIENT_BUILDER.comment("Settings for the scanner, composer, and projector").push(CATEGORY_SCANNER);

        SCANNER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the scanner can hold")
                .defineInRange("scannerMaxRF", 500000, 0, Integer.MAX_VALUE);
        SCANNER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the scanner can receive")
                .defineInRange("scannerRFPerTick", 20000, 0, Integer.MAX_VALUE);
        SCANNER_PERTICK = SERVER_BUILDER
                .comment("Amount of RF needed per tick during the scan")
                .defineInRange("scannerUsePerTick", 1000, 0, Integer.MAX_VALUE);
        REMOTE_SCANNER_PERTICK = SERVER_BUILDER
                .comment("Amount of RF needed per tick during the scan for a remote scanner")
                .defineInRange("remoteScannerUsePerTick", 2000, 0, Integer.MAX_VALUE);
        LOCATOR_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the locator can hold")
                .defineInRange("locatorMaxRF", 2000000, 0, Integer.MAX_VALUE);
        LOCATOR_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the locator can receive")
                .defineInRange("locatorRFPerTick", 20000, 0, Integer.MAX_VALUE);
        LOCATOR_PERSCAN_BASE = SERVER_BUILDER
                .comment("Fixed amount of RF needed for a scan")
                .defineInRange("locatorUsePerTickBase", 5000, 0, Integer.MAX_VALUE);

        LOCATOR_PERSCAN_CHUNK = SERVER_BUILDER
                .comment("Base amount of RF needed for a scan per 16x16x16 subchunk")
                .defineInRange("locatorUsePerTickChunk", 0.1, 0, 1000000000.0);
        LOCATOR_PERSCAN_HOSTILE = SERVER_BUILDER
                .comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for hostile entities")
                .defineInRange("locatorUsePerTickHostile", 1.0, 0, 1000000000.0);
        LOCATOR_PERSCAN_PASSIVE = SERVER_BUILDER
                .comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for passive entities")
                .defineInRange("locatorUsePerTickPassive", 0.5, 0, 1000000000.0);
        LOCATOR_PERSCAN_PLAYER = SERVER_BUILDER
                .comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for players")
                .defineInRange("locatorUsePerTickPlayer", 2, 0, 1000000000.0);
        LOCATOR_PERSCAN_ENERGY = SERVER_BUILDER
                .comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for low energy")
                .defineInRange("locatorUsePerTickEnergy", 5, 0, 1000000000.0);
        LOCATOR_FILTER_COST = SERVER_BUILDER
                .comment("Additional amount of RF per 16x16x16 subchunk needed for a filtered scan")
                .defineInRange("locatorFilterCost", 0.5, 0, 1000000000.0);

        PROJECTOR_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the projector can hold")
                .defineInRange("projectorMaxRF", 500000, 0, Integer.MAX_VALUE);
        PROJECTOR_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the projector can receive")
                .defineInRange("projectorRFPerTick", 10000, 0, Integer.MAX_VALUE);
        PROJECTOR_USEPERTICK = SERVER_BUILDER
                .comment("RF/t for the projector while it is in use")
                .defineInRange("projectorUsePerTick", 1000, 0, Integer.MAX_VALUE);

        ticksPerLocatorScan = SERVER_BUILDER
                .comment("Number of ticks between every scan of the locator")
                .defineInRange("ticksPerLocatorScan", 40, 0, Integer.MAX_VALUE);
        locatorBeaconHeight = CLIENT_BUILDER
                .comment("Height of the beacon in case beacons are used")
                .defineInRange("locatorBeaconHeight", 30, 0, Integer.MAX_VALUE);
        locatorEntitySafety = SERVER_BUILDER
                .comment("Maximum amount of entities in a single block to show markers/beacons for")
                .defineInRange("locatorEntitySafety", 10, 0, Integer.MAX_VALUE);
        locatorMaxEnergyChunks = SERVER_BUILDER
                .comment("Maximum amount of 16x16 chunks we support for energy scanning")
                .defineInRange("locatorMaxEnergyChunks", 5*5, 0, Integer.MAX_VALUE);

        maxScannerOffset = SERVER_BUILDER
                .comment("Maximum offset of the shape when a shape card is used in the scanner/projector")
                .defineInRange("maxScannerOffset", 2048, 0, Integer.MAX_VALUE);
        maxScannerDimension = SERVER_BUILDER
                .comment("Maximum dimension of the shape when a scanner/projector card is used")
                .defineInRange("maxScannerDimension", 512, 0, 10000);

        surfaceAreaPerTick = SERVER_BUILDER
                .comment("The amount of surface area the scanner will scan in a tick. Increasing this will increase the speed of the scanner but cause more strain on the server")
                .defineInRange("surfaceAreaPerTick", 512*256*2,
                100, 32768*32768);
        planeSurfacePerTick = SERVER_BUILDER
                .comment("The amount of 'surface area' that the server will send to the client for the projector. Increasing this will increase the speed at which projections are ready but also increase the load for server and client")
                .defineInRange("planeSurfacePerTick", 200*200,
                100, 10000000);
        clientRenderDataTimeout = CLIENT_BUILDER
                .comment("The amount of milliseconds before the client will remove shape render data that hasn't been used. Decreasing this will free memory faster at the cost of having to update shape renders more often")
                .defineInRange("clientRenderDataTimeout", 10000,
                100, 1000000);

        projectorFlashTimeout = CLIENT_BUILDER
                .comment("The amount of milliseconds that a scanline 'flash' will exist on the client")
                .defineInRange("projectorFlashTimeout", 400,
                10, 1000000);

        baseProjectorVolume = CLIENT_BUILDER
                .comment("The volume for the projector sound (0.0 is off)")
                .defineInRange("baseProjectorVolume", 0.4, 0, 1.0);

        useVBO = CLIENT_BUILDER
                .comment("Use VBO for rendering shapecard views. Otherwise display lists")
                .define("useVBO", true);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
