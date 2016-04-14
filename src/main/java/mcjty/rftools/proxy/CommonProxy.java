package mcjty.rftools.proxy;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.preferences.PreferencesProperties;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftools.ForgeEventHandlers;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.ModSounds;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.endergen.EndergenicConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.generator.CoalGeneratorConfiguration;
import mcjty.rftools.blocks.infuser.MachineInfuserConfiguration;
import mcjty.rftools.blocks.powercell.PowerCellConfiguration;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.spawner.SpawnerConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.crafting.ModCrafting;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.playerprops.BuffProperties;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.playerprops.PorterProperties;
import mcjty.rftools.world.ModWorldgen;
import mcjty.rftools.world.WorldTickHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Level;

import java.io.File;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        McJtyLib.preInit(e);

        GeneralConfig.preInit(e);

        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "rftools.cfg"));

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

        CapabilityManager.INSTANCE.register(PorterProperties.class, new Capability.IStorage<PorterProperties>() {
            @Override
            public NBTBase writeNBT(Capability<PorterProperties> capability, PorterProperties instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<PorterProperties> capability, PorterProperties instance, EnumFacing side, NBTBase nbt) {
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


        readMainConfig();

        SimpleNetworkWrapper network = PacketHandler.registerMessages(RFTools.MODID, "rftools");
        RFToolsMessages.registerNetworkMessages(network);

        ModItems.init();
        ModBlocks.init();
        ModWorldgen.init();
        ModSounds.init();

        RFTools.screenModuleRegistry.registerBuiltins();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
            cfg.addCustomCategoryComment(CoalGeneratorConfiguration.CATEGORY_COALGEN, "Settings for the coal generator");
            cfg.addCustomCategoryComment(CrafterConfiguration.CATEGORY_CRAFTER, "Settings for the crafter");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE, "Settings for the modular storage system");
            cfg.addCustomCategoryComment(ModularStorageConfiguration.CATEGORY_STORAGE_CONFIG, "Generic item module categories for various items");
            cfg.addCustomCategoryComment(ScreenConfiguration.CATEGORY_SCREEN, "Settings for the screen system");
            cfg.addCustomCategoryComment(MachineInfuserConfiguration.CATEGORY_INFUSER, "Settings for the infuser");
            cfg.addCustomCategoryComment(BuilderConfiguration.CATEGORY_BUILDER, "Settings for the builder");
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

            GeneralConfiguration.init(cfg);
            CoalGeneratorConfiguration.init(cfg);
            CrafterConfiguration.init(cfg);
            ModularStorageConfiguration.init(cfg);
            ScreenConfiguration.init(cfg);
            MachineInfuserConfiguration.init(cfg);
            BuilderConfiguration.init(cfg);
            PowerCellConfiguration.init(cfg);
            ShieldConfiguration.init(cfg);
            EnvironmentalConfiguration.init(cfg);
            SpawnerConfiguration.init(cfg);
            BlockProtectorConfiguration.init(cfg);
            NetworkMonitorConfiguration.init(cfg);
            EndergenicConfiguration.init(cfg);
            StorageScannerConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(WorldTickHandler.instance);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        ModCrafting.init();
    }

    public void postInit(FMLPostInitializationEvent e) {
//        MobConfiguration.readModdedMobConfig(mainConfig);
//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }


        mainConfig = null;
        WrenchChecker.init();
    }

}
