package mcjty.rftools;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import mcjty.rftools.apideps.WrenchChecker;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorConfiguration;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import mcjty.rftools.crafting.ModCrafting;
import mcjty.rftools.dimension.DimensionTickEvent;
import mcjty.rftools.dimension.ModDimensions;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import mcjty.rftools.mobs.ModEntities;
import mcjty.rftools.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "main.cfg"));

        readMainConfig();

        PacketHandler.registerMessages();

        ModItems.init();
        ModBlocks.init();
        ModCrafting.init();
        ModDimensions.init();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the automatic crafter machine");
            cfg.addCustomCategoryComment(ScreenConfiguration.CATEGORY_SCREEN, "Settings for the screen system");
            cfg.addCustomCategoryComment(SpaceProjectorConfiguration.CATEGORY_SPACEPROJECTOR, "Settings for the space projector system");
            cfg.addCustomCategoryComment(StorageScannerConfiguration.CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(TeleportConfiguration.CATEGORY_TELEPORTER, "Settings for the teleporter system");
            cfg.addCustomCategoryComment(EndergenicConfiguration.CATEGORY_ENDERGENIC, "Settings for the endergenic generator");
            cfg.addCustomCategoryComment(ShieldConfiguration.CATEGORY_SHIELD, "Settings for the shield system");
            cfg.addCustomCategoryComment(DimletConfiguration.CATEGORY_DIMLETS, "Settings for the dimlet/dimension system");
            cfg.addCustomCategoryComment(DimletConstructionConfiguration.CATEGORY_DIMLET_CONSTRUCTION, "Settings for the dimlet construction system");
            cfg.addCustomCategoryComment(EnvironmentalConfiguration.CATEGORY_ENVIRONMENTAL, "Settings for the environmental controller system");
            cfg.addCustomCategoryComment(BlockProtectorConfiguration.CATEGORY_BLOCKPROTECTOR, "Settings for the block protector machine");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE, "Settings for the modular storage system");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE_CONFIG, "Generic item module categories for various items");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_SPAWNER, "Settings for the spawner system");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNAMOUNTS, "Amount of materials needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNRF, "Amount of RF needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_LIVINGMATTER, "Blocks and items that are seen as living for the spawner");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_RARITY, "General rarity distribution for dimlet selection");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_GENERAL, "General dimension configuration");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, "Settings for the mob dimlets");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_RECURRENTCOMPLEX, "Settings Recurrent Complex structure dimlets");

            GeneralConfiguration.init(cfg);
            NetworkMonitorConfiguration.init(cfg);
            CrafterConfiguration.init(cfg);
            StorageScannerConfiguration.init(cfg);
            TeleportConfiguration.init(cfg);
            EndergenicConfiguration.init(cfg);
            ShieldConfiguration.init(cfg);
            DimletConfiguration.init(cfg);
            DimletConstructionConfiguration.init(cfg);
            EnvironmentalConfiguration.init(cfg);
            SpawnerConfiguration.init(cfg);
            ScreenConfiguration.init(cfg);
            SpaceProjectorConfiguration.init(cfg);
            KnownDimletConfiguration.initGeneralConfig(cfg);
            BlockProtectorConfiguration.init(cfg);
            ModularStorageConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        ModEntities.init();
        RFToolsTradeHandler.INSTANCE.load();

        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        FMLCommonHandler.instance().bus().register(new ClientDisconnectEvent());
        MinecraftForge.EVENT_BUS.register(new WorldLoadEvent());
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        FMLCommonHandler.instance().bus().register(new FMLEventHandlers());
        FMLCommonHandler.instance().bus().register(new DimensionTickEvent());
    }

    public void postInit(FMLPostInitializationEvent e) {
//        MobConfiguration.readModdedMobConfig(mainConfig);
//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }


        mainConfig = null;
        WrenchChecker.init();
    }

}
