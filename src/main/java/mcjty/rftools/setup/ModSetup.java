package mcjty.rftools.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.compat.wheelsupport.WheelSupport;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Method;

public class ModSetup extends DefaultModSetup {

    // Are some mods loaded?.
    public boolean rftoolsDimensions = false;
    public boolean xnet = false;
    public boolean top = false;

    public ModSetup() {
        createTab("rftools", () -> new ItemStack(Blocks.DIRT));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
/*
        super.init(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        // @todo 1.14
//        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
//        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());

        CommandHandler.registerCommands();
        RFTools.screenModuleRegistry.registerBuiltins();
        reflect();
//        ForgeChunkManager.setForcedChunkLoadingCallback(RFTools.instance, (tickets, world) -> { });
        setupCapabilities();

        RFToolsMessages.registerMessages("rftools");

        Achievements.init();
        ModCrafting.init();
        ModItems.init();
        ModBlocks.init();
//        ModWorldgen.init();
*/
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        WheelSupport.registerWheel();

        if (ModList.get().isLoaded("rftoolsdim")) {
            rftoolsDimensions = true;
            Logging.log("RFTools Detected Dimensions addon: enabling support");
            // @todo 1.14
//            FMLInterModComms.sendFunctionMessage("rftoolsdim", "getDimensionManager", "mcjty.rftools.compat.RFToolsDimensionChecker$GetDimensionManager");
        }

        // @todo 1.14
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.compat.theoneprobe.TheOneProbeSupport");

        if (ModList.get().isLoaded("xnet")) {
            xnet = true;
            Logging.log("RFTools Detected XNet: enabling support");
            // @todo 1.14
//            FMLInterModComms.sendFunctionMessage("xnet", "getXNet", XNetSupport.GetXNet.class.getName());
        }
        top = ModList.get().isLoaded("theoneprobe");

        // @todo 1.14
//        if (ModList.get().isLoaded("opencomputers")) {
//            OpenComputersIntegration.init();
//        }
    }

    private void setupCapabilities() {
        CapabilityManager.INSTANCE.register(IModuleProvider.class, new Capability.IStorage<IModuleProvider>() {
            @Override
            public INBT writeNBT(Capability<IModuleProvider> capability, IModuleProvider instance, Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<IModuleProvider> capability, IModuleProvider instance, Direction side, INBT nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });
    }

    public static Method Block_getSilkTouch;

    private void reflect() {
        // @todo 1.14
//        Block_getSilkTouch = ReflectionHelper.findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", BlockState.class);
    }
}
