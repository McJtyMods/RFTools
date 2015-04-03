package mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.items.ModItems;
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

        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        GameRegistry.registerBlock(destinationAnalyzerBlock, "destinationAnalyzerBlock");

        matterBoosterBlock = new MatterBoosterBlock();
        GameRegistry.registerBlock(matterBoosterBlock, "matterBoosterBlock");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(matterTransmitterBlock), "ooo", "rMr", "iii", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(matterReceiverBlock), "iii", "rMr", "ooo", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl, 'r', Items.redstone, 'i', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(dialingDeviceBlock), "rrr", "TMT", "rrr", 'M', ModBlocks.machineFrame, 'r', Items.redstone,
                'T', redstoneTorch);
        GameRegistry.addRecipe(new ItemStack(destinationAnalyzerBlock), "o o", " M ", "o o", 'M', ModBlocks.machineFrame,
                'o', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(matterBoosterBlock), " B ", "BMB", " B ", 'M', destinationAnalyzerBlock,
                'B', Blocks.redstone_block);
        GameRegistry.addRecipe(new ItemStack(ModItems.chargedPorterItem), " e ", "eRe", "iei", 'e', Items.ender_pearl, 'R', Blocks.redstone_block, 'i', Items.iron_ingot);
    }
}
