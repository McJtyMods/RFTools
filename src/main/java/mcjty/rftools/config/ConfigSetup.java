package mcjty.rftools.config;

import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.booster.BoosterConfiguration;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.elevator.ElevatorConfiguration;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigSetup {

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    static {
        GeneralConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        SecurityConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
//        CoalGeneratorConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        CrafterConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
//        ModularStorageConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        ScreenConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
//        MachineInfuserConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        BuilderConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        ScannerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
//        PowerCellConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        ShieldConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        EnvironmentalConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        BlockProtectorConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        NetworkMonitorConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        EndergenicConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
//        StorageScannerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        ElevatorConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        BoosterConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        TeleportConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
    }

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static void init() {
        // @todo 1.14
//        mainConfig = new Configuration(new File(RFTools.setup.getModConfigDir().getPath() + File.separator + "rftools", "rftools.cfg"));
//        Configuration cfg = mainConfig;
//        try {
//            cfg.load();
//            SERVER_CONFIG = SERVER_BUILDER.build(mainConfig);
//            CLIENT_CONFIG = CLIENT_BUILDER.build(mainConfig);
//
//            SpawnerConfiguration.init(cfg);
//
//        } catch (Exception e1) {
//            Logging.getLogger().log(Level.ERROR, "Problem loading config file!", e1);
//        } finally {
//        }
    }

    public static void postInit() {
        GeneralConfiguration.resolve();
//        ModularStorageConfiguration.resolve();

//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }
    }
}
