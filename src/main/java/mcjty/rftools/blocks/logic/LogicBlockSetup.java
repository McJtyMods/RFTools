package mcjty.rftools.blocks.logic;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.logic.analog.AnalogBlock;
import mcjty.rftools.blocks.logic.counter.CounterBlock;
import mcjty.rftools.blocks.logic.digit.DigitTileEntity;
import mcjty.rftools.blocks.logic.invchecker.InvCheckerBlock;
import mcjty.rftools.blocks.logic.sensor.SensorBlock;
import mcjty.rftools.blocks.logic.sequencer.SequencerBlock;
import mcjty.rftools.blocks.logic.threelogic.ThreeLogicBlock;
import mcjty.rftools.blocks.logic.timer.GuiTimer;
import mcjty.rftools.blocks.logic.timer.TimerTileEntity;
import mcjty.rftools.blocks.logic.wire.WireTileEntity;
import mcjty.rftools.blocks.logic.wireless.RedstoneReceiverBlock;
import mcjty.rftools.blocks.logic.wireless.RedstoneTransmitterBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogicBlockSetup {
    public static SequencerBlock sequencerBlock;
    public static CounterBlock counterBlock;
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;
    public static ThreeLogicBlock threeLogicBlock;
    public static InvCheckerBlock invCheckerBlock;
    public static SensorBlock sensorBlock;
    public static AnalogBlock analogBlock;

    public static GenericBlock<DigitTileEntity, GenericContainer> digitBlock;
    public static GenericBlock<WireTileEntity, GenericContainer> wireBlock;
    public static GenericBlock<TimerTileEntity, GenericContainer> timerBlock;

    public static void init() {
        sequencerBlock = new SequencerBlock();
        counterBlock = new CounterBlock();
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();
        threeLogicBlock = new ThreeLogicBlock();
        invCheckerBlock = new InvCheckerBlock();
        sensorBlock = new SensorBlock();
        analogBlock = new AnalogBlock();

        digitBlock = ModBlocks.logicFactory.<DigitTileEntity> builder("digit_block")
                .tileEntityClass(DigitTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE,
                        BlockFlags.RENDER_CUTOUT, BlockFlags.RENDER_SOLID)
                .property(DigitTileEntity.VALUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.digit")
                .build();
        wireBlock = ModBlocks.logicFactory.<WireTileEntity> builder("wire_block")
                .tileEntityClass(WireTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.wire")
                .build();
        timerBlock = ModBlocks.logicFactory.<TimerTileEntity> builder("timer_block")
                .tileEntityClass(TimerTileEntity.class)
                .guiId(RFTools.GUI_TIMER)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.timer")
                .infoExtendedParameter(ItemStackTools.intGetter("delay", 0))
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        sequencerBlock.initModel();

        timerBlock.initModel();
        timerBlock.setGuiClass(GuiTimer.class);

        counterBlock.initModel();
        redstoneTransmitterBlock.initModel();
        redstoneReceiverBlock.initModel();
        threeLogicBlock.initModel();
        invCheckerBlock.initModel();
        sensorBlock.initModel();
        wireBlock.initModel();
        analogBlock.initModel();
        digitBlock.initModel();
    }
}
