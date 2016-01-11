package mcjty.rftools.blocks.logic;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogicBlockSetup {
    public static SequencerBlock sequencerBlock;
    public static TimerBlock timerBlock;
    public static CounterBlock counterBlock;
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;

    public static void init() {
        sequencerBlock = new SequencerBlock();
        timerBlock = new TimerBlock();
        counterBlock = new CounterBlock();
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        sequencerBlock.initModel();
        timerBlock.initModel();
        counterBlock.initModel();
        redstoneTransmitterBlock.initModel();
        redstoneReceiverBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.redstone_torch;
        GameRegistry.addRecipe(new ItemStack(sequencerBlock), "rTr", "TMT", "rTr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(counterBlock), "gcg", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'g', Items.gold_nugget);
        GameRegistry.addRecipe(new ItemStack(timerBlock), "rcr", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneReceiverBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', Items.comparator, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "r", 'r', redstoneTransmitterBlock);    // To clear it
    }
}
