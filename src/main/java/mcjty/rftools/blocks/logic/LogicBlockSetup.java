package mcjty.rftools.blocks.logic;

import mcjty.rftools.blocks.logic.counter.CounterBlock;
import mcjty.rftools.blocks.logic.invchecker.InvCheckerBlock;
import mcjty.rftools.blocks.logic.sensor.SensorBlock;
import mcjty.rftools.blocks.logic.sequencer.SequencerBlock;
import mcjty.rftools.blocks.logic.threelogic.ThreeLogicBlock;
import mcjty.rftools.blocks.logic.timer.TimerBlock;
import mcjty.rftools.blocks.logic.wire.WireBlock;
import mcjty.rftools.blocks.logic.wireless.RedstoneReceiverBlock;
import mcjty.rftools.blocks.logic.wireless.RedstoneTransmitterBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogicBlockSetup {
    public static SequencerBlock sequencerBlock;
    public static TimerBlock timerBlock;
    public static CounterBlock counterBlock;
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;
    public static ThreeLogicBlock threeLogicBlock;
    public static InvCheckerBlock invCheckerBlock;
    public static SensorBlock sensorBlock;
    public static WireBlock wireBlock;

    public static void init() {
        sequencerBlock = new SequencerBlock();
        timerBlock = new TimerBlock();
        counterBlock = new CounterBlock();
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();
        threeLogicBlock = new ThreeLogicBlock();
        invCheckerBlock = new InvCheckerBlock();
        sensorBlock = new SensorBlock();
        wireBlock = new WireBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        sequencerBlock.initModel();
        timerBlock.initModel();
        counterBlock.initModel();
        redstoneTransmitterBlock.initModel();
        redstoneReceiverBlock.initModel();
        threeLogicBlock.initModel();
        invCheckerBlock.initModel();
        sensorBlock.initModel();
        wireBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

    }
}
