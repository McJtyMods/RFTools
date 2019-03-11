package mcjty.rftools;

import mcjty.lib.base.ModBase;
import mcjty.lib.proxy.IProxy;
import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftools.api.teleportation.ITeleportationManager;
import mcjty.rftools.apiimpl.ScreenModuleRegistry;
import mcjty.rftools.apiimpl.TeleportationManager;
import mcjty.rftools.commands.CommandRftCfg;
import mcjty.rftools.commands.CommandRftDb;
import mcjty.rftools.commands.CommandRftShape;
import mcjty.rftools.commands.CommandRftTp;
import mcjty.rftools.items.manual.GuiRFToolsManual;
import mcjty.rftools.setup.CommonSetup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

import java.util.Optional;
import java.util.function.Function;

@Mod(modid = RFTools.MODID, name = "RFTools",
        dependencies =
                        "required-after:mcjtylib_ng@[" + RFTools.MIN_MCJTYLIB_VER + ",);" +
                        "before:xnet@[" + RFTools.MIN_XNET_VER + ",);" +
                        "after:forge@[" + RFTools.MIN_FORGE_VER + ",)",
        acceptedMinecraftVersions = "[1.12,1.13)",
        version = RFTools.VERSION)
public class RFTools implements ModBase {
    public static final String MODID = "rftools";
    public static final String VERSION = "7.61";
    public static final String MIN_FORGE_VER = "14.22.0.2464";
    public static final String MIN_MCJTYLIB_VER = "3.1.0";
    public static final String MIN_XNET_VER = "1.7.0";

    @SidedProxy(clientSide = "mcjty.rftools.proxy.ClientProxy", serverSide = "mcjty.rftools.proxy.ServerProxy")
    public static IProxy proxy;
    public static CommonSetup setup = new CommonSetup();

    @Mod.Instance("rftools")
    public static RFTools instance;

    public static ScreenModuleRegistry screenModuleRegistry = new ScreenModuleRegistry();

    public ClientInfo clientInfo = new ClientInfo();

    @Override
    public String getModId() {
        return MODID;
    }

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        setup.preInit(e);
        proxy.preInit(e);
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }


    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRftTp());
        event.registerServerCommand(new CommandRftShape());
        event.registerServerCommand(new CommandRftDb());
        event.registerServerCommand(new CommandRftCfg());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerAboutToStartEvent event) {
        TickOrderHandler.clean();
    }


    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if (message.key.equalsIgnoreCase("getApi") || message.key.equalsIgnoreCase("getTeleportationManager")) {
                Optional<Function<ITeleportationManager, Void>> value = message.getFunctionValue(ITeleportationManager.class, Void.class);
                value.get().apply(new TeleportationManager());
            } else if (message.key.equalsIgnoreCase("getScreenModuleRegistry")) {
                Optional<Function<IScreenModuleRegistry, Void>> value = message.getFunctionValue(IScreenModuleRegistry.class, Void.class);
                value.get().apply(screenModuleRegistry);
            }
        }

    }

    @Override
    public void openManual(EntityPlayer player, int bookIndex, String page) {
        GuiRFToolsManual.locatePage = page;
        player.openGui(RFTools.instance, bookIndex, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
