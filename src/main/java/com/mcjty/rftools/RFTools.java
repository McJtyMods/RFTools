package com.mcjty.rftools;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
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

    private Coordinate hilightedBlock = null;
    private long expireHilight = 0;

    /** Set our custom inventory Gui index to the next available Gui index */
    public static final int GUI_LIST_BLOCKS = modGuiIndex++;
    public static final int GUI_RF_MONITOR = modGuiIndex++;
    public static final int GUI_CRAFTER = modGuiIndex++;
    public static final int GUI_STORAGE_SCANNER = modGuiIndex++;
    public static final int GUI_RELAY = modGuiIndex++;
    public static final int GUI_MATTER_TRANSMITTER = modGuiIndex++;
    public static final int GUI_MATTER_RECEIVER = modGuiIndex++;
    public static final int GUI_DIALING_DEVICE = modGuiIndex++;
    public static final int GUI_TELEPORTPROBE = modGuiIndex++;
    public static final int GUI_MANUAL = modGuiIndex++;
    public static final int GUI_ENDERGENIC = modGuiIndex++;
    public static final int GUI_SEQUENCER = modGuiIndex++;

    public static void logError(String msg) {
        instance.logger.log(Level.ERROR, msg);
    }

    public void hilightBlock(Coordinate c, long expireHilight) {
        hilightedBlock = c;
        this.expireHilight = expireHilight;
    }

    public Coordinate getHilightedBlock() {
        return hilightedBlock;
    }

    public long getExpireHilight() {
        return expireHilight;
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

    public static void message(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public static void warn(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }
}
