package mcjty.rftools.blocks.shaper;

import net.minecraftforge.common.config.Configuration;

public class ScannerConfiguration {
    public static final String CATEGORY_SCANNER = "scanner";

    public static int SCANNER_MAXENERGY = 500000;
    public static int SCANNER_RECEIVEPERTICK = 20000;
    public static int SCANNER_PERTICK = 1000;

    public static int LOCATOR_MAXENERGY = 500000;
    public static int LOCATOR_RECEIVEPERTICK = 20000;
    public static int LOCATOR_PERSCAN = 20000;

    public static int PROJECTOR_MAXENERGY = 500000;
    public static int PROJECTOR_RECEIVEPERTICK = 10000;
    public static int PROJECTOR_USEPERTICK = 1000;

    public static boolean useVBO = true;

    // Maximum dimension when the shape card is used for projection/scanner
    public static int maxScannerDimension = 512;
    public static int maxScannerOffset = 2048;

    public static int surfaceAreaPerTick = 512*256*2;
    public static int planeSurfacePerTick = 200*200;
    public static int clientRenderDataTimeout = 5000;

    public static int projectorFlashTimeout = 400;

    public static float baseProjectorVolume = 0.4f;      // Use 0 to turn off projector sounds


    public static void init(Configuration cfg) {
        SCANNER_MAXENERGY = cfg.get(CATEGORY_SCANNER, "scannerMaxRF", SCANNER_MAXENERGY,
                "Maximum RF storage that the scanner can hold").getInt();
        SCANNER_RECEIVEPERTICK = cfg.get(CATEGORY_SCANNER, "scannerRFPerTick", SCANNER_RECEIVEPERTICK,
                "RF per tick that the scanner can receive").getInt();
        SCANNER_PERTICK = cfg.get(CATEGORY_SCANNER, "scannerUsePerTick", SCANNER_PERTICK,
                "Amount of RF needed per tick during the scan").getInt();
        LOCATOR_MAXENERGY = cfg.get(CATEGORY_SCANNER, "locatorMaxRF", LOCATOR_MAXENERGY,
                "Maximum RF storage that the locator can hold").getInt();
        LOCATOR_RECEIVEPERTICK = cfg.get(CATEGORY_SCANNER, "locatorRFPerTick", LOCATOR_RECEIVEPERTICK,
                "RF per tick that the locator can receive").getInt();
        LOCATOR_PERSCAN = cfg.get(CATEGORY_SCANNER, "locatorUsePerTick", LOCATOR_PERSCAN,
                "Amount of RF needed for a scan").getInt();
        PROJECTOR_MAXENERGY = cfg.get(CATEGORY_SCANNER, "projectorMaxRF", PROJECTOR_MAXENERGY,
                "Maximum RF storage that the projector can hold").getInt();
        PROJECTOR_RECEIVEPERTICK = cfg.get(CATEGORY_SCANNER, "projectorRFPerTick", PROJECTOR_RECEIVEPERTICK,
                "RF per tick that the projector can receive").getInt();
        PROJECTOR_USEPERTICK = cfg.get(CATEGORY_SCANNER, "projectorUsePerTick", PROJECTOR_USEPERTICK,
                "RF/t for the projector while it is in use").getInt();

        maxScannerOffset = cfg.get(CATEGORY_SCANNER, "maxScannerOffset", maxScannerOffset,
                "Maximum offset of the shape when a shape card is used in the scanner/projector").getInt();
        maxScannerDimension = cfg.get(CATEGORY_SCANNER, "maxScannerDimension", maxScannerDimension,
                "Maximum dimension of the shape when a scanner/projector card is used").getInt();

        surfaceAreaPerTick = cfg.get(CATEGORY_SCANNER, "surfaceAreaPerTick", surfaceAreaPerTick,
                "The amount of surface area the scanner will scan in a tick. Increasing this will increase the speed of the scanner but cause more strain on the server",
                100, 32768*32768).getInt();
        planeSurfacePerTick = cfg.get(CATEGORY_SCANNER, "planeSurfacePerTick", planeSurfacePerTick,
                "The amount of 'surface area' that the server will send to the client for the projector. Increasing this will increase the speed at which projections are ready but also increase the load for server and client",
                100, 10000000).getInt();
        clientRenderDataTimeout = cfg.get(CATEGORY_SCANNER, "clientRenderDataTimeout", clientRenderDataTimeout,
                "The amount of milliseconds before the client will remove shape render data that hasn't been used. Decreasing this will free memory faster at the cost of having to update shape renders more often",
                100, 1000000).getInt();

        projectorFlashTimeout = cfg.get(CATEGORY_SCANNER, "projectorFlashTimeout", projectorFlashTimeout,
                "The amount of milliseconds that a scanline 'flash' will exist on the client",
                10, 1000000).getInt();

        baseProjectorVolume = (float) cfg.get(CATEGORY_SCANNER, "baseProjectorVolume", baseProjectorVolume,
                "The volume for the projector sound (0.0 is off)").getDouble();

        useVBO = cfg.get(CATEGORY_SCANNER, "useVBO", useVBO,
                "Use VBO for rendering shapecard views. Otherwise display lists").getBoolean();
    }
}
