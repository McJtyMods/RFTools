package mcjty.rftools.blocks.environmental;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import mcjty.rftools.crafting.NBTMatchingRecipe;
import mcjty.rftools.items.ModItems;
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

    public static void setupBlocks() {
        environmentalControllerBlock = new EnvironmentalControllerBlock();
        GameRegistry.registerBlock(environmentalControllerBlock, GenericItemBlock.class, "environmentalControllerBlock");
        GameRegistry.registerTileEntity(EnvironmentalControllerTileEntity.class, "EnvironmentalControllerTileEntity");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(environmentalControllerBlock), "oDo", "GMI", "oEo", 'o', Items.ender_pearl, 'M', ModBlocks.machineFrame,
                'D', Blocks.diamond_block, 'E', Blocks.emerald_block, 'G', Blocks.gold_block, 'I', Blocks.iron_block);
    }

    public static void initEnvModuleCrafting() {
        Object inkSac = Item.itemRegistry.getObjectById(351);

        String[] syringeMatcher = new String[] { "level", "mobName" };
        String[] pickMatcher = new String[] { "ench" };

        ItemStack ironGolemSyringe = createMobSyringe("Iron Golem");
        ItemStack ghastSyringe = createMobSyringe("Ghast");
        ItemStack chickenSyringe = createMobSyringe("Chicken");
        ItemStack batSyringe = createMobSyringe("Bat");
        ItemStack horseSyringe = createMobSyringe("Horse");
        ItemStack zombieSyringe = createMobSyringe("Zombie");
        ItemStack diamondPick = createEnchantedItem(Items.diamond_pickaxe, Enchantment.efficiency.effectId, 3);
        ItemStack reds = new ItemStack(Items.redstone);
        ItemStack gold = new ItemStack(Items.gold_ingot);
        ItemStack ink = new ItemStack((Item) inkSac);

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, chickenSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.featherFallingEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, ironGolemSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.regenerationEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, horseSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.speedEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3, new ItemStack[] {null, diamondPick, null, reds, gold, reds, null, ink, null},
                new String[][] {null, pickMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.hasteEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, zombieSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.saturationEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(3, 3,
                new ItemStack[] {null, ghastSyringe, null, reds, gold, reds, null, ink, null},
                new String[][] {null, syringeMatcher, null, null, null, null, null, null, null},
                new ItemStack(ModItems.flightEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(ModItems.regenerationEModuleItem), ironGolemSyringe, ironGolemSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(ModItems.regenerationPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(ModItems.speedEModuleItem), horseSyringe, horseSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(ModItems.speedPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(ModItems.hasteEModuleItem), diamondPick, null, null},
                new String[][] {null, pickMatcher, null, null},
                new ItemStack(ModItems.hastePlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(ModItems.saturationEModuleItem), zombieSyringe, zombieSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(ModItems.saturationPlusEModuleItem)));

        GameRegistry.addRecipe(new NBTMatchingRecipe(2, 2,
                new ItemStack[]{new ItemStack(ModItems.featherFallingEModuleItem), chickenSyringe, batSyringe, null},
                new String[][] {null, syringeMatcher, syringeMatcher, null},
                new ItemStack(ModItems.featherFallingPlusEModuleItem)));

        GameRegistry.addRecipe(new ItemStack(ModItems.peacefulEModuleItem, 1), " p ", "rgr", " i ", 'p', ModItems.peaceEssenceItem,
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
        ItemStack syringe = new ItemStack(ModItems.syringeItem);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("mobName", mobName);
        tagCompound.setInteger("level", DimletConstructionConfiguration.maxMobInjections);
        syringe.setTagCompound(tagCompound);
        return syringe;
    }
}
