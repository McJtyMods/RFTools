package mcjty.rftools;

import mcjty.lib.base.ModBase;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftools.api.teleportation.ITeleportationManager;
import mcjty.rftools.apiimpl.ScreenModuleRegistry;
import mcjty.rftools.apiimpl.TeleportationManager;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import mcjty.rftools.blocks.logic.wireless.RedstoneChannels;
import mcjty.rftools.blocks.powercell.PowerCellNetwork;
import mcjty.rftools.blocks.security.SecurityChannels;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.storage.RemoteStorageIdRegistry;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.commands.CommandRftCfg;
import mcjty.rftools.commands.CommandRftDb;
import mcjty.rftools.commands.CommandRftShape;
import mcjty.rftools.commands.CommandRftTp;
import mcjty.rftools.integration.computers.OpenComputersIntegration;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.manual.GuiRFToolsManual;
import mcjty.rftools.proxy.CommonProxy;
import mcjty.rftools.shapes.ScanDataManager;
import mcjty.rftools.wheelsupport.WheelSupport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
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
    public static final String VERSION = "7.22";
    public static final String MIN_FORGE_VER = "14.22.0.2464";
    public static final String MIN_MCJTYLIB_VER = "2.6.1";
    public static final String MIN_XNET_VER = "1.6.0";

    @SidedProxy(clientSide = "mcjty.rftools.proxy.ClientProxy", serverSide = "mcjty.rftools.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance("rftools")
    public static RFTools instance;

    // Are some mods loaded?.
    public boolean rftoolsDimensions = false;
    public boolean xnet = false;
    public static boolean redstoneflux = false;

    public static ScreenModuleRegistry screenModuleRegistry = new ScreenModuleRegistry();

    /**
     * This is used to keep track of GUIs that we make
     */
    private static int modGuiIndex = 0;

    public ClientInfo clientInfo = new ClientInfo();

    @Override
    public String getModId() {
        return MODID;
    }

    public static CreativeTabs tabRfTools = new CreativeTabs("RfTools") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModItems.rfToolsManualItem);
        }
    };

    public static final String SHIFT_MESSAGE = "<Press Shift>";

    /**
     * Set our custom inventory Gui index to the next available Gui index
     */
    public static final int GUI_MANUAL_MAIN = modGuiIndex++;
    public static final int GUI_MANUAL_SHAPE = modGuiIndex++;
    public static final int GUI_COALGENERATOR = modGuiIndex++;
    public static final int GUI_CRAFTER = modGuiIndex++;
    public static final int GUI_MODULAR_STORAGE = modGuiIndex++;
    public static final int GUI_REMOTE_STORAGE = modGuiIndex++;
    public static final int GUI_STORAGE_FILTER = modGuiIndex++;
    public static final int GUI_MODIFIER_MODULE = modGuiIndex++;
    public static final int GUI_REMOTE_STORAGE_ITEM = modGuiIndex++;
    public static final int GUI_MODULAR_STORAGE_ITEM = modGuiIndex++;
    public static final int GUI_REMOTE_STORAGESCANNER_ITEM = modGuiIndex++;
    public static final int GUI_DIALING_DEVICE = modGuiIndex++;
    public static final int GUI_MATTER_RECEIVER = modGuiIndex++;
    public static final int GUI_MATTER_TRANSMITTER = modGuiIndex++;
    public static final int GUI_ADVANCEDPORTER = modGuiIndex++;
    public static final int GUI_TELEPORTPROBE = modGuiIndex++;
    public static final int GUI_SCREEN = modGuiIndex++;
    public static final int GUI_SCREENCONTROLLER = modGuiIndex++;
    public static final int GUI_COUNTER = modGuiIndex++;
    public static final int GUI_SEQUENCER = modGuiIndex++;
    public static final int GUI_TIMER = modGuiIndex++;
    public static final int GUI_THREE_LOGIC = modGuiIndex++;
    public static final int GUI_MACHINE_INFUSER = modGuiIndex++;
    public static final int GUI_BUILDER = modGuiIndex++;
    public static final int GUI_SHAPECARD = modGuiIndex++;
    public static final int GUI_SHAPECARD_COMPOSER = modGuiIndex++;
    public static final int GUI_CHAMBER_DETAILS = modGuiIndex++;
    public static final int GUI_POWERCELL = modGuiIndex++;
    public static final int GUI_RELAY = modGuiIndex++;
    public static final int GUI_LIQUID_MONITOR = modGuiIndex++;
    public static final int GUI_RF_MONITOR = modGuiIndex++;
    public static final int GUI_SHIELD = modGuiIndex++;
    public static final int GUI_ENVIRONMENTAL_CONTROLLER = modGuiIndex++;
    public static final int GUI_MATTER_BEAMER = modGuiIndex++;
    public static final int GUI_SPAWNER = modGuiIndex++;
    public static final int GUI_BLOCK_PROTECTOR = modGuiIndex++;
    public static final int GUI_ITEMFILTER = modGuiIndex++;
    public static final int GUI_SECURITY_MANAGER = modGuiIndex++;
    public static final int GUI_DEVELOPERS_DELIGHT = modGuiIndex++;
    public static final int GUI_LIST_BLOCKS = modGuiIndex++;
    public static final int GUI_ENDERGENIC = modGuiIndex++;
    public static final int GUI_PEARL_INJECTOR = modGuiIndex++;
    public static final int GUI_ENDERMONITOR = modGuiIndex++;
    public static final int GUI_STORAGE_SCANNER = modGuiIndex++;
    public static final int GUI_BOOSTER = modGuiIndex++;
    public static final int GUI_INVCHECKER = modGuiIndex++;
    public static final int GUI_SENSOR = modGuiIndex++;
    public static final int GUI_STORAGE_TERMINAL = modGuiIndex++;
    public static final int GUI_STORAGE_TERMINAL_SCANNER = modGuiIndex++;
    public static final int GUI_ELEVATOR = modGuiIndex++;
    public static final int GUI_COMPOSER = modGuiIndex++;
    public static final int GUI_SCANNER = modGuiIndex++;
    public static final int GUI_PROJECTOR = modGuiIndex++;
    public static final int GUI_LOCATOR = modGuiIndex++;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        WheelSupport.registerWheel();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);

        Achievements.init();

        rftoolsDimensions = Loader.isModLoaded("rftoolsdim");
        if (rftoolsDimensions) {
            Logging.log("RFTools Detected Dimensions addon: enabling support");
            FMLInterModComms.sendFunctionMessage("rftoolsdim", "getDimensionManager", "mcjty.rftools.apideps.RFToolsDimensionChecker$GetDimensionManager");
        }

        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.theoneprobe.TheOneProbeSupport");

        redstoneflux = Loader.isModLoaded("redstoneflux");
        if (redstoneflux) {
            Logging.log("RFTools Detected RedstoneFlux: enabling support");
        }
        xnet = Loader.isModLoaded("xnet");
        if (xnet) {
            Logging.log("RFTools Detected XNet: enabling support");
            FMLInterModComms.sendFunctionMessage("xnet", "getXNet", "mcjty.rftools.xnet.XNetSupport$GetXNet");
        }

        if (Loader.isModLoaded("opencomputers")) {
            OpenComputersIntegration.init();
        }
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRftTp());
        event.registerServerCommand(new CommandRftShape());
        event.registerServerCommand(new CommandRftDb());
        event.registerServerCommand(new CommandRftCfg());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        Logging.log("RFTools: server is stopping. Shutting down gracefully");
        TeleportDestinations.clearInstance();
        RemoteStorageIdRegistry.clearInstance();
        RedstoneChannels.clearInstance();
        PowerCellNetwork.clearInstance();
        ScanDataManager.clearInstance();
        if(SecurityConfiguration.enabled)
            SecurityChannels.clearInstance();
        if(BlockProtectorConfiguration.enabled)
            BlockProtectors.clearInstance();
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
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
