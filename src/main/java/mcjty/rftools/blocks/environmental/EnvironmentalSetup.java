package mcjty.rftools.blocks.environmental;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.crafting.NBTMatchingRecipe;
import mcjty.rftools.items.envmodules.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentalSetup {
    public static EnvironmentalControllerBlock environmentalControllerBlock;

    public static RegenerationEModuleItem regenerationEModuleItem;
    public static RegenerationPlusEModuleItem regenerationPlusEModuleItem;
    public static SpeedEModuleItem speedEModuleItem;
    public static SpeedPlusEModuleItem speedPlusEModuleItem;
    public static HasteEModuleItem hasteEModuleItem;
    public static HastePlusEModuleItem hastePlusEModuleItem;
    public static SaturationEModuleItem saturationEModuleItem;
    public static SaturationPlusEModuleItem saturationPlusEModuleItem;
    public static FeatherFallingEModuleItem featherFallingEModuleItem;
    public static FeatherFallingPlusEModuleItem featherFallingPlusEModuleItem;
    public static FlightEModuleItem flightEModuleItem;
    public static PeacefulEModuleItem peacefulEModuleItem;
    public static WaterBreathingEModuleItem waterBreathingEModuleItem;
    public static NightVisionEModuleItem nightVisionEModuleItem;

    public static void setupBlocks() {
        environmentalControllerBlock = new EnvironmentalControllerBlock();
        GameRegistry.registerBlock(environmentalControllerBlock, GenericItemBlock.class, "environmentalControllerBlock");
        GameRegistry.registerTileEntity(EnvironmentalControllerTileEntity.class, "EnvironmentalControllerTileEntity");
    }

    public static void setupItems() {
        regenerationEModuleItem = new RegenerationEModuleItem();
        regenerationEModuleItem.setUnlocalizedName("RegenerationEModule");
        regenerationEModuleItem.setCreativeTab(RFTools.tabRfTools);
        regenerationEModuleItem.setTextureName(RFTools.MODID + ":envmodules/regenerationEModuleItem");
        GameRegistry.registerItem(regenerationEModuleItem, "regenerationEModuleItem");

        regenerationPlusEModuleItem = new RegenerationPlusEModuleItem();
        regenerationPlusEModuleItem.setUnlocalizedName("RegenerationPlusEModule");
        regenerationPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        regenerationPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/regenerationPlusEModuleItem");
        GameRegistry.registerItem(regenerationPlusEModuleItem, "regenerationPlusEModuleItem");

        speedEModuleItem = new SpeedEModuleItem();
        speedEModuleItem.setUnlocalizedName("SpeedEModule");
        speedEModuleItem.setCreativeTab(RFTools.tabRfTools);
        speedEModuleItem.setTextureName(RFTools.MODID + ":envmodules/speedEModuleItem");
        GameRegistry.registerItem(speedEModuleItem, "speedEModuleItem");

        speedPlusEModuleItem = new SpeedPlusEModuleItem();
        speedPlusEModuleItem.setUnlocalizedName("SpeedPlusEModule");
        speedPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        speedPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/speedPlusEModuleItem");
        GameRegistry.registerItem(speedPlusEModuleItem, "speedPlusEModuleItem");

        hasteEModuleItem = new HasteEModuleItem();
        hasteEModuleItem.setUnlocalizedName("HasteEModule");
        hasteEModuleItem.setCreativeTab(RFTools.tabRfTools);
        hasteEModuleItem.setTextureName(RFTools.MODID + ":envmodules/hasteEModuleItem");
        GameRegistry.registerItem(hasteEModuleItem, "hasteEModuleItem");

        hastePlusEModuleItem = new HastePlusEModuleItem();
        hastePlusEModuleItem.setUnlocalizedName("HastePlusEModule");
        hastePlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        hastePlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/hastePlusEModuleItem");
        GameRegistry.registerItem(hastePlusEModuleItem, "hastePlusEModuleItem");

        saturationEModuleItem = new SaturationEModuleItem();
        saturationEModuleItem.setUnlocalizedName("SaturationEModule");
        saturationEModuleItem.setCreativeTab(RFTools.tabRfTools);
        saturationEModuleItem.setTextureName(RFTools.MODID + ":envmodules/saturationEModuleItem");
        GameRegistry.registerItem(saturationEModuleItem, "saturationEModuleItem");

        saturationPlusEModuleItem = new SaturationPlusEModuleItem();
        saturationPlusEModuleItem.setUnlocalizedName("SaturationPlusEModule");
        saturationPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        saturationPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/saturationPlusEModuleItem");
        GameRegistry.registerItem(saturationPlusEModuleItem, "saturationPlusEModuleItem");

        featherFallingEModuleItem = new FeatherFallingEModuleItem();
        featherFallingEModuleItem.setUnlocalizedName("FeatherFallingEModule");
        featherFallingEModuleItem.setCreativeTab(RFTools.tabRfTools);
        featherFallingEModuleItem.setTextureName(RFTools.MODID + ":envmodules/featherfallingEModuleItem");
        GameRegistry.registerItem(featherFallingEModuleItem, "featherFallingEModuleItem");

        featherFallingPlusEModuleItem = new FeatherFallingPlusEModuleItem();
        featherFallingPlusEModuleItem.setUnlocalizedName("FeatherFallingPlusEModule");
        featherFallingPlusEModuleItem.setCreativeTab(RFTools.tabRfTools);
        featherFallingPlusEModuleItem.setTextureName(RFTools.MODID + ":envmodules/featherfallingPlusEModuleItem");
        GameRegistry.registerItem(featherFallingPlusEModuleItem, "featherFallingPlusEModuleItem");

        flightEModuleItem = new FlightEModuleItem();
        flightEModuleItem.setUnlocalizedName("FlightEModule");
        flightEModuleItem.setCreativeTab(RFTools.tabRfTools);
        flightEModuleItem.setTextureName(RFTools.MODID + ":envmodules/flightEModuleItem");
        GameRegistry.registerItem(flightEModuleItem, "flightEModuleItem");

        peacefulEModuleItem = new PeacefulEModuleItem();
        peacefulEModuleItem.setUnlocalizedName("PeacefulEModule");
        peacefulEModuleItem.setCreativeTab(RFTools.tabRfTools);
        peacefulEModuleItem.setTextureName(RFTools.MODID + ":envmodules/peacefulEModuleItem");
        GameRegistry.registerItem(peacefulEModuleItem, "peacefulEModuleItem");

        waterBreathingEModuleItem = new WaterBreathingEModuleItem();
        waterBreathingEModuleItem.setUnlocalizedName("WaterBreathingEModule");
        waterBreathingEModuleItem.setCreativeTab(RFTools.tabRfTools);
        waterBreathingEModuleItem.setTextureName(RFTools.MODID + ":envmodules/waterBreathingEModuleItem");
        GameRegistry.registerItem(waterBreathingEModuleItem, "waterBreathingEModuleItem");

        nightVisionEModuleItem = new NightVisionEModuleItem();
        nightVisionEModuleItem.setUnlocalizedName("NightVisionEModule");
        nightVisionEModuleItem.setCreativeTab(RFTools.tabRfTools);
        nightVisionEModuleItem.setTextureName(RFTools.MODID + ":envmodules/nightVisionEModuleItem");
        GameRegistry.registerItem(nightVisionEModuleItem, "nightVisionEModuleItem");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(environmentalControllerBlock), "oDo", "GMI", "oEo", 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'D', Blocks.diamond_block, 'E', Blocks.emerald_block, 'G', Blocks.gold_block, 'I', Blocks.iron_block);

        Object inkSac = Item.itemRegistry.getObjectById(351);

        String[] syringeMatcher = new String[] { "level", "mobName" };
        String[] pickMatcher = new String[] { "ench" };

        ItemStack ironGolemSyringe = createMobSyringe("Iron Golem");
        ItemStack ghastSyringe = createMobSyringe("Ghast");
        ItemStack chickenSyringe = createMobSyringe("Chicken");
        ItemStack batSyringe = createMobSyringe("Bat");
        ItemStack horseSyringe = createMobSyringe("Horse");
        ItemStack zombieSyringe = createMobSyringe("Zombie");
        ItemStack squidSyringe = createMobSyringe("Squid");
        ItemStack caveSpiderSyringe = createMobSyringe("Cave Spider");
        ItemStack diamondPick = createEnchantedItem(Items.diamond_pickaxe, Enchantment.efficiency.effectId, 3);
        ItemStack reds = new ItemStack(Items.redstone);
        ItemStack gold = new ItemStack(Items.gold_ingot);
        ItemStack ink = new ItemStack((Item) inkSac);

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, chickenSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(featherFallingEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, ironGolemSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(regenerationEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, horseSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(speedEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3, new ItemStack[] {null, diamondPick, null, reds, gold, reds, null, ink, null},
                new String[][] {null, pickMatcher, null, null, null, null, null, null, null},
                new ItemStack(hasteEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, zombieSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(saturationEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, ghastSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(flightEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, squidSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(waterBreathingEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, caveSpiderSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(nightVisionEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(regenerationEModuleItem), ironGolemSyringe, ironGolemSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(regenerationPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(speedEModuleItem), horseSyringe, horseSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(speedPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(hasteEModuleItem), diamondPick, null, null},
                new String[][] {null, pickMatcher, null, null},
                new ItemStack(hastePlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(saturationEModuleItem), zombieSyringe, zombieSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(saturationPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(featherFallingEModuleItem), chickenSyringe, batSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(featherFallingPlusEModuleItem)));

        GameRegistry.addRecipe(new ItemStack(peacefulEModuleItem, 1), " p ", "rgr", " i ", 'p', DimletConstructionSetup.peaceEssenceItem,
                'r', reds, 'g', gold, 'i', ink);
    }

    public static ItemStack createEnchantedItem(Item item, int effectId, int amount) {
        ItemStack stack = new ItemStack(item);
        Map enchant = new HashMap();
        enchant.put(effectId, amount);
        EnchantmentHelper.setEnchantments(enchant, stack);
        return stack;
    }

    public static ItemStack createMobSyringe(String mobName) {
        ItemStack syringe = new ItemStack(DimletConstructionSetup.syringeItem);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("mobName", mobName);
        tagCompound.setInteger("level", DimletConstructionConfiguration.maxMobInjections);
        syringe.setTagCompound(tagCompound);
        return syringe;
    }
}
