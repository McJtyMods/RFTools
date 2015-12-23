package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.crafting.PreservingShapedRecipe;
import mcjty.rftools.items.teleportprobe.AdvancedChargedPorterItem;
import mcjty.rftools.items.teleportprobe.ChargedPorterItem;
import mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    public static void init() {
        matterTransmitterBlock = new MatterTransmitterBlock();
        matterReceiverBlock = new MatterReceiverBlock();
        dialingDeviceBlock = new DialingDeviceBlock();
//        simpleDialerBlock = new SimpleDialerBlock();
        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        matterBoosterBlock = new MatterBoosterBlock();

        teleportProbeItem = new TeleportProbeItem();
        chargedPorterItem = new ChargedPorterItem();
        advancedChargedPorterItem = new AdvancedChargedPorterItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        matterTransmitterBlock.initModel();
        matterReceiverBlock.initModel();
        dialingDeviceBlock.initModel();
        destinationAnalyzerBlock.initModel();
        matterBoosterBlock.initModel();

        teleportProbeItem.initModel();
        chargedPorterItem.initModel();
        advancedChargedPorterItem.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.redstone_torch;

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

        // @todo recipe should change if rftools_dimension is present
//        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
//                new ItemStack(Blocks.redstone_block), new ItemStack(DimletSetup.infusedDiamond), new ItemStack(Blocks.redstone_block),
//                new ItemStack(DimletSetup.infusedDiamond), new ItemStack(chargedPorterItem), new ItemStack(DimletSetup.infusedDiamond),
//                new ItemStack(Blocks.redstone_block), new ItemStack(DimletSetup.infusedDiamond), new ItemStack(Blocks.redstone_block)
//        }, new ItemStack(advancedChargedPorterItem), 4));
        GameRegistry.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
                new ItemStack(Blocks.redstone_block), new ItemStack(Items.diamond), new ItemStack(Blocks.redstone_block),
                new ItemStack(Items.diamond), new ItemStack(chargedPorterItem), new ItemStack(Items.diamond),
                new ItemStack(Blocks.redstone_block), new ItemStack(Items.diamond), new ItemStack(Blocks.redstone_block)
        }, new ItemStack(advancedChargedPorterItem), 4));

//        if (GeneralConfiguration.enableDialingDeviceRecipe) {
//            GameRegistry.addRecipe(new ItemStack(simpleDialerBlock), "rRr", "TMT", "rRr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'R', Blocks.redstone_block);
//        }
    }
}
