package mcjty.rftools.proxy;

import mcjty.lib.base.GeneralConfig;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultCommonSetup;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftools.*;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.booster.BoosterConfiguration;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.elevator.ElevatorConfiguration;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.generator.CoalGeneratorConfiguration;
import mcjty.rftools.blocks.infuser.MachineInfuserConfiguration;
import mcjty.rftools.blocks.powercell.PowerCellConfiguration;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import mcjty.rftools.crafting.ModCrafting;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.integration.computers.OpenComputersIntegration;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;

import java.io.File;
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
        CommandHandler.registerCommands();
        reflect();

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

        readMainConfig();

        RFToolsMessages.registerMessages("rftools");

        ModItems.init();
        ModBlocks.init();
        ModWorldgen.init();

        RFTools.screenModuleRegistry.registerBuiltins();

        ForgeChunkManager.setForcedChunkLoadingCallback(RFTools.instance, (tickets, world) -> {
        });

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        WheelSupport.registerWheel();
    }

    @Override
    public void createTabs() {
        createTab("RfTools", new ItemStack(ModItems.rfToolsManualItem));
    }

    public static Method Block_getSilkTouch;

    private void reflect() {
        Block_getSilkTouch = ReflectionHelper.findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", IBlockState.class);
    }

    private Configuration mainConfig;

    private void readMainConfig() {
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "rftools.cfg"));
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
            cfg.addCustomCategoryComment(SecurityConfiguration.CATEGORY_SECURITY, "Settings for the block security system");
            cfg.addCustomCategoryComment(CoalGeneratorConfiguration.CATEGORY_COALGEN, "Settings for the coal generator");
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the crafter");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE, "Settings for the modular storage system");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE_CONFIG, "Generic item module categories for various items");
            cfg.addCustomCategoryComment(ScreenConfiguration.CATEGORY_SCREEN, "Settings for the screen system");
            cfg.addCustomCategoryComment(MachineInfuserConfiguration.CATEGORY_INFUSER, "Settings for the infuser");
            cfg.addCustomCategoryComment(BuilderConfiguration.CATEGORY_BUILDER, "Settings for the builder");
            cfg.addCustomCategoryComment(ScannerConfiguration.CATEGORY_SCANNER, "Settings for the scanner, composer, and projector");
            cfg.addCustomCategoryComment(PowerCellConfiguration.CATEGORY_POWERCELL, "Settings for the powercell");
            cfg.addCustomCategoryComment(ShieldConfiguration.CATEGORY_SHIELD, "Settings for the shield system");
            cfg.addCustomCategoryComment(EnvironmentalConfiguration.CATEGORY_ENVIRONMENTAL, "Settings for the environmental controller");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_SPAWNER, "Settings for the spawner system");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNAMOUNTS, "Amount of materials needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_MOBSPAWNRF, "Amount of RF needed to spawn mobs");
            cfg.addCustomCategoryComment(SpawnerConfiguration.CATEGORY_LIVINGMATTER, "Blocks and items that are seen as living for the spawner");
            cfg.addCustomCategoryComment(BlockProtectorConfiguration.CATEGORY_BLOCKPROTECTOR, "Settings for the block protector machine");
            cfg.addCustomCategoryComment(NetworkMonitorConfiguration.CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(EndergenicConfiguration.CATEGORY_ENDERGENIC, "Settings for the endergenic generator");
            cfg.addCustomCategoryComment(StorageScannerConfiguration.CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(ElevatorConfiguration.CATEGORY_ELEVATOR, "Settings for the elevator");
            cfg.addCustomCategoryComment(BoosterConfiguration.CATEGORY_BOOSTER, "Settings for the booster");

            GeneralConfiguration.init(cfg);
            SecurityConfiguration.init(cfg);
            CoalGeneratorConfiguration.init(cfg);
            CrafterConfiguration.init(cfg);
            ModularStorageConfiguration.init(cfg);
            ScreenConfiguration.init(cfg);
            MachineInfuserConfiguration.init(cfg);
            BuilderConfiguration.init(cfg);
            ScannerConfiguration.init(cfg);
            PowerCellConfiguration.init(cfg);
            ShieldConfiguration.init(cfg);
            EnvironmentalConfiguration.init(cfg);
            SpawnerConfiguration.init(cfg);
            BlockProtectorConfiguration.init(cfg);
            NetworkMonitorConfiguration.init(cfg);
            EndergenicConfiguration.init(cfg);
            StorageScannerConfiguration.init(cfg);
            ElevatorConfiguration.init(cfg);
            BoosterConfiguration.init(cfg);
            TeleportConfiguration.init(cfg);

        } catch (Exception e1) {
            Logging.getLogger().log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        ModCrafting.init();
        SpawnerConfiguration.readMobSpawnAmountConfig(mainConfig);


        Achievements.init();

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

        if (Loader.isModLoaded("opencomputers")) {
            OpenComputersIntegration.init();
        }

        top = Loader.isModLoaded("theoneprobe");
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
//        MobConfiguration.readModdedMobConfig(mainConfig);
//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }


        mainConfig = null;
        WrenchChecker.init();
    }
}
