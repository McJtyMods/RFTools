package mcjty.rftools.blocks.storage;

import mcjty.lib.thirteen.ConfigSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModularStorageConfiguration {
    public static final String CATEGORY_STORAGE = "storage";
    public static final String CATEGORY_STORAGE_CONFIG = "storageconfig";

    public static ConfigSpec.IntValue groupBackground;
    public static ConfigSpec.IntValue groupForeground;

    public static ConfigSpec.IntValue REMOTE_MAXENERGY;
    public static ConfigSpec.IntValue REMOTE_RECEIVEPERTICK;

    public static ConfigSpec.IntValue TABLET_MAXENERGY;
    public static ConfigSpec.IntValue TABLET_RECEIVEPERTICK;
    public static ConfigSpec.IntValue TABLET_CONSUMEPERUSE;    // Base consumption per usage for the remote storage unit.
    public static ConfigSpec.IntValue TABLET_CONSUMEPERUSE_SCANNER;    // Consumption per usage for the storage scanner version
    public static ConfigSpec.IntValue TABLET_EXTRACONSUME;     // Extra RF usage per storage tier.

    public static ConfigSpec.IntValue remoteShareLocal;         // RF/tick to share this inventory locally (same dimension).
    public static ConfigSpec.IntValue remoteShareGlobal;        // RF/tick to share this inventory to other dimensions.

    public static ConfigSpec.BooleanValue autofocusSearch;  // If true we set auto focus on the search field when opening the GUI.
    public static ConfigSpec.BooleanValue clearSearchOnOpen; // If true we clear the search field when opening the GUI

    public static ConfigSpec.IntValue height1;
    public static ConfigSpec.IntValue height2;
    public static ConfigSpec.IntValue height3;

    private static ConfigSpec.ConfigValue<List<? extends String>> categories;

    public static Map<String,String> categoryMapper = new HashMap<>();

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the modular storage system").push(CATEGORY_STORAGE);
        CLIENT_BUILDER.comment("Settings for the modular storage system").push(CATEGORY_STORAGE);

        groupBackground = CLIENT_BUILDER
                .comment("Background color for group lines")
                .defineInRange("groupBackground", 0xffeedd33, 0, Integer.MAX_VALUE);
        groupForeground = CLIENT_BUILDER
                .comment("Foreground color for group lines")
                .defineInRange("groupForeground", 0xff000000, 0, Integer.MAX_VALUE);

        REMOTE_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the remote storage block can hold")
                .defineInRange("remoteStorageMaxRF", 100000, 0, Integer.MAX_VALUE);
        REMOTE_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the remote storage block can receive")
                .defineInRange("remoteStorageRFPerTick", 300, 0, Integer.MAX_VALUE);

        TABLET_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the storage tablet can hold")
                .defineInRange("tabletMaxRF", 20000, 0, Integer.MAX_VALUE);
        TABLET_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the storage tablet can receive")
                .defineInRange("tabletRFPerTick", 500, 0, Integer.MAX_VALUE);
        TABLET_CONSUMEPERUSE = SERVER_BUILDER
                .comment("RF per usage of the storage tablet")
                .defineInRange("tabletRFUsage", 100, 0, Integer.MAX_VALUE);
        TABLET_CONSUMEPERUSE_SCANNER = SERVER_BUILDER
                .comment("RF per usage of the storage tablet when used in combation with the scanner module")
                .defineInRange("tabletRFUsageScanner", 100, 0, Integer.MAX_VALUE);
        TABLET_EXTRACONSUME = SERVER_BUILDER
                .comment("Extra RF per usage per storage tier")
                .defineInRange("tabletExtraRFUsage", 100, 0, Integer.MAX_VALUE);

        remoteShareLocal = SERVER_BUILDER
                .comment("RF/tick to share an inventory to the same dimension")
                .defineInRange("remoteShareLocal", 10, 0, Integer.MAX_VALUE);
        remoteShareGlobal = SERVER_BUILDER
                .comment("RF/tick to share an inventory to all dimensions")
                .defineInRange("remoteShareGlobal", 50, 0, Integer.MAX_VALUE);

        autofocusSearch = CLIENT_BUILDER
                .comment("If true we automatically set the focus on the search field when opening the GUI for the modular storage. Set to false if you don't want that")
                .define("autofocusSearch", false);
        clearSearchOnOpen = CLIENT_BUILDER
                .comment("If true we clear the search field when opening the GUI for the modular storage. Set to false if you don't want that")
                .define("clearSearchOnOpen", true);

        height1 = SERVER_BUILDER
                .comment("The height for the smallest style modular storage GUI")
                .defineInRange("modularStorageGuiHeight1", 236, 0, 1000000);
        height2 = SERVER_BUILDER
                .comment("The height for the middle style modular storage GUI")
                .defineInRange("modularStorageGuiHeight2", 320, 0, 1000000);
        height3 = SERVER_BUILDER
                .comment("The height for the tallest style modular storage GUI")
                .defineInRange("modularStorageGuiHeight3", 490, 0, 1000000);

        initCategories();
        List<String> defValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoryMapper.entrySet()) {
            String v = entry.getKey() + "=" + entry.getValue();
            defValues.add(v);
        }
        categories = SERVER_BUILDER.defineList("categories", defValues, o -> o instanceof String);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static void resolve() {
        categoryMapper.clear();
        for (String s : categories.get()) {
            String[] split = StringUtils.split(s, "=");
            categoryMapper.put(split[0], split[1]);
        }
    }

    public static String getCategory(Class<?> cls) {
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

        for (Class<?> intface : cls.getInterfaces()) {
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

        categoryMapper.put("mcjty.lib.container.GenericBlock", "Machines");

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
        categoryMapper.put("mcjty.rftools.items.manual.RFToolsShapeManualItem", "Books");
        categoryMapper.put("mcjty.rftools.items.manual.RFToolsDimensionManualItem", "Books");
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
