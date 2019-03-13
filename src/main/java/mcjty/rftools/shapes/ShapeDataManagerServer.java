package mcjty.rftools.shapes;

import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.RLE;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/// Server side handling for shape data
public class ShapeDataManagerServer {


    private static class WorkUnit {
        private final List<EntityPlayerMP> players = new ArrayList<>();
        private ItemStack stack;
        private int offsetY;
        private IFormula formula;

        public WorkUnit(ItemStack stack, int offsetY, IFormula formula, EntityPlayerMP player) {
            this.stack = stack;
            this.offsetY = offsetY;
            this.formula = formula;
            this.players.add(player);
        }

        public void update(ItemStack stack, int offsetY, IFormula formula, EntityPlayerMP player) {
            this.stack = stack;
            this.offsetY = offsetY;
            this.formula = formula;
            if (!players.contains(player)) {
                players.add(player);
            }
        }

        public List<EntityPlayerMP> getPlayers() {
            return players;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public IFormula getFormula() {
            return formula;
        }
    }

    private static class WorkQueue {
        private final ArrayDeque<WorkUnit> workQueue = new ArrayDeque<>();
        private final Map<Integer, WorkUnit> workingOn = new HashMap<>();
    }

    // Server-side
    private static final Map<ShapeID, WorkQueue> workQueues = new HashMap<>();

    public static void pushWork(ShapeID shapeID, ItemStack stack, int offsetY, IFormula formula, EntityPlayerMP player) {
        WorkQueue queue = workQueues.get(shapeID);
        if (queue == null) {
            queue = new WorkQueue();
            workQueues.put(shapeID, queue);
        }
        if (queue.workingOn.containsKey(offsetY)) {
            queue.workingOn.get(offsetY).update(stack, offsetY, formula, player);
        } else {
            WorkUnit unit = new WorkUnit(stack, offsetY, formula, player);
            queue.workQueue.addLast(unit);
            queue.workingOn.put(offsetY, unit);
        }
    }

    public static void handleWork() {
        Set<ShapeID> toRemove = new HashSet<>();
        for (Map.Entry<ShapeID, WorkQueue> entry : workQueues.entrySet()) {
            ShapeID shapeID = entry.getKey();
            WorkQueue queue = entry.getValue();

            int pertick = ScannerConfiguration.planeSurfacePerTick.get();
            while (!queue.workQueue.isEmpty()) {
                WorkUnit unit = queue.workQueue.removeFirst();
                queue.workingOn.remove(unit.getOffsetY());

                ItemStack card = unit.getStack();
                boolean solid = ShapeCardItem.isSolid(card);
                BlockPos dimension = ShapeCardItem.getDimension(card);

                RLE positions = new RLE();
                StatePalette statePalette = new StatePalette();
                int cnt = ShapeCardItem.getRenderPositions(card, solid, positions, statePalette, unit.getFormula(), unit.getOffsetY());

                for (EntityPlayerMP player : unit.getPlayers()) {
                    RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(shapeID, positions, statePalette, dimension, cnt, unit.getOffsetY(), ""), player);
                }
                if (cnt > 0) {
                    pertick -= dimension.getX() * dimension.getZ();
                    if (pertick <= 0) {
                        break;
                    }
                }
            }
            if (queue.workQueue.isEmpty()) {
                toRemove.add(shapeID);
            }
        }
        for (ShapeID id : toRemove) {
            workQueues.remove(id);
        }

    }

}
