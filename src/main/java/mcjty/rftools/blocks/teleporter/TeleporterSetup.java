package mcjty.rftools.blocks.teleporter;

import mcjty.lib.compat.MyGameReg;
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
        simpleDialerBlock = new SimpleDialerBlock();
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
        simpleDialerBlock.initModel();

        teleportProbeItem.initModel();
        chargedPorterItem.initModel();
        advancedChargedPorterItem.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

        if (GeneralConfiguration.enableMatterTransmitterRecipe) {
            MyGameReg.addRecipe(new ItemStack(matterTransmitterBlock), "ooo", "rMr", "iii", 'M', ModBlocks.machineFrame,
                    'o', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);
        }
        if (GeneralConfiguration.enableMatterReceiverRecipe) {
            MyGameReg.addRecipe(new ItemStack(matterReceiverBlock), "iii", "rMr", "ooo", 'M', ModBlocks.machineFrame,
                    'o', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'i', Items.IRON_INGOT);
        }
        if (GeneralConfiguration.enableDialingDeviceRecipe) {
            MyGameReg.addRecipe(new ItemStack(dialingDeviceBlock), "rrr", "TMT", "rrr", 'M', ModBlocks.machineFrame, 'r', Items.REDSTONE,
                    'T', redstoneTorch);
        }
        MyGameReg.addRecipe(new ItemStack(destinationAnalyzerBlock), "o o", " M ", "o o", 'M', ModBlocks.machineFrame,
                'o', Items.ENDER_PEARL);
        MyGameReg.addRecipe(new ItemStack(matterBoosterBlock), " B ", "BMB", " B ", 'M', destinationAnalyzerBlock,
                'B', Blocks.REDSTONE_BLOCK);
        MyGameReg.addRecipe(new ItemStack(chargedPorterItem), " e ", "eRe", "iei", 'e', Items.ENDER_PEARL, 'R', Blocks.REDSTONE_BLOCK, 'i', Items.IRON_INGOT);

        // @todo recipes
//        MyGameReg.addRecipe(new PreservingShapedRecipe(3, 3, new ItemStack[]{
//                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Items.DIAMOND), new ItemStack(Blocks.REDSTONE_BLOCK),
//                new ItemStack(Items.DIAMOND), new ItemStack(chargedPorterItem), new ItemStack(Items.DIAMOND),
//                new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Items.DIAMOND), new ItemStack(Blocks.REDSTONE_BLOCK)
//        }, new ItemStack(advancedChargedPorterItem), 4));

        if (GeneralConfiguration.enableDialingDeviceRecipe) {
            MyGameReg.addRecipe(new ItemStack(simpleDialerBlock), "rRr", "TMT", "rRr", 'r', Items.REDSTONE, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'R', Blocks.REDSTONE_BLOCK);
        }
    }
}
