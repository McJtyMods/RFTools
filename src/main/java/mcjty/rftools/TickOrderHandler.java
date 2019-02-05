package mcjty.rftools;

import mcjty.rftools.blocks.endergen.EnderMonitorTileEntity;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import mcjty.rftools.blocks.endergen.PearlInjectorTileEntity;
import mcjty.rftools.blocks.logic.sequencer.SequencerTileEntity;
import mcjty.rftools.blocks.logic.timer.TimerTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TickOrderHandler {
    public interface ICheckStateServer {
        void checkStateServer();
    }

    private static List<PearlInjectorTileEntity> pearlInjectors = new ArrayList<>();

    private static List<EndergenicTileEntity> connectedEndergenics = new ArrayList<>();
    private static List<EndergenicTileEntity> endergenics = new ArrayList<>();

    private static List<SequencerTileEntity> sequencers = new ArrayList<>();
    private static List<TimerTileEntity> timers = new ArrayList<>();
    private static List<EnderMonitorTileEntity> enderMonitors = new ArrayList<>();

    private TickOrderHandler() {
    }

    public static void queuePearlInjector(PearlInjectorTileEntity pearlInjector) {
        pearlInjectors.add(pearlInjector);

        // Find all connected endergenics in order
        EndergenicTileEntity endergenic = pearlInjector.findEndergenicTileEntity();
        while (endergenic != null && !connectedEndergenics.contains(endergenic)) {
            connectedEndergenics.add(endergenic);
            endergenic = endergenic.getDestinationTE();
        }

    }

    public static void queueEndergenic(EndergenicTileEntity endergenic) {
        endergenics.add(endergenic);
    }

    public static void queueSequencer(SequencerTileEntity sequencer) {
        sequencers.add(sequencer);
    }

    public static void queueTimer(TimerTileEntity timer) {
        timers.add(timer);
    }

    public static void queueEnderMonitor(EnderMonitorTileEntity enderMonitor) {
        enderMonitors.add(enderMonitor);
    }

    private static <T extends ICheckStateServer> void checkStateServer(List<T> tileEntities) {
        for (ICheckStateServer tileEntity : tileEntities) {
            tileEntity.checkStateServer();
        }
        tileEntities.clear();
    }

    static void postWorldTick() {
        /*
            There is *no* pearl delay between:
            * Pearl Injector -> Endergenic (connected)
            * Endergenic -> Endergenic (connected) *except* for last one to first one (one tick)

            It is possible to have a consistent pearl delay of one tick by reversing the tick order of the
            endergenics, however this slows down the cycle a lot.

            At this point everything else has ticked already, including redstone. If a redstone source entity is now
            ticked *before* its destination entity the delay depends on if there is a direct connection or a redstone
            connection between the blocks. If an redstone source entity is ticked *before* its destination entity the
            delay is always exactly one tick. The tick order was chosen in order for the most common and useful
            combinations to have consistent delay, i.e. one tick.

            There is exactly *one tick* delay between both w/ and w/o redstone between:
            * Ender Monitor -> Pearl Injector, Endergenic, Sequencer, Timer
            * Timer -> Pearl Injector, Endergenic, Sequencer
            * Sequencer -> Pearl Injector, Endergenic

            There is *no* delay w/o redstone and *one tick* delay w/ redstone between:
            * Sequencer -> Timer

            There is *no* delay or *one tick* delay (placement/load order/redstone dependent) between:
            * Sequencer -> Sequencer
            * Timer -> Timer
         */
        checkStateServer(pearlInjectors);

        endergenics.removeAll(connectedEndergenics);
        checkStateServer(connectedEndergenics); // In order of connection
        checkStateServer(endergenics); // Unconnected

        checkStateServer(sequencers);
        checkStateServer(timers);
        checkStateServer(enderMonitors);
    }
}
