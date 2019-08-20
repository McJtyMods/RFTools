package mcjty.rftools.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.rftools.Achievements;
import mcjty.rftools.ForgeEventHandlers;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.compat.computers.OpenComputersIntegration;
import mcjty.rftools.compat.wheelsupport.WheelSupport;
import mcjty.rftools.compat.xnet.XNetSupport;
import mcjty.rftools.config.ConfigSetup;
import mcjty.rftools.crafting.ModCrafting;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.playerprops.BuffProperties;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.world.ModWorldgen;
import mcjty.rftools.world.WorldTickHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

public class ModSetup extends DefaultModSetup {

    // Are some mods loaded?.
    public boolean rftoolsDimensions = false;
    public boolean xnet = false;
    public boolean top = false;


    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());

        CommandHandler.registerCommands();
        RFTools.screenModuleRegistry.registerBuiltins();
        reflect();
        ForgeChunkManager.setForcedChunkLoadingCallback(RFTools.instance, (tickets, world) -> { });
        setupCapabilities();

        RFToolsMessages.registerMessages("rftools");

        ModItems.init();
        ModBlocks.init();
        ModWorldgen.init();
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        WheelSupport.registerWheel();

        if (Loader.isModLoaded("rftoolsdim")) {
            rftoolsDimensions = true;
            Logging.log("RFTools Detected Dimensions addon: enabling support");
            FMLInterModComms.sendFunctionMessage("rftoolsdim", "getDimensionManager", "mcjty.rftools.compat.RFToolsDimensionChecker$GetDimensionManager");
        }

        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.compat.theoneprobe.TheOneProbeSupport");

        if (Loader.isModLoaded("xnet")) {
            xnet = true;
            Logging.log("RFTools Detected XNet: enabling support");
            FMLInterModComms.sendFunctionMessage("xnet", "getXNet", XNetSupport.GetXNet.class.getName());
        }
        top = Loader.isModLoaded("theoneprobe");

        if (Loader.isModLoaded("opencomputers")) {
            OpenComputersIntegration.init();
        }
    }

    @Override
    protected void setupConfig() {
        ConfigSetup.init();
    }

    private void setupCapabilities() {
        CapabilityManager.INSTANCE.register(BuffProperties.class, new Capability.IStorage<BuffProperties>() {
            @Override
            public NBTBase writeNBT(Capability<BuffProperties> capability, BuffProperties instance, Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<BuffProperties> capability, BuffProperties instance, Direction side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });

        CapabilityManager.INSTANCE.register(FavoriteDestinationsProperties.class, new Capability.IStorage<FavoriteDestinationsProperties>() {
            @Override
            public NBTBase writeNBT(Capability<FavoriteDestinationsProperties> capability, FavoriteDestinationsProperties instance, Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<FavoriteDestinationsProperties> capability, FavoriteDestinationsProperties instance, Direction side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });

        CapabilityManager.INSTANCE.register(IModuleProvider.class, new Capability.IStorage<IModuleProvider>() {
            @Override
            public NBTBase writeNBT(Capability<IModuleProvider> capability, IModuleProvider instance, Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<IModuleProvider> capability, IModuleProvider instance, Direction side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override
    public void createTabs() {
        createTab("RfTools", () -> new ItemStack(ModItems.rfToolsManualItem));
    }

    public static Method Block_getSilkTouch;

    private void reflect() {
        Block_getSilkTouch = ReflectionHelper.findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", BlockState.class);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        ModCrafting.init();
        SpawnerConfiguration.readMobSpawnAmountConfig(ConfigSetup.mainConfig);

        Achievements.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ConfigSetup.postInit();
    }
}
