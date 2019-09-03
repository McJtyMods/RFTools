package mcjty.rftools.blocks.logic;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.blocks.logic.analog.AnalogTileEntity;
import mcjty.rftools.blocks.logic.counter.CounterTileEntity;
import mcjty.rftools.blocks.logic.digit.DigitTileEntity;
import mcjty.rftools.blocks.logic.invchecker.InvCheckerTileEntity;
import mcjty.rftools.blocks.logic.sensor.SensorTileEntity;
import mcjty.rftools.blocks.logic.sequencer.SequencerMode;
import mcjty.rftools.blocks.logic.sequencer.SequencerTileEntity;
import mcjty.rftools.blocks.logic.threelogic.ThreeLogicTileEntity;
import mcjty.rftools.blocks.logic.timer.TimerTileEntity;
import mcjty.rftools.blocks.logic.wire.WireTileEntity;
import mcjty.rftools.blocks.logic.wireless.RedstoneReceiverBlock;
import mcjty.rftools.blocks.logic.wireless.RedstoneTransmitterBlock;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class LogicBlockSetup {
    public static RedstoneTransmitterBlock redstoneTransmitterBlock;
    public static RedstoneReceiverBlock redstoneReceiverBlock;

    @ObjectHolder("rftools:sequencer_block")
    public static BaseBlock sequencerBlock;

    @ObjectHolder("rftools:counter_block")
    public static BaseBlock counterBlock;

    @ObjectHolder("rftools:logic_block")
    public static BaseBlock threeLogicBlock;

    @ObjectHolder("rftools:invchecker_block")
    public static BaseBlock invCheckerBlock;

    @ObjectHolder("rftools:sensor_block")
    public static BaseBlock sensorBlock;

    @ObjectHolder("rftools:analog_block")
    public static BaseBlock analogBlock;

    @ObjectHolder("rftools:digit_block")
    public static BaseBlock digitBlock;

    @ObjectHolder("rftools:wire_block")
    public static BaseBlock wireBlock;

    @ObjectHolder("rftools:timer_block")
    public static BaseBlock timerBlock;

    @ObjectHolder("rftools:sequencer")
    public static TileEntityType<?> TYPE_SEQUENCER;

    @ObjectHolder("rftools:timer")
    public static TileEntityType<?> TYPE_TIMER;

    @ObjectHolder("rftools:three_logic")
    public static TileEntityType<?> TYPE_THREE_LOGIC;

    @ObjectHolder("rftools:invchecker")
    public static TileEntityType<?> TYPE_INVCHECKER;

    @ObjectHolder("rftools:sensor")
    public static TileEntityType<?> TYPE_SENSOR;

    @ObjectHolder("rftools:redstone_receiver")
    public static TileEntityType<?> TYPE_REDSTONE_RECEIVER;

    @ObjectHolder("rftools:redstone_transmitter")
    public static TileEntityType<?> TYPE_REDSTONE_TRANSMITTER;

    @ObjectHolder("rftools:wire")
    public static TileEntityType<?> TYPE_WIRE;

    @ObjectHolder("rftools:counter")
    public static TileEntityType<?> TYPE_COUNTER;

    @ObjectHolder("rftools:analog")
    public static TileEntityType<?> TYPE_ANALOG;

    public static void init() {
        redstoneTransmitterBlock = new RedstoneTransmitterBlock();
        redstoneReceiverBlock = new RedstoneReceiverBlock();

        sequencerBlock = new BaseBlock("sequencer_block", new BlockBuilder()
                .tileEntitySupplier(SequencerTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.sequencer")
                .infoExtendedParameter(ItemStackTools.intGetter("delay", 0))
                .infoExtendedParameter(stack -> ItemStackTools.mapTag(stack, compound -> SequencerMode.values()[compound.getInt("mode")].getDescription(), "<none>"))
                .infoExtendedParameter(stack -> ItemStackTools.mapTag(stack, compound -> Long.toHexString(compound.getLong("bits")), "<unset>")));
        counterBlock = new BaseBlock("counter_block", new BlockBuilder()
                .tileEntitySupplier(CounterTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.counter")
                .infoExtendedParameter(ItemStackTools.intGetter("counter", 0))
                .infoExtendedParameter(ItemStackTools.intGetter("current", 0)));
        threeLogicBlock = new BaseBlock("logic_block", new BlockBuilder()
                .tileEntitySupplier(ThreeLogicTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.threelogic"));
        invCheckerBlock = new BaseBlock("invchecker_block", new BlockBuilder()
                .tileEntitySupplier(InvCheckerTileEntity::new)
//                .container(InvCheckerTileEntity.CONTAINER_FACTORY)
//                .flags(BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.invchecker"));
        sensorBlock = new BaseBlock("sensor_block", new BlockBuilder()
                .tileEntitySupplier(SensorTileEntity::new)
//                .container(SensorTileEntity.CONTAINER_FACTORY)
//                .flags(BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.sensor"));
        analogBlock = new BaseBlock("analog_block", new BlockBuilder()
                .tileEntitySupplier(AnalogTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.analog"));
        digitBlock = new BaseBlock("digit_block", new BlockBuilder()
                .tileEntitySupplier(DigitTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE,
//                        BlockFlags.RENDER_CUTOUT, BlockFlags.RENDER_SOLID)
//                .property(DigitTileEntity.VALUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.digit"));
        wireBlock = new BaseBlock("wire_block", new BlockBuilder()
                .tileEntitySupplier(WireTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.wire"));
        timerBlock = new BaseBlock("timer_block", new BlockBuilder()
                .tileEntitySupplier(TimerTileEntity::new)
//                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.REDSTONE_OUTPUT, BlockFlags.NON_OPAQUE)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.timer")
                .infoExtendedParameter(ItemStackTools.intGetter("delay", 0)));
    }

//    public static void initClient() {
//        sequencerBlock.initModel();
//        sequencerBlock.setGuiFactory(GuiSequencer::new);
//
//        timerBlock.initModel();
//        timerBlock.setGuiFactory(GuiTimer::new);
//
//        counterBlock.initModel();
//        counterBlock.setGuiFactory(GuiCounter::new);
//
//        redstoneTransmitterBlock.initModel();
//        redstoneReceiverBlock.initModel();
//
//        threeLogicBlock.initModel();
//        threeLogicBlock.setGuiFactory(GuiThreeLogic::new);
//
//        invCheckerBlock.initModel();
//        invCheckerBlock.setGuiFactory(GuiInvChecker::new);
//
//        sensorBlock.initModel();
//        sensorBlock.setGuiFactory(GuiSensor::new);
//
//        wireBlock.initModel();
//
//        analogBlock.initModel();
//        analogBlock.setGuiFactory(GuiAnalog::new);
//
//        digitBlock.initModel();
//    }
}
