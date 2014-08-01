package com.mcjty.rftools;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = RFTools.MODID, version = RFTools.VERSION)
public class RFTools {
    public static final String MODID = "rftools";
    public static final String VERSION = "0.0";

    @SidedProxy(clientSide="com.mcjty.rftools.ClientProxy", serverSide="com.mcjty.rftools.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance("rftools")
    public static RFTools instance;

    public Logger logger;

    /** This is used to keep track of GUIs that we make*/
    private static int modGuiIndex = 0;
    /** Set our custom inventory Gui index to the next available Gui index */
    public static final int GUI_LIST_BLOCKS = modGuiIndex++;

    public static void logError(String msg) {
        instance.logger.log(Level.ERROR, msg);
    }

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        this.proxy.preInit(e);
    }
    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @EventHandler
    public void init(FMLInitializationEvent e) {
        this.proxy.init(e);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        this.proxy.postInit(e);
    }
}
