package mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.teleportprobe.AdvancedChargedPorterItem;
import mcjty.rftools.items.teleportprobe.ChargedPorterItem;
import mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TeleporterSetup {
    public static MatterTransmitterBlock matterTransmitterBlock;
    public static MatterReceiverBlock matterReceiverBlock;
    public static DialingDeviceBlock dialingDeviceBlock;
    public static DestinationAnalyzerBlock destinationAnalyzerBlock;
    public static MatterBoosterBlock matterBoosterBlock;
    public static SimpleDialerBlock simpleDialerBlock;

    public static TeleportProbeItem teleportProbeItem;
    public static ChargedPorterItem chargedPorterItem;
    public static AdvancedChargedPorterItem advancedChargedPorterItem;

    public static void setupBlocks() {
        matterTransmitterBlock = new MatterTransmitterBlock();
        GameRegistry.registerBlock(matterTransmitterBlock, GenericItemBlock.class, "matterTransmitterBlock");
        GameRegistry.registerTileEntity(MatterTransmitterTileEntity.class, "MatterTransmitterTileEntity");

        matterReceiverBlock = new MatterReceiverBlock();
        GameRegistry.registerBlock(matterReceiverBlock, GenericItemBlock.class, "matterReceiverBlock");
        GameRegistry.registerTileEntity(MatterReceiverTileEntity.class, "MatterReceiverTileEntity");

        dialingDeviceBlock = new DialingDeviceBlock();
        GameRegistry.registerBlock(dialingDeviceBlock, GenericItemBlock.class, "dialingDeviceBlock");
        GameRegistry.registerTileEntity(DialingDeviceTileEntity.class, "DialingDeviceTileEntity");

        simpleDialerBlock = new SimpleDialerBlock();
        GameRegistry.registerBlock(simpleDialerBlock, SimpleDialerItemBlock.class, "simpleDialerBlock");
        GameRegistry.registerTileEntity(SimpleDialerTileEntity.class, "SimpleDialerTileEntity");

        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");

        matterBoosterBlock = new MatterBoosterBlock();
        GameRegistry.registerBlock(matterBoosterBlock, "matterBoosterBlock");
    }

    public static void setupItems() {
        teleportProbeItem = new TeleportProbeItem();
        teleportProbeItem.setUnlocalizedName("TeleportProbe");
        teleportProbeItem.setCreativeTab(RFTools.tabRfTools);
        teleportProbeItem.setTextureName(RFTools.MODID + ":teleportProbeItem");
        GameRegistry.registerItem(teleportProbeItem, "teleportProbeItem");

        chargedPorterItem = new ChargedPorterItem();
        chargedPorterItem.setUnlocalizedName("ChargedPorter");
        chargedPorterItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(chargedPorterItem, "chargedPorterItem");

        advancedChargedPorterItem = new AdvancedChargedPorterItem();
        advancedChargedPorterItem.setUnlocalizedName("AdvancedChargedPorter");
        advancedChargedPorterItem.setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(advancedChargedPorterItem, "advancedChargedPorterItem");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");

        if (GeneralConfiguration.enableMatterTransmitterRecipe) {
            GameRegistry.addRecipe(new ItemStack(matterTransmitterBlock), "ooo", "rMr", "iii", 'M', ModBlocks.machineFrame,
                    'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        }
        if (GeneralConfiguration.enableMatterReceiverRecipe) {
            GameRegistry.addRecipe(new ItemStack(matterReceiverBlock), "iii", "rMr", "ooo", 'M', ModBlocks.machineFrame,
                    'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        }
        if (GeneralConfiguration.enableDialingDeviceRecipe) {
            GameRegistry.addRecipe(new ItemStack(dialingDeviceBlock), "rrr", "TMT", "rrr", 'M', ModBlocks.machineFrame, 'r', Items.redstone,
                    'T', redstoneTorch);
        }
        GameRegistry.addRecipe(new ItemStack(destinationAnalyzerBlock), "o o", " M ", "o o", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(matterBoosterBlock), " B ", "BMB", " B ", 'M', destinationAnalyzerBlock,
                'B', Blocks.redstone_block);
        GameRegistry.addRecipe(new ItemStack(chargedPorterItem), " e ", "eRe", "iei", 'e', Items.ender_pearl, 'R', Blocks.redstone_block, 'i', Items.iron_ingot);

        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(Blocks.redstone_block), new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Blocks.redstone_block),
                new ItemStack(DimletSetup.dimensionalShard), new ItemStack(chargedPorterItem), new ItemStack(DimletSetup.dimensionalShard),
                new ItemStack(Blocks.redstone_block), new ItemStack(DimletSetup.dimensionalShard), new ItemStack(Blocks.redstone_block)
        }, new ItemStack(advancedChargedPorterItem), 4));

        if (GeneralConfiguration.enableDialingDeviceRecipe) {
            GameRegistry.addRecipe(new ItemStack(simpleDialerBlock), "rRr", "TMT", "rRr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'R', Blocks.redstone_block);
        }
    }
}
