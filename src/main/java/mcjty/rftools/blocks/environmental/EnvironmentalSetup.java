package mcjty.rftools.blocks.environmental;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.NBTMatchingRecipe;
import mcjty.rftools.proxy.GuiProxy;
import mcjty.rftools.items.SyringeItem;
import mcjty.rftools.items.envmodules.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentalSetup {
    public static GenericBlock<EnvironmentalControllerTileEntity, GenericContainer> environmentalControllerBlock;

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
    public static GlowingEModuleItem glowingEModuleItem;
    public static LuckEModuleItem luckEModuleItem;
    public static NoTeleportEModuleItem noTeleportEModuleItem;

    public static BlindnessEModuleItem blindnessEModuleItem;
    public static WeaknessEModuleItem weaknessEModuleItem;
    public static PoisonEModuleItem poisonEModuleItem;
    public static SlownessEModuleItem slownessEModuleItem;

    public static void init() {

        environmentalControllerBlock = ModBlocks.builderFactory.<EnvironmentalControllerTileEntity> builder("environmental_controller")
                .tileEntityClass(EnvironmentalControllerTileEntity.class)
                .container(EnvironmentalControllerTileEntity.CONTAINER_FACTORY)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.NON_OPAQUE, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .lightValue(13)
                .infusable()
                .rotationType(BaseBlock.RotationType.NONE)
                .moduleSupport(EnvironmentalControllerTileEntity.MODULE_SUPPORT)
                .guiId(GuiProxy.GUI_ENVIRONMENTAL_CONTROLLER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.environmental_controller")
                .infoExtendedParameter(ItemStackTools.intGetter("radius", 0))
                .infoExtendedParameter(ItemStackTools.intGetter("miny", 0))
                .infoExtendedParameter(ItemStackTools.intGetter("maxy", 0))
                .build();

        regenerationEModuleItem = new RegenerationEModuleItem();
        regenerationPlusEModuleItem = new RegenerationPlusEModuleItem();
        speedEModuleItem = new SpeedEModuleItem();
        speedPlusEModuleItem = new SpeedPlusEModuleItem();
        hasteEModuleItem = new HasteEModuleItem();
        hastePlusEModuleItem = new HastePlusEModuleItem();
        saturationEModuleItem = new SaturationEModuleItem();
        saturationPlusEModuleItem = new SaturationPlusEModuleItem();
        featherFallingEModuleItem = new FeatherFallingEModuleItem();
        featherFallingPlusEModuleItem = new FeatherFallingPlusEModuleItem();
        flightEModuleItem = new FlightEModuleItem();
        peacefulEModuleItem = new PeacefulEModuleItem();
        waterBreathingEModuleItem = new WaterBreathingEModuleItem();
        nightVisionEModuleItem = new NightVisionEModuleItem();
        blindnessEModuleItem = new BlindnessEModuleItem();
        weaknessEModuleItem = new WeaknessEModuleItem();
        poisonEModuleItem = new PoisonEModuleItem();
        slownessEModuleItem = new SlownessEModuleItem();
        glowingEModuleItem = new GlowingEModuleItem();
        luckEModuleItem = new LuckEModuleItem();
        noTeleportEModuleItem = new NoTeleportEModuleItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {

        environmentalControllerBlock.initModel();
        environmentalControllerBlock.setGuiFactory(GuiEnvironmentalController::new);
        EnvironmentalTESR.register();

        regenerationEModuleItem.initModel();
        regenerationPlusEModuleItem.initModel();
        speedEModuleItem.initModel();
        speedPlusEModuleItem.initModel();
        hasteEModuleItem.initModel();
        hastePlusEModuleItem.initModel();
        saturationEModuleItem.initModel();
        saturationPlusEModuleItem.initModel();
        featherFallingEModuleItem.initModel();
        featherFallingPlusEModuleItem.initModel();
        flightEModuleItem.initModel();
        peacefulEModuleItem.initModel();
        waterBreathingEModuleItem.initModel();
        nightVisionEModuleItem.initModel();
        blindnessEModuleItem.initModel();
        weaknessEModuleItem.initModel();
        poisonEModuleItem.initModel();
        slownessEModuleItem.initModel();
        glowingEModuleItem.initModel();
        luckEModuleItem.initModel();
        noTeleportEModuleItem.initModel();
    }

    public static void initCrafting() {

        Object inkSac = Item.REGISTRY.getObjectById(351);

        String[] syringeMatcher = new String[]{"level", "mobId"};
        String[] pickMatcher = new String[]{"ench"};

        ItemStack ironGolemSyringe = SyringeItem.createMobSyringe(EntityIronGolem.class);
        ItemStack endermanSyringe = SyringeItem.createMobSyringe(EntityEnderman.class);
        ItemStack ghastSyringe = SyringeItem.createMobSyringe(EntityGhast.class);
        ItemStack chickenSyringe = SyringeItem.createMobSyringe(EntityChicken.class);
        ItemStack batSyringe = SyringeItem.createMobSyringe(EntityBat.class);
        ItemStack horseSyringe = SyringeItem.createMobSyringe(EntityHorse.class);
        ItemStack zombieSyringe = SyringeItem.createMobSyringe(EntityZombie.class);
        ItemStack squidSyringe = SyringeItem.createMobSyringe(EntitySquid.class);
        ItemStack guardianSyringe = SyringeItem.createMobSyringe(EntityGuardian.class);
        ItemStack caveSpiderSyringe = SyringeItem.createMobSyringe(EntityCaveSpider.class);
        ItemStack blazeSyringe = SyringeItem.createMobSyringe(EntityBlaze.class);
        ItemStack shulkerEntity = SyringeItem.createMobSyringe(EntityShulker.class);
        ItemStack diamondPick = createEnchantedItem(Items.DIAMOND_PICKAXE, Enchantment.REGISTRY.getObject(new ResourceLocation("efficiency")), 3);
        ItemStack reds = new ItemStack(Items.REDSTONE);
        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        ItemStack ink = new ItemStack((Item) inkSac);
        ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, chickenSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(featherFallingEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "featherfalling_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, ironGolemSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(regenerationEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "regeneration_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, horseSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(speedEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "speed_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3, new ItemStack[]{ItemStack.EMPTY, diamondPick, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, pickMatcher, null, null, null, null, null, null, null},
                        new ItemStack(hasteEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "haste_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, zombieSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(saturationEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "saturation_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, ghastSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(flightEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "flight_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, guardianSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(waterBreathingEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "waterbreathing_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, caveSpiderSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(nightVisionEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "nightvision_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(2, 2,
                        new ItemStack[]{new ItemStack(regenerationEModuleItem), ironGolemSyringe, ironGolemSyringe, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, syringeMatcher, null},
                        new ItemStack(regenerationPlusEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "regenerationplus_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(2, 2,
                        new ItemStack[]{new ItemStack(speedEModuleItem), horseSyringe, horseSyringe, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, syringeMatcher, null},
                        new ItemStack(speedPlusEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "speedplus_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(2, 2,
                        new ItemStack[]{new ItemStack(hasteEModuleItem), diamondPick, ItemStack.EMPTY, ItemStack.EMPTY},
                        new String[][]{null, pickMatcher, null, null},
                        new ItemStack(hastePlusEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "hasteplus_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(2, 2,
                        new ItemStack[]{new ItemStack(saturationEModuleItem), zombieSyringe, zombieSyringe, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, syringeMatcher, null},
                        new ItemStack(saturationPlusEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "saturationplus_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(2, 2,
                        new ItemStack[]{new ItemStack(featherFallingEModuleItem), chickenSyringe, batSyringe, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, syringeMatcher, null},
                        new ItemStack(featherFallingPlusEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "featherfallingplus_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, blazeSyringe, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(glowingEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "glowing_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, shulkerEntity, ItemStack.EMPTY, reds, gold, reds, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(luckEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "luck_module")));


        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, squidSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(blindnessEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "blindness_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, batSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(weaknessEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "weakness_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, caveSpiderSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(poisonEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "poison_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, new ItemStack(Items.CLOCK), ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, null, null, null, null, null, null, null, null},
                        new ItemStack(slownessEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "slowness_module")));

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{ItemStack.EMPTY, endermanSyringe, ItemStack.EMPTY, lapis, obsidian, lapis, ItemStack.EMPTY, ink, ItemStack.EMPTY},
                        new String[][]{null, syringeMatcher, null, null, null, null, null, null, null},
                        new ItemStack(noTeleportEModuleItem))
                        .setRegistryName(new ResourceLocation(RFTools.MODID, "noteleport_module")));
    }

    public static ItemStack createEnchantedItem(Item item, Enchantment effectId, int amount) {
        ItemStack stack = new ItemStack(item);
        Map<Enchantment, Integer> enchant = new HashMap<>();
        enchant.put(effectId, amount);
        EnchantmentHelper.setEnchantments(enchant, stack);
        return stack;
    }

}
