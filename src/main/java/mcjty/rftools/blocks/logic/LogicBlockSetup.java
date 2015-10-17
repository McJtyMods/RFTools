package mcjty.rftools.blocks.logic;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LogicBlockSetup {
    public static SequencerBlock sequencerBlock;
    public static TimerBlock timerBlock;
    public static CounterBlock counterBlock;
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;

    public static void setupBlocks() {
        sequencerBlock = new SequencerBlock();
        GameRegistry.registerBlock(sequencerBlock, GenericItemBlock.class, "sequencerBlock");
        GameRegistry.registerTileEntity(SequencerTileEntity.class, "SequencerTileEntity");

        timerBlock = new TimerBlock();
        GameRegistry.registerBlock(timerBlock, GenericItemBlock.class, "timerBlock");
        GameRegistry.registerTileEntity(TimerTileEntity.class, "TimerTileEntity");

        counterBlock = new CounterBlock();
        GameRegistry.registerBlock(counterBlock, GenericItemBlock.class, "counterBlock");
        GameRegistry.registerTileEntity(CounterTileEntity.class, "CounterTileEntity");

        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        GameRegistry.registerBlock(redstoneTransmitterBlock, RedstoneReceiverItemBlock.class, "redstoneTransmitterBlock");
        GameRegistry.registerTileEntity(RedstoneTransmitterTileEntity.class, "RedstoneTransmitterTileEntity");

        redstoneReceiverBlock = new RedstoneReceiverBlock();
        GameRegistry.registerBlock(redstoneReceiverBlock, RedstoneReceiverItemBlock.class, "redstoneReceiverBlock");
        GameRegistry.registerTileEntity(RedstoneReceiverTileEntity.class, "RedstoneReceiverTileEntity");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(sequencerBlock), "rTr", "TMT", "rTr", 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(counterBlock), "gcg", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase, 'g', Items.gold_nugget);
        GameRegistry.addRecipe(new ItemStack(timerBlock), "rcr", "TMT", "rTr", 'c', Items.clock, 'r', Items.redstone, 'T', redstoneTorch, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', redstoneTorch, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneReceiverBlock), "ror", "TMT", "rRr", 'o', Items.ender_pearl, 'r', Items.redstone, 'T', Items.comparator, 'R', Blocks.redstone_block, 'M', ModBlocks.machineBase);
        GameRegistry.addRecipe(new ItemStack(redstoneTransmitterBlock), "r", 'r', redstoneTransmitterBlock);    // To clear it
    }
}
