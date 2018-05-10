package mcjty.rftools.blocks.logic;

import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.logic.analog.AnalogTileEntity;
import mcjty.rftools.blocks.logic.analog.GuiAnalog;
import mcjty.rftools.blocks.logic.counter.CounterBlock;
import mcjty.rftools.blocks.logic.digit.DigitTileEntity;
import mcjty.rftools.blocks.logic.invchecker.GuiInvChecker;
import mcjty.rftools.blocks.logic.invchecker.InvCheckerTileEntity;
import mcjty.rftools.blocks.logic.sensor.GuiSensor;
import mcjty.rftools.blocks.logic.sensor.SensorTileEntity;
import mcjty.rftools.blocks.logic.sequencer.SequencerBlock;
import mcjty.rftools.blocks.logic.threelogic.GuiThreeLogic;
import mcjty.rftools.blocks.logic.threelogic.ThreeLogicTileEntity;
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

    public static GenericBlock<ThreeLogicTileEntity, GenericContainer> threeLogicBlock;
    public static GenericBlock<InvCheckerTileEntity, GenericContainer> invCheckerBlock;
    public static GenericBlock<SensorTileEntity, GenericContainer> sensorBlock;
    public static GenericBlock<AnalogTileEntity, GenericContainer> analogBlock;
    public static GenericBlock<DigitTileEntity, GenericContainer> digitBlock;
    public static GenericBlock<WireTileEntity, GenericContainer> wireBlock;
    public static GenericBlock<TimerTileEntity, GenericContainer> timerBlock;

    public static void init() {
        sequencerBlock = new SequencerBlock();
        counterBlock = new CounterBlock();
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();

        threeLogicBlock = ModBlocks.logicFactory.<ThreeLogicTileEntity> builder("logic_block")
                .tileEntityClass(ThreeLogicTileEntity.class)
                .guiId(RFTools.GUI_THREE_LOGIC)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.threelogic")
                .build();
        invCheckerBlock = ModBlocks.logicFactory.<InvCheckerTileEntity> builder("invchecker_block")
                .tileEntityClass(InvCheckerTileEntity.class)
                .guiId(RFTools.GUI_INVCHECKER)
                .container(InvCheckerTileEntity.CONTAINER_FACTORY)
                .flags(BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.invchecker")
                .build();
        sensorBlock = ModBlocks.logicFactory.<SensorTileEntity> builder("sensor_block")
                .tileEntityClass(SensorTileEntity.class)
                .guiId(RFTools.GUI_SENSOR)
                .container(SensorTileEntity.CONTAINER_FACTORY)
                .flags(BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.sensor")
                .build();
        analogBlock = ModBlocks.logicFactory.<AnalogTileEntity> builder("analog_block")
                .tileEntityClass(AnalogTileEntity.class)
                .guiId(RFTools.GUI_ANALOG)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.analog")
                .build();
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
        threeLogicBlock.setGuiClass(GuiThreeLogic.class);

        invCheckerBlock.initModel();
        invCheckerBlock.setGuiClass(GuiInvChecker.class);

        sensorBlock.initModel();
        sensorBlock.setGuiClass(GuiSensor.class);

        wireBlock.initModel();

        analogBlock.initModel();
        analogBlock.setGuiClass(GuiAnalog.class);

        digitBlock.initModel();
    }
}
