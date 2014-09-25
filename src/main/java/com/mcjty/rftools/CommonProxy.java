package com.mcjty.rftools;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.StorageMonitorTileEntity;
import com.mcjty.rftools.crafting.ModCrafting;
import com.mcjty.rftools.gui.GuiProxy;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.mobs.ModEntities;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

/**
 * Created by jorrit on 18/07/14.
 */
public class CommonProxy {

    public static final String CATEGORY_CRAFTER = "Crafter";
    public static final String CATEGORY_STORAGE_MONITOR = "StorageMonitor";

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
            cfg.addCustomCategoryComment(CATEGORY_CRAFTER, "Settings for the automatic crafter machine");
            cfg.addCustomCategoryComment(CATEGORY_STORAGE_MONITOR, "Settings for the storage monitor machine");
            CrafterBlockTileEntity.rfPerOperation = cfg.get(CATEGORY_CRAFTER, "rfPerOperation", CrafterBlockTileEntity.rfPerOperation, "Amount of RF used per crafting operation").getInt();
            StorageMonitorTileEntity.rfPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "rfPerOperation", StorageMonitorTileEntity.rfPerOperation, "Amount of RF used per scan operation").getInt();
            StorageMonitorTileEntity.scansPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "scansPerOperation", StorageMonitorTileEntity.scansPerOperation, "How many blocks to scan per operation").getInt();
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

    }

    public void postInit(FMLPostInitializationEvent e) {

    }

}
