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
    public static ThreeLogicBlock threeLogicBlock;

    public static void init() {
        sequencerBlock = new SequencerBlock();
        timerBlock = new TimerBlock();
        counterBlock = new CounterBlock();
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();
        threeLogicBlock = new ThreeLogicBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        sequencerBlock.initModel();
        timerBlock.initModel();
        counterBlock.initModel();
        redstoneTransmitterBlock.initModel();
        redstoneReceiverBlock.initModel();
        threeLogicBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;
        GameRegistry.addRecipe(new ItemStack(sequencerBlock), "rTr", "TMT", "rTr", 'r', Items.REDSTONE, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(counterBlock), "gcg", "TMT", "rTr", 'c', Items.CLOCK, 'r', Items.REDSTONE, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'g', Items.GOLD_NUGGET);
        GameRegistry.addRecipe(new ItemStack(timerBlock), "rcr", "TMT", "rTr", 'c', Items.CLOCK, 'r', Items.REDSTONE, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(threeLogicBlock), "rcr", "cMc", "rcr", 'c', Items.COMPARATOR, 'r', Items.REDSTONE, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "ror", "TMT", "rRr", 'o', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'T', redstoneTorch, 'R', Blocks.REDSTONE_BLOCK, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneReceiverBlock), "ror", "TMT", "rRr", 'o', Items.ENDER_PEARL, 'r', Items.REDSTONE, 'T', Items.COMPARATOR, 'R', Blocks.REDSTONE_BLOCK, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "r", 'r', redstoneTransmitterBlock);    // To clear it
    }
}
