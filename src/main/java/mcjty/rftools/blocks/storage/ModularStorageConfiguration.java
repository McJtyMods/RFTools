package mcjty.rftools.blocks.storage;

import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModularStorageConfiguration {
    public static final String CATEGORY_STORAGE = "storage";
    public static final String CATEGORY_STORAGE_CONFIG = "storageconfig";

    public static int groupBackground = 0xffeedd33;
    public static int groupForeground = 0xff000000;

    public static int REMOTE_MAXENERGY = 100000;
    public static int REMOTE_RECEIVEPERTICK = 300;

    public static int TABLET_MAXENERGY = 20000;
    public static int TABLET_RECEIVEPERTICK = 500;
    public static int TABLET_CONSUMEPERUSE = 100;    // Base consumption per usage for the remote storage unit.
    public static int TABLET_EXTRACONSUME = 100;     // Extra RF usage per storage tier.

    public static int remoteShareLocal = 10;         // RF/tick to share this inventory locally (same dimension).
    public static int remoteShareGlobal = 50;        // RF/tick to share this inventory to other dimensions.

    public static Map<String,String> categoryMapper = new HashMap<String, String>();

    public static void init(Configuration cfg) {
        groupBackground = cfg.get(CATEGORY_STORAGE, "groupBackground", groupBackground,
                "Background color for group lines").getInt();
        groupForeground = cfg.get(CATEGORY_STORAGE, "groupForeground", groupForeground,
                "Foreground color for group lines").getInt();

        REMOTE_MAXENERGY = cfg.get(CATEGORY_STORAGE, "remoteStorageMaxRF", REMOTE_MAXENERGY,
                "Maximum RF storage that the remote storage block can hold").getInt();
        REMOTE_RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE, "remoteStorageRFPerTick", REMOTE_RECEIVEPERTICK,
                "RF per tick that the remote storage block can receive").getInt();

        TABLET_MAXENERGY = cfg.get(CATEGORY_STORAGE, "tabletMaxRF", TABLET_MAXENERGY,
                "Maximum RF storage that the storage tablet can hold").getInt();
        TABLET_RECEIVEPERTICK = cfg.get(CATEGORY_STORAGE, "tabletRFPerTick", TABLET_RECEIVEPERTICK,
                "RF per tick that the storage tablet can receive").getInt();
        TABLET_CONSUMEPERUSE = cfg.get(CATEGORY_STORAGE, "tabletRFUsage", TABLET_CONSUMEPERUSE,
                "RF per usage of the storage tablet").getInt();
        TABLET_EXTRACONSUME = cfg.get(CATEGORY_STORAGE, "tabletExtraRFUsage", TABLET_EXTRACONSUME,
                "Extra RF per usage per storage tier").getInt();

        remoteShareLocal = cfg.get(CATEGORY_STORAGE, "remoteShareLocal", remoteShareLocal,
                "RF/tick to share an inventory to the same dimension").getInt();
        remoteShareGlobal = cfg.get(CATEGORY_STORAGE, "remoteShareGlobal", remoteShareGlobal,
                "RF/tick to share an inventory to all dimensions").getInt();

        initCategories();
        ConfigCategory category = cfg.getCategory(CATEGORY_STORAGE_CONFIG);

        // Make a copy of the keys we already have.
        Set<String> keys = new HashSet<String>(categoryMapper.keySet());
        // Scan the config to see if there were updates.
        for (String key : keys) {
            categoryMapper.put(key, cfg.get(CATEGORY_STORAGE_CONFIG, key, categoryMapper.get(key)).getString());
        }
        // Now find all new keys in the config and add those.
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String key = entry.getKey();
            if (!categoryMapper.containsKey(key)) {
                categoryMapper.put(key, entry.getValue().getString());
            }
        }
    }



    public static void dumpClasses(boolean docode) {
        RFTools.log("#### Dumping item and block classification");
        for (Object o : Block.blockRegistry) {
            Block block = (Block) o;
            if (docode) {
                formateAsCode(block.getClass(), getCategory(block.getClass()));
            } else {
                formatClassification("B", BlockInfo.getReadableName(block, 0), block.getClass(), getCategory(block.getClass()));
            }
        }

        for (Object o : Item.itemRegistry) {
            Item item = (Item) o;
            if (docode) {
                formateAsCode(item.getClass(), getCategory(item.getClass()));
            } else {
                formatClassification("I", BlockInfo.getReadableName(item, 0), item.getClass(), getCategory(item.getClass()));
            }
        }
    }

    private static void formatClassification(String type, String name, Class clz, String group) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%1$-1.1s Name:%2$-30.30s Class:%3$-50.50s Group:%4$-20.20s", type, name, clz.getCanonicalName(), group);
        RFTools.log(sb.toString());
    }

    private static void formateAsCode(Class clz, String group) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("categoryMapper.put(\"%1$s\", \"%2$s\");", clz.getCanonicalName(), group);
        RFTools.log(sb.toString());
    }

    public static String getCategory(Class cls) {
        if (cls == null) {
            return null;
        }
        String name = cls.getCanonicalName();
        if (name == null) {
            return null;
        }
        if (categoryMapper.containsKey(name)) {
            return categoryMapper.get(name);
        }

        String[] strings = StringUtils.split(name, ".");
        if (strings == null) {
            return null;
        }

        for (int i = strings.length-1 ; i >= 1 ; i--) {
            String joined = StringUtils.join(strings, '.', 0, i);
            if (categoryMapper.containsKey(joined)) {
                return categoryMapper.get(joined);
            }
        }

        name = getCategory(cls.getSuperclass());
        if (name != null) {
            return name;
        }

        for (Class intface : cls.getInterfaces()) {
            String cat = getCategory(intface);
            if (cat != null) {
                return null;
            }
        }

        return null;
    }

    static void initCategories() {
        categoryMapper.put("net.minecraft.item.ItemPotion", "Potions");
        categoryMapper.put("net.minecraft.item.ItemArmor", "Armor");
        categoryMapper.put("net.minecraft.item.ItemBook", "Books");
        categoryMapper.put("net.minecraft.item.ItemFood", "Food");
        categoryMapper.put("net.minecraft.item.ItemRecord", "Records");
        categoryMapper.put("net.minecraft.item.ItemBucket", "Buckets");
        categoryMapper.put("net.minecraft.item.ItemSkull", "Skulls");
        categoryMapper.put("net.minecraft.item.ItemTool", "Tools");
        categoryMapper.put("net.minecraft.item.ItemHoe", "Tools");
        categoryMapper.put("net.minecraft.item.ItemShears", "Tools");
        categoryMapper.put("net.minecraft.item.ItemSword", "Weapons");
        categoryMapper.put("net.minecraft.item.ItemBow", "Weapons");

        categoryMapper.put("net.minecraft.block.BlockOre", "Ores");
        categoryMapper.put("net.minecraft.block.BlockCake", "Food");
        categoryMapper.put("net.minecraft.block.BlockSapling", "Saplings");
        categoryMapper.put("net.minecraft.block.BlockFlower", "Flowers");

        categoryMapper.put("mcjty.container.GenericBlock", "Machines");

        categoryMapper.put("mcjty.rftools.items.dimlets.KnownDimlet", "Dimlets");
        categoryMapper.put("mcjty.rftools.items.dimlets.UnknownDimlet", "Dimlets");

        categoryMapper.put("mcjty.rftools.items.screenmodules", "Modules");
        categoryMapper.put("mcjty.rftools.items.envmodules", "Modules");
        categoryMapper.put("mcjty.rftools.items.storage", "Modules");


        categoryMapper.put("powercrystals.minefactoryreloaded.block", "Machines");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.base.ItemFactoryBucket", "Buckets");

        categoryMapper.put("cofh.core.item.ItemBucket", "Buckets");
        categoryMapper.put("cofh.thermalexpansion.block.device", "Machines");

        categoryMapper.put("buildcraft.builders", "Machines");

        categoryMapper.put("thermalexpansion.block.machine", "Machines");
        categoryMapper.put("thermalexpansion.block.cell", "Machines");

        categoryMapper.put("crazypants.enderio.machine", "Machines");
        categoryMapper.put("crazypants.enderio.item.skull", "Skulls");
        categoryMapper.put("crazypants.enderio.fluid.ItemBucketEio", "Buckets");

        categoryMapper.put("codechicken.microblock.ItemMicroPart", "Microblocks");

        categoryMapper.put("biomesoplenty.common.items.ItemBOPBucket", "Buckets");

        categoryMapper.put("extrabiomes.blocks.BlockCustomFlower", "Flowers");


        categoryMapper.put("net.minecraft.block.BlockWood", "null");
        categoryMapper.put("net.minecraft.block.BlockDispenser", "Technical");
        categoryMapper.put("net.minecraft.block.BlockNote", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRailPowered", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRailDetector", "Technical");
        categoryMapper.put("net.minecraft.block.BlockPistonBase", "Technical");
        categoryMapper.put("net.minecraft.block.BlockTNT", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRedstoneWire", "Technical");
        categoryMapper.put("net.minecraft.block.BlockWorkbench", "Technical");
        categoryMapper.put("net.minecraft.block.BlockFurnace", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRail", "Technical");
        categoryMapper.put("net.minecraft.block.BlockLever", "Technical");
        categoryMapper.put("net.minecraft.block.BlockPressurePlate", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRedstoneTorch", "Technical");
        categoryMapper.put("net.minecraft.block.BlockButtonStone", "Technical");
        categoryMapper.put("net.minecraft.block.BlockJukebox", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRedstoneRepeater", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRedstoneLight", "Technical");
        categoryMapper.put("net.minecraft.block.BlockTripWireHook", "Technical");
        categoryMapper.put("net.minecraft.block.BlockTripWire", "Technical");
        categoryMapper.put("net.minecraft.block.BlockCommandBlock", "Technical");
        categoryMapper.put("net.minecraft.block.BlockCarrot", "Food");
        categoryMapper.put("net.minecraft.block.BlockPotato", "Food");
        categoryMapper.put("net.minecraft.block.BlockPressurePlateWeighted", "Technical");
        categoryMapper.put("net.minecraft.block.BlockRedstoneComparator", "Technical");
        categoryMapper.put("net.minecraft.block.BlockDaylightDetector", "Technical");
        categoryMapper.put("net.minecraft.block.BlockHopper", "Technical");
        categoryMapper.put("net.minecraft.block.BlockDropper", "Technical");
        categoryMapper.put("mcjty.rftools.blocks.teleporter.DestinationAnalyzerBlock", "Machines");
        categoryMapper.put("mcjty.rftools.blocks.teleporter.MatterBoosterBlock", "Machines");
        categoryMapper.put("mcjty.rftools.blocks.shield.ShieldTemplateBlock", "Machines");
        categoryMapper.put("mcjty.rftools.blocks.MachineFrame", "Machines");
        categoryMapper.put("mcjty.rftools.blocks.MachineBase", "Machines");
        categoryMapper.put("biomesoplenty.common.blocks.BlockBOPFlower", "Flowers");
        categoryMapper.put("biomesoplenty.common.blocks.BlockBOPFlower2", "Flowers");
        categoryMapper.put("crazypants.enderio.block.BlockDarkSteelPressurePlate", "Technical");
        categoryMapper.put("crazypants.enderio.rail.BlockEnderRail", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.BlockConduitBundle", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.facade.BlockConduitFacade", "Technical");
        categoryMapper.put("thermalfoundation.block.BlockOre", "Ores");
        categoryMapper.put("mcjty.rftools.blocks.spaceprojector.SpaceChamberBlock", "Machines");
        categoryMapper.put("com.rwtema.extrautils.tileentity.enderquarry.BlockEnderMarkers", "Technical");
        categoryMapper.put("com.rwtema.extrautils.tileentity.enderquarry.BlockEnderQuarry", "Machines");
        categoryMapper.put("com.rwtema.extrautils.tileentity.generators.BlockGenerator", "Machines");
        categoryMapper.put("com.rwtema.extrautils.tileentity.transfernodes.BlockTransferPipe", "Technical");
        categoryMapper.put("com.rwtema.extrautils.tileentity.enderquarry.BlockQuarryUpgrades", "Technical");
        categoryMapper.put("com.rwtema.extrautils.block.BlockEnderthermicPump", "Machines");
        categoryMapper.put("biomesoplenty.common.itemblocks.ItemBlockFlower", "Flowers");
        categoryMapper.put("biomesoplenty.common.itemblocks.ItemBlockFlower2", "Flowers");
        categoryMapper.put("net.minecraft.item.ItemFlintAndSteel", "Tools");
        categoryMapper.put("net.minecraft.item.ItemMinecart", "Technical");
        categoryMapper.put("net.minecraft.item.ItemRedstone", "Technical");
        categoryMapper.put("net.minecraft.item.ItemMinecart", "Technical");
        categoryMapper.put("codechicken.chunkloader.ItemChunkLoader", "Machines");
        categoryMapper.put("thermalexpansion.block.device.ItemBlockDevice", "Machines");
        categoryMapper.put("thermalexpansion.block.dynamo.ItemBlockDynamo", "Machines");
        categoryMapper.put("mcjty.rftools.items.netmonitor.NetworkMonitorItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.teleportprobe.TeleportProbeItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.teleportprobe.ChargedPorterItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.manual.RFToolsManualItem", "Books");
        categoryMapper.put("mcjty.rftools.items.manual.RFToolsManualDimensionItem", "Books");
        categoryMapper.put("mcjty.rftools.items.devdelight.DevelopersDelightItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.dimlets.DimletTemplate", "Dimlets");
        categoryMapper.put("mcjty.rftools.items.dimlets.EmptyDimensionTab", "Dimlets");
        categoryMapper.put("mcjty.rftools.items.dimlets.RealizedDimensionTab", "Dimlets");
        categoryMapper.put("mcjty.rftools.items.dimensionmonitor.DimensionMonitorItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.dimensionmonitor.PhasedFieldGeneratorItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.parts.DimletControlCircuitItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.DimletEnergyModuleItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.DimletMemoryUnitItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.DimletTypeControllerItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.SyringeItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.PeaceEssenceItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.EfficiencyEssenceItem", "Dimlet Parts");
        categoryMapper.put("mcjty.rftools.items.parts.MediocreEfficiencyEssenceItem", "Dimlet Parts");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.tool.ItemRedNetMeter", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.tool.ItemRedNetMemoryCard", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.ItemLogicUpgradeCard", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.gun.ItemSafariNetLauncher", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.ItemSafariNet", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.ItemPortaSpawner", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.tool.ItemXpExtractor", "Technical");
        categoryMapper.put("powercrystals.minefactoryreloaded.item.ItemLaserFocus", "Technical");
        categoryMapper.put("crazypants.enderio.machine.spawner.ItemBrokenSpawner", "Machines");
        categoryMapper.put("crazypants.enderio.conduit.redstone.ItemRedstoneConduit", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.power.ItemPowerConduit", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.liquid.ItemLiquidConduit", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.item.ItemItemConduit", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.gas.ItemGasConduit", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.item.filter.ItemBasicItemFilter", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.item.filter.ItemExistingItemFilter", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.item.filter.ItemModItemFilter", "Technical");
        categoryMapper.put("crazypants.enderio.conduit.item.ItemExtractSpeedUpgrade", "Technical");
        categoryMapper.put("crazypants.enderio.material.ItemCapacitor", "Technical");
        categoryMapper.put("crazypants.enderio.material.ItemMachinePart", "Technical");
        categoryMapper.put("crazypants.enderio.item.ItemConduitProbe", "Technical");
        categoryMapper.put("crazypants.enderio.item.ItemMagnet", "Technical");
        categoryMapper.put("thermalexpansion.item.tool.ItemWrench", "Technical");
        categoryMapper.put("thermalexpansion.item.tool.ItemMultimeter", "Technical");
        categoryMapper.put("thermalexpansion.item.tool.ItemIgniter", "Technical");
        categoryMapper.put("thermalexpansion.item.ItemCapacitor", "Technical");
        categoryMapper.put("codechicken.microblock.ItemSaw", "Tools");
        categoryMapper.put("mcjty.rftools.blocks.spaceprojector.SpaceChamberCardItem", "Technical");
        categoryMapper.put("mcjty.rftools.items.smartwrench.SmartWrenchItem", "Technical");
    }
}
