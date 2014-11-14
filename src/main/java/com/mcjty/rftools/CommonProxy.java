package com.mcjty.rftools;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.crafter.CrafterConfiguration;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import com.mcjty.rftools.blocks.shield.ShieldConfiguration;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.crafting.ModCrafting;
import com.mcjty.rftools.gui.GuiProxy;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import com.mcjty.rftools.mobs.ModEntities;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        loadConfiguration(e);

        PacketHandler.registerMessages();

        ModItems.init();
        ModBlocks.init();
        ModCrafting.init();
    }

    private void loadConfiguration(FMLPreInitializationEvent e) {
        Configuration cfg = new Configuration(e.getSuggestedConfigurationFile());
        try {
            cfg.load();
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the automatic crafter machine");
            cfg.addCustomCategoryComment(StorageScannerConfiguration.CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(TeleportConfiguration.CATEGORY_TELEPORTER, "Settings for the teleporter system");
            cfg.addCustomCategoryComment(EndergenicConfiguration.CATEGORY_ENDERGENIC, "Settings for the endergenic generator");
            cfg.addCustomCategoryComment(ShieldConfiguration.CATEGORY_SHIELD, "Settings for the shield system");
            cfg.addCustomCategoryComment(DimletConfiguration.CATEGORY_DIMLETS, "Settings for the dimlet/dimension system");

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
    }

    public void postInit(FMLPostInitializationEvent e) {

    }

}
