package com.mcjty.rftools;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity3;
import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import com.mcjty.rftools.blocks.teleporter.DialingDeviceTileEntity;
import com.mcjty.rftools.blocks.teleporter.MatterReceiverTileEntity;
import com.mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;
import com.mcjty.rftools.crafting.ModCrafting;
import com.mcjty.rftools.gui.GuiProxy;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import com.mcjty.rftools.mobs.ModEntities;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

/**
 * Created by jorrit on 18/07/14.
 */
public class CommonProxy {

    public static final String CATEGORY_CRAFTER = "Crafter";
    public static final String CATEGORY_STORAGE_MONITOR = "StorageMonitor";
    public static final String CATEGORY_NETWORK_MONITOR = "NetworkMonitor";
    public static final String CATEGORY_TELEPORTER = "Teleporter";
    public static final String CATEGORY_ENDERGENIC = "Endergenic";

    public void preInit(FMLPreInitializationEvent e) {
        loadConfiguration(e);

        PacketHandler.registerMessages();

        ModItems.init();
        ModBlocks.init();
        ModCrafting.init();
    }

    private void loadConfiguration(FMLPreInitializationEvent e) {
        Configuration cfg = new Configuration(e.getSuggestedConfigurationFile());
        try {
            cfg.load();
            cfg.addCustomCategoryComment(CATEGORY_CRAFTER, "Settings for the automatic crafter machine");
            cfg.addCustomCategoryComment(CATEGORY_STORAGE_MONITOR, "Settings for the storage scanner machine");
            cfg.addCustomCategoryComment(CATEGORY_NETWORK_MONITOR, "Settings for the network monitor item");
            cfg.addCustomCategoryComment(CATEGORY_TELEPORTER, "Settings for the teleporter system");
            cfg.addCustomCategoryComment(CATEGORY_ENDERGENIC, "Settings for the endergenic generator");

            CrafterBlockTileEntity3.rfPerOperation = cfg.get(CATEGORY_CRAFTER, "rfPerOperation", CrafterBlockTileEntity3.rfPerOperation, "Amount of RF used per crafting operation").getInt();
            CrafterBlockTileEntity3.speedOperations = cfg.get(CATEGORY_CRAFTER, "speedOperations", CrafterBlockTileEntity3.speedOperations, "How many operations to do at once in fast mode").getInt();
            CrafterBlockTileEntity3.MAXENERGY = cfg.get(CATEGORY_CRAFTER, "crafterMaxRF", CrafterBlockTileEntity3.MAXENERGY,
                    "Maximum RF storage that the crafter can hold").getInt();
            CrafterBlockTileEntity3.RECEIVEPERTICK = cfg.get(CATEGORY_CRAFTER, "crafterRFPerTick", CrafterBlockTileEntity3.RECEIVEPERTICK,
                    "RF per tick that the crafter can receive").getInt();

            StorageScannerTileEntity.rfPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "rfPerOperation", StorageScannerTileEntity.rfPerOperation, "Amount of RF used per scan operation").getInt();
            StorageScannerTileEntity.scansPerOperation = cfg.get(CATEGORY_STORAGE_MONITOR, "scansPerOperation", StorageScannerTileEntity.scansPerOperation, "How many blocks to scan per operation").getInt();
            StorageScannerTileEntity.hilightTime = cfg.get(CATEGORY_STORAGE_MONITOR, "hilightTime", StorageScannerTileEntity.hilightTime, "Time (in seconds) to hilight a block in the world").getInt();
            StorageScannerTileEntity.MAXENERGY = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerMaxRF", StorageScannerTileEntity.MAXENERGY,
                    "Maximum RF storage that the storage scanner can hold").getInt();
            StorageScannerTileEntity.RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE_MONITOR, "scannerRFPerTick", StorageScannerTileEntity.RECEIVEPERTICK,
                    "RF per tick that the storage scanner can receive").getInt();

            NetworkMonitorItem.hilightTime = cfg.get(CATEGORY_NETWORK_MONITOR, "hilightTime", NetworkMonitorItem.hilightTime, "Time (in seconds) to hilight a block in the world").getInt();

            MatterTransmitterTileEntity.MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "transmitterMaxRF", MatterTransmitterTileEntity.MAXENERGY,
                    "Maximum RF storage that the matter transmitter can hold. This should be at least equal to 'rfStartTeleportDim'").getInt();
            MatterTransmitterTileEntity.RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "transmitterRFPerTick", MatterTransmitterTileEntity.RECEIVEPERTICK,
                    "RF per tick that the matter transmitter can receive. It is recommended to keep this at least equal to 'rfTeleportPerTick'").getInt();

            MatterReceiverTileEntity.MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "receiverMaxRF", MatterReceiverTileEntity.MAXENERGY,
                    "Maximum RF storage that the matter receiver can hold").getInt();
            MatterReceiverTileEntity.RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "receiverRFPerTick", MatterReceiverTileEntity.RECEIVEPERTICK,
                    "RF per tick that the matter receiver can receive").getInt();

            DialingDeviceTileEntity.MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "dialerMaxRF", DialingDeviceTileEntity.MAXENERGY,
                    "Maximum RF storage that the dialing device can hold").getInt();
            DialingDeviceTileEntity.RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "dialerRFPerTick", DialingDeviceTileEntity.RECEIVEPERTICK,
                    "RF per tick that the dialing device can receive").getInt();

            MatterTransmitterTileEntity.horizontalDialerRange = cfg.get(CATEGORY_TELEPORTER, "horizontalDialerRange", MatterTransmitterTileEntity.horizontalDialerRange,
                    "The horizontal range the dialing device uses to check for transmitters. These are the transmitters the dialing device will be able to control").getInt();
            MatterTransmitterTileEntity.verticalDialerRange = cfg.get(CATEGORY_TELEPORTER, "verticalDialerRange", MatterTransmitterTileEntity.verticalDialerRange,
                    "The vertical range the dialing device uses to check for transmitters").getInt();

            MatterTransmitterTileEntity.rfPerDial = cfg.get(CATEGORY_TELEPORTER, "rfPerDial", MatterTransmitterTileEntity.rfPerDial,
                    "The amount of RF consumed when dialing a transmitter to another receiver").getInt();
            MatterTransmitterTileEntity.rfPerCheck = cfg.get(CATEGORY_TELEPORTER, "rfPerCheck", MatterTransmitterTileEntity.rfPerCheck,
                    "The amount of RF consumed when the dialing device checks for the capabilities of a receiver ('Check' button)").getInt();
            MatterTransmitterTileEntity.rfDialedConnectionPerTick = cfg.get(CATEGORY_TELEPORTER, "rfDialedConnectionPerTick", MatterTransmitterTileEntity.rfDialedConnectionPerTick,
                    "The amount of RF that is consumed by the matter transmitter when a dial is active").getInt();

            MatterTransmitterTileEntity.rfStartTeleportBaseLocal = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportLocal", MatterTransmitterTileEntity.rfStartTeleportBaseLocal,
                    "The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This value is used for a teleport in the same dimension. In addition to this value the 'rfStartTeleportDist' is also added per traveled distance").getInt();
            MatterTransmitterTileEntity.rfStartTeleportBaseDim = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportDim", MatterTransmitterTileEntity.rfStartTeleportBaseDim,
                    "The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This version is for a teleportation to another dimension and in this case 'rfStartTeleportDist' is not used. This value also acts as the maximum rf that can be consumed for a local teleport").getInt();
            MatterTransmitterTileEntity.rfStartTeleportDist = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportDist", MatterTransmitterTileEntity.rfStartTeleportDist,
                    "For every unit in distance this value is added to the initial RF cost for starting the teleportation. This value is not used when teleporting to another dimension").getInt();
            MatterTransmitterTileEntity.rfTeleportPerTick = cfg.get(CATEGORY_TELEPORTER, "rfTeleportPerTick", MatterTransmitterTileEntity.rfTeleportPerTick,
                    "For the duration of the teleport process this value represents the amount of RF that is consumed by the matter transmitter for every tick").getInt();

            MatterTransmitterTileEntity.rfPerTeleportReceiver = cfg.get(CATEGORY_TELEPORTER, "rfPerTeleportReceiver", MatterTransmitterTileEntity.rfPerTeleportReceiver,
                    "This is the amount of RF that is consumed at the receiving side for every teleport. This RF is only consumed when the teleportation actually happens").getInt();

            MatterTransmitterTileEntity.timeTeleportBaseLocal = cfg.get(CATEGORY_TELEPORTER, "timeTeleportBaseLocal", MatterTransmitterTileEntity.timeTeleportBaseLocal,
                    "The base time used for a teleportation for a local teleport. The 'timeTeleportDist' value is added per distance traveled").getInt();
            MatterTransmitterTileEntity.timeTeleportBaseDim = cfg.get(CATEGORY_TELEPORTER, "timeTeleportBaseDim", MatterTransmitterTileEntity.timeTeleportBaseDim,
                    "The base time used for a teleportation to another dimension. The 'timeTeleportDist' value is not used").getInt();
            MatterTransmitterTileEntity.timeTeleportDist = cfg.get(CATEGORY_TELEPORTER, "timeTeleportDist", MatterTransmitterTileEntity.timeTeleportDist,
                    "The amount of time that is added depending on distance for a local teleport. This value is in militicks which means that 1000 is one tick and one tick is 1/20 of a second").getInt();

            EndergenicTileEntity.chanceLost = cfg.get(CATEGORY_ENDERGENIC, "endergenicChanceLost", EndergenicTileEntity.chanceLost,
                    "The chance (in percent) that an endergenic pearl is lost while trying to hold it").getInt();
            EndergenicTileEntity.rfToHoldPearl = cfg.get(CATEGORY_ENDERGENIC, "endergenicRfHolding", EndergenicTileEntity.rfToHoldPearl,
                    "The amount of RF that is consumed every tick to hold the endergenic pearl").getInt();
            EndergenicTileEntity.rfOutput = cfg.get(CATEGORY_ENDERGENIC, "endergenicRfOutput", EndergenicTileEntity.rfOutput,
                    "The amount of RF per tick that this generator can give from its internal buffer to adjacent blocks").getInt();
            EndergenicTileEntity.goodParticleCount = cfg.get(CATEGORY_ENDERGENIC, "endergenicGoodParticles", EndergenicTileEntity.goodParticleCount,
                    "The amount of particles to spawn whenever energy is generated (use 0 to disable)").getInt();
            EndergenicTileEntity.badParticleCount = cfg.get(CATEGORY_ENDERGENIC, "endergenicBadParticles", EndergenicTileEntity.badParticleCount,
                    "The amount of particles to spawn whenever a pearl is lost (use 0 to disable)").getInt();
            EndergenicTileEntity.logEndergenic = cfg.get(CATEGORY_ENDERGENIC, "endergenicLogging", EndergenicTileEntity.logEndergenic,
                    "If true dump a lot of logging information about the generators. Useful for debugging.").getBoolean();

        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        ModEntities.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(RFTools.instance, new GuiProxy());
        MinecraftForge.EVENT_BUS.register(new WorldLoadEvent());
    }

    public void postInit(FMLPostInitializationEvent e) {

    }

}
