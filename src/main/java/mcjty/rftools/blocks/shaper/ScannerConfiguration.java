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
    public static int maxScannerOffset = 512;


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

        useVBO = cfg.get(CATEGORY_SCANNER, "useVBO", useVBO,
                "Use VBO for rendering shapecard views. Otherwise display lists").getBoolean();
    }
}
