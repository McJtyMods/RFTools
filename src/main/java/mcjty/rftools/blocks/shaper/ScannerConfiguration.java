package mcjty.rftools.blocks.shaper;

import net.minecraftforge.common.config.Configuration;

public class ScannerConfiguration {
    public static final String CATEGORY_SCANNER = "scanner";

    public static int SCANNER_MAXENERGY = 500000;
    public static int SCANNER_RECEIVEPERTICK = 20000;
    public static int SCANNER_ONESCAN = 500000;

    public static int PROJECTOR_MAXENERGY = 500000;
    public static int PROJECTOR_RECEIVEPERTICK = 10000;
    public static int PROJECTOR_USEPERTICK = 1000;

    public static boolean useVBO = true;

    // Maximum dimension when the shape card is used for projection/scanner
    public static int maxScannerDimension = 512;
    public static int maxScannerOffset = 2048;

    public static int planesPerTick = 2;
    public static int planeSurfacePerTick = 200*200;
    public static int clientRenderDataTimeout = 5000;


    public static void init(Configuration cfg) {
        SCANNER_MAXENERGY = cfg.get(CATEGORY_SCANNER, "scannerMaxRF", SCANNER_MAXENERGY,
                "Maximum RF storage that the scanner can hold").getInt();
        SCANNER_RECEIVEPERTICK = cfg.get(CATEGORY_SCANNER, "scannerRFPerTick", SCANNER_RECEIVEPERTICK,
                "RF per tick that the scanner can receive").getInt();
        SCANNER_ONESCAN = cfg.get(CATEGORY_SCANNER, "scannerOneScanRF", SCANNER_ONESCAN,
                "Amount of RF needed for a single scan operation").getInt();
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

        planesPerTick = cfg.get(CATEGORY_SCANNER, "planesPerTick", planesPerTick,
                "The amount of planes the scanner will scan in a tick. Increasing this will increase the speed of the scanner but cause more strain on the server",
                1, 1000).getInt();
        planeSurfacePerTick = cfg.get(CATEGORY_SCANNER, "planeSurfacePerTick", planeSurfacePerTick,
                "The amount of 'surface area' that the server will send to the client for the projector. Increasing this will increase the speed at which projections are ready but also increase the load for server and client",
                100, 10000000).getInt();
        clientRenderDataTimeout = cfg.get(CATEGORY_SCANNER, "clientRenderDataTimeout", clientRenderDataTimeout,
                "The amount of milliseconds before the client will remove shape render data that hasn't been used. Decreasing this will free memory faster at the cost of having to update shape renders more often",
                100, 1000000).getInt();

        useVBO = cfg.get(CATEGORY_SCANNER, "useVBO", useVBO,
                "Use VBO for rendering shapecard views. Otherwise display lists").getBoolean();
    }
}
