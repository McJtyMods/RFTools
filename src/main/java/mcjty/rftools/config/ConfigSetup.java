package mcjty.rftools.config;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.booster.BoosterConfiguration;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.elevator.ElevatorConfiguration;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.generator.CoalGeneratorConfiguration;
import mcjty.rftools.blocks.infuser.MachineInfuserConfiguration;
import mcjty.rftools.blocks.powercell.PowerCellConfiguration;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigSetup {

    public static Configuration mainConfig;

    public static void init() {
        mainConfig = new Configuration(new File(RFTools.setup.getModConfigDir().getPath() + File.separator + "rftools", "rftools.cfg"));
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
            cfg.addCustomCategoryComment(SecurityConfiguration.CATEGORY_SECURITY, "Settings for the block security system");
            cfg.addCustomCategoryComment(CoalGeneratorConfiguration.CATEGORY_COALGEN, "Settings for the coal generator");
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the crafter");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE, "Settings for the modular storage system");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE_CONFIG, "Generic item module categories for various items");
            cfg.addCustomCategoryComment(ScreenConfiguration.CATEGORY_SCREEN, "Settings for the screen system");
            cfg.addCustomCategoryComment(MachineInfuserConfiguration.CATEGORY_INFUSER, "Settings for the infuser");
            cfg.addCustomCategoryComment(BuilderConfiguration.CATEGORY_BUILDER, "Settings for the builder");
            cfg.addCustomCategoryComment(ScannerConfiguration.CATEGORY_SCANNER, "Settings for the scanner, composer, and projector");
            cfg.addCustomCategoryComment(PowerCellConfiguration.CATEGORY_POWERCELL, "Settings for the powercell");
            cfg.addCustomCategoryComment(ShieldConfiguration.CATEGORY_SHIELD, "Settings for the shield system");
            cfg.addCustomCategoryComment(EnvironmentalConfiguration.CATEGORY_ENVIRONMENTAL, "Settings for the environmental controller");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_SPAWNER, "Settings for the spawner system");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNAMOUNTS, "Amount of materials needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNRF, "Amount of RF needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_LIVINGMATTER, "Blocks and items that are seen as living for the spawner");
            cfg.addCustomCategoryComment(BlockProtectorConfiguration.CATEGORY_BLOCKPROTECTOR, "Settings for the block protector machine");
            cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(EndergenicConfiguration.CATEGORY_ENDERGENIC, "Settings for the endergenic generator");
            cfg.addCustomCategoryComment(StorageScannerConfiguration.CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(ElevatorConfiguration.CATEGORY_ELEVATOR, "Settings for the elevator");
            cfg.addCustomCategoryComment(BoosterConfiguration.CATEGORY_BOOSTER, "Settings for the booster");

            GeneralConfiguration.init(cfg);
            SecurityConfiguration.init(cfg);
            CoalGeneratorConfiguration.init(cfg);
            CrafterConfiguration.init(cfg);
            ModularStorageConfiguration.init(cfg);
            ScreenConfiguration.init(cfg);
            MachineInfuserConfiguration.init(cfg);
            BuilderConfiguration.init(cfg);
            ScannerConfiguration.init(cfg);
            PowerCellConfiguration.init(cfg);
            ShieldConfiguration.init(cfg);
            EnvironmentalConfiguration.init(cfg);
            SpawnerConfiguration.init(cfg);
            BlockProtectorConfiguration.init(cfg);
            NetworkMonitorConfiguration.init(cfg);
            EndergenicConfiguration.init(cfg);
            StorageScannerConfiguration.init(cfg);
            ElevatorConfiguration.init(cfg);
            BoosterConfiguration.init(cfg);
            TeleportConfiguration.init(cfg);

        } catch (Exception e1) {
            Logging.getLogger().log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
        }
    }

    public static void postInit() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }
}
