package mcjty.rftools.proxy;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultCommonSetup;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftools.*;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.crafting.ModCrafting;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.integration.computers.OpenComputersIntegration;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.playerprops.BuffProperties;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.wheelsupport.WheelSupport;
import mcjty.rftools.world.ModWorldgen;
import mcjty.rftools.world.WorldTickHandler;
import mcjty.rftools.xnet.XNetSupport;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
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

public class CommonSetup extends DefaultCommonSetup {

    // Are some mods loaded?.
    public boolean rftoolsDimensions = false;
    public boolean xnet = false;
    public boolean top = false;


    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);

        setupModCompat();

        CommandHandler.registerCommands();
        RFTools.screenModuleRegistry.registerBuiltins();
        reflect();
        ForgeChunkManager.setForcedChunkLoadingCallback(RFTools.instance, (tickets, world) -> { });
        setupCapabilities();

        RFToolsMessages.registerMessages("rftools");

        ConfigSetup.init();
        ModItems.init();
        ModBlocks.init();
        ModWorldgen.init();
    }

    private void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        WheelSupport.registerWheel();

        if (Loader.isModLoaded("rftoolsdim")) {
            rftoolsDimensions = true;
            Logging.log("RFTools Detected Dimensions addon: enabling support");
            FMLInterModComms.sendFunctionMessage("rftoolsdim", "getDimensionManager", "mcjty.rftools.apideps.RFToolsDimensionChecker$GetDimensionManager");
        }

        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.theoneprobe.TheOneProbeSupport");

        if (Loader.isModLoaded("xnet")) {
            xnet = true;
            Logging.log("RFTools Detected XNet: enabling support");
            FMLInterModComms.sendFunctionMessage("xnet", "getXNet", XNetSupport.GetXNet.class.getName());
        }
        top = Loader.isModLoaded("theoneprobe");
    }

    private void setupCapabilities() {
        CapabilityManager.INSTANCE.register(BuffProperties.class, new Capability.IStorage<BuffProperties>() {
            @Override
            public NBTBase writeNBT(Capability<BuffProperties> capability, BuffProperties instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<BuffProperties> capability, BuffProperties instance, EnumFacing side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });

        CapabilityManager.INSTANCE.register(FavoriteDestinationsProperties.class, new Capability.IStorage<FavoriteDestinationsProperties>() {
            @Override
            public NBTBase writeNBT(Capability<FavoriteDestinationsProperties> capability, FavoriteDestinationsProperties instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<FavoriteDestinationsProperties> capability, FavoriteDestinationsProperties instance, EnumFacing side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });

        CapabilityManager.INSTANCE.register(IModuleProvider.class, new Capability.IStorage<IModuleProvider>() {
            @Override
            public NBTBase writeNBT(Capability<IModuleProvider> capability, IModuleProvider instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<IModuleProvider> capability, IModuleProvider instance, EnumFacing side, NBTBase nbt) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });
    }

    @Override
    public void createTabs() {
        createTab("RfTools", new ItemStack(ModItems.rfToolsManualItem));
    }

    public static Method Block_getSilkTouch;

    private void reflect() {
        Block_getSilkTouch = ReflectionHelper.findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", IBlockState.class);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        ModCrafting.init();
        SpawnerConfiguration.readMobSpawnAmountConfig(ConfigSetup.mainConfig);

        Achievements.init();

        if (Loader.isModLoaded("opencomputers")) {
            OpenComputersIntegration.init();
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ConfigSetup.postInit();
        WrenchChecker.init();
    }
}
