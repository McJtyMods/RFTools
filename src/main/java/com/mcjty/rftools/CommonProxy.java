package com.mcjty.rftools;

import com.mcjty.rftools.apideps.WrenchChecker;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.crafter.CrafterConfiguration;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import com.mcjty.rftools.blocks.shield.ShieldConfiguration;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.crafting.ModCrafting;
import com.mcjty.rftools.dimension.DimensionTickEvent;
import com.mcjty.rftools.dimension.ModDimensions;
import com.mcjty.rftools.gui.GuiProxy;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import com.mcjty.rftools.mobs.ModEntities;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class CommonProxy {

    private Configuration mainConfig;
    private Configuration dimletConfig;

    public void preInit(FMLPreInitializationEvent e) {
        File modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "main.cfg"));
        dimletConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "dimlets.cfg"));

        readMainConfig();
        readDimletConfig();

        PacketHandler.registerMessages();

        ModItems.init();
        ModBlocks.init();
        ModCrafting.init();
        ModDimensions.init();
    }

    private void readDimletConfig() {
        Configuration cfg = dimletConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_GENERAL, "General dimension configuration");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_KNOWNDIMLETS, "Dimlet configuration");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_TYPERARIRTY, "Default rarity per type of dimlet. 0 is very common, 100 is non-existant");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_TYPETICKCOST, "The base amount of time needed to create a dimension per type of dimlet in it");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_TYPERFCREATECOST, "The base amount of RF needed to create a dimension per type of dimlet in it");
            cfg.addCustomCategoryComment(KnownDimletConfiguration.CATEGORY_TYPERFMAINTAINCOST, "The base amount of RF needed to maintain a dimension per type of dimlet in it");
            KnownDimletConfiguration.initGeneralConfig(cfg);
            KnownDimletConfiguration.initTypeRarity(cfg);
            KnownDimletConfiguration.initTypeRfCreateCost(cfg);
            KnownDimletConfiguration.initTypeRfMaintainCost(cfg);
            KnownDimletConfiguration.initTypeTickCost(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading dimlet config file!");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the automatic crafter machine");
            cfg.addCustomCategoryComment(StorageScannerConfiguration.CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(TeleportConfiguration.CATEGORY_TELEPORTER, "Settings for the teleporter system");
            cfg.addCustomCategoryComment(EndergenicConfiguration.CATEGORY_ENDERGENIC, "Settings for the endergenic generator");
            cfg.addCustomCategoryComment(ShieldConfiguration.CATEGORY_SHIELD, "Settings for the shield system");
            cfg.addCustomCategoryComment(DimletConfiguration.CATEGORY_DIMLETS, "Settings for the dimlet/dimension system");

            GeneralConfiguration.init(cfg);
            NetworkMonitorConfiguration.init(cfg);
            CrafterConfiguration.init(cfg);
            StorageScannerConfiguration.init(cfg);
            TeleportConfiguration.init(cfg);
            EndergenicConfiguration.init(cfg);
            ShieldConfiguration.init(cfg);
            DimletConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        ModEntities.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(new WorldLoadEvent());
        FMLCommonHandler.instance().bus().register(new DimensionTickEvent());
    }

    public void postInit(FMLPostInitializationEvent e) {
        KnownDimletConfiguration.init(dimletConfig);

        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
        if (dimletConfig.hasChanged()) {
            dimletConfig.save();
        }
        mainConfig = null;
        dimletConfig = null;
        WrenchChecker.init();
    }

}
