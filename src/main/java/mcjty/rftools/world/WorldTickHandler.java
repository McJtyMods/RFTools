package mcjty.rftools.world;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class WorldTickHandler {

    public static WorldTickHandler instance = new WorldTickHandler();

    public static TIntObjectHashMap<ArrayDeque<RetroChunkCoord>> chunksToGen = new TIntObjectHashMap<>();
    public static TIntObjectHashMap<ArrayDeque<Pair<Integer,Integer>>> chunksToPreGen = new TIntObjectHashMap<>();

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER) {
            return;
        }
        World world = event.world;
        int dim = world.provider.getDimension();

        if (event.phase == TickEvent.Phase.END) {
            ArrayDeque<RetroChunkCoord> chunks = chunksToGen.get(dim);

            if (chunks != null && !chunks.isEmpty()) {
                RetroChunkCoord r = chunks.pollFirst();
                Pair<Integer,Integer> c = r.coord;
//                Logging.log("Retrogen " + c.toString() + ".");
                long worldSeed = world.getSeed();
                Random rand = new Random(worldSeed);
                long xSeed = rand.nextLong() >> 2 + 1L;
                long zSeed = rand.nextLong() >> 2 + 1L;
                rand.setSeed(xSeed * c.getLeft() + zSeed * c.getRight() ^ worldSeed);
                RFToolsWorldGenerator.instance.generateWorld(rand, r.coord.getLeft(), r.coord.getRight(), world, false);
                chunksToGen.put(dim, chunks);
            } else if (chunks != null) {
                chunksToGen.remove(dim);
            }
        } else {
            Deque<Pair<Integer, Integer>> chunks = chunksToPreGen.get(dim);

            if (chunks != null && !chunks.isEmpty()) {
                Pair<Integer,Integer> c = chunks.pollFirst();
//                Logging.log("Pregen " + c.toString() + ".");
                world.getChunkFromChunkCoords(c.getLeft(), c.getRight());
            } else if (chunks != null) {
                chunksToPreGen.remove(dim);
            }
        }
    }

    public static class RetroChunkCoord {

        private static final THashSet<String> emptySet = new THashSet<>(0);
        public final Pair<Integer,Integer> coord;
        public final THashSet<String> generatedFeatures;

        public RetroChunkCoord(Pair<Integer,Integer> pos, NBTTagList features) {

            coord = pos;
            if (features == null) {
                generatedFeatures = emptySet;
            } else {
                int e = features.tagCount();
                generatedFeatures = new THashSet<>(e);
                for (int i = 0 ; i < e; ++i) {
                    generatedFeatures.add(features.getStringTagAt(i));
                }
            }
        }
    }

}