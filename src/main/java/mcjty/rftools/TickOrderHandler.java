package mcjty.rftools;

import mcjty.rftools.blocks.endergen.EnderMonitorTileEntity;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import mcjty.rftools.blocks.endergen.PearlInjectorTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TickOrderHandler {
    public interface ICheckStateServer {
        void checkStateServer();
        int getDimension();
    }

    private static List<PearlInjectorTileEntity> pearlInjectors = new ArrayList<>();

    private static List<EndergenicTileEntity> connectedEndergenics = new ArrayList<>();
    private static List<EndergenicTileEntity> endergenics = new ArrayList<>();

    private static List<SequencerTileEntity> sequencers = new ArrayList<>();
    private static List<TimerTileEntity> timers = new ArrayList<>();
    private static List<EnderMonitorTileEntity> enderMonitors = new ArrayList<>();

    private TickOrderHandler() {
    }

    public static void clean() {
        pearlInjectors.clear();
        connectedEndergenics.clear();
        endergenics.clear();
        sequencers.clear();
        timers.clear();
        enderMonitors.clear();
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

    private static <T extends ICheckStateServer> List<T> checkStateServer(int dimension, List<T> tileEntities) {
        List<T> remainingTes = new ArrayList<>();
        for (T tileEntity : tileEntities) {
            if (tileEntity.getDimension() == dimension) {
                tileEntity.checkStateServer();
            } else {
                remainingTes.add(tileEntity);
            }
        }
        return remainingTes;
    }

    static void postWorldTick(int dimension) {
        /*
            There is *no* pearl delay between:
            * Pearl Injector -> Endergenic (connected)
            * Endergenic -> Endergenic (connected) *except* for last one to first one (one tick)

            It is possible to have a consistent pearl delay of one tick by reversing the tick order of the
            endergenics, however this slows down the cycle a lot.

            At this point everything else has ticked already, including redstone. If a redstone source entity is now
            ticked *before* its destination entity the delay depends on if there is a direct connection or a redstone
            connection between the blocks. If an redstone source entity is ticked *before* its destination entity the
            delay is always exactly one tick.

            There is exactly *one tick* delay between both w/ and w/o redstone between:
            * Ender Monitor -> Pearl Injector, Endergenic
            * Timer -> Pearl Injector, Endergenic
            * Sequencer -> Pearl Injector, Endergenic, Timer

            There is *no* delay w/o redstone and *one tick* delay w/ redstone between:
            * Ender Monitor -> Sequencer, Timer
            * Timer -> Sequencer

            There is *no* delay or *one tick* delay (placement/load order/redstone dependent) between:
            * Sequencer -> Sequencer
            * Timer -> Timer
         */
        pearlInjectors = checkStateServer(dimension, pearlInjectors);

        endergenics.removeAll(connectedEndergenics);

        connectedEndergenics = checkStateServer(dimension, connectedEndergenics); // In order of connection
        endergenics = checkStateServer(dimension, endergenics); // Unconnected

        enderMonitors = checkStateServer(dimension, enderMonitors);
        timers = checkStateServer(dimension, timers);
        sequencers = checkStateServer(dimension, sequencers);
    }
}
