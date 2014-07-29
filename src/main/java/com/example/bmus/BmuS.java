package com.example.bmus;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = BmuS.MODID, version = BmuS.VERSION)
public class BmuS
{
    public static final String MODID = "bmus";
    public static final String VERSION = "0.0";

    @SidedProxy(clientSide="com.example.bmus.ClientProxy", serverSide="com.example.bmus.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance("bmus")
    public static BmuS instance;

    /** This is used to keep track of GUIs that we make*/
    private static int modGuiIndex = 0;
    /** Set our custom inventory Gui index to the next available Gui index */
    public static final int GUI_ITEM_INV = modGuiIndex++;
    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
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
