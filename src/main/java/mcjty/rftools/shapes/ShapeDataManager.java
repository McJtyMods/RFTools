package mcjty.rftools.shapes;

import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.RLE;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/// Server side handling for shape data
public class ShapeDataManager {

    // Client-side
    private static final Map<ShapeID, RenderData> renderDataMap = new HashMap<>();

    @Nullable
    public static RenderData getRenderData(ShapeID shapeID) {
        return renderDataMap.get(shapeID);
    }

    @Nonnull
    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = renderDataMap.get(shapeID);
        if (data == null) {
            data = new RenderData();
            renderDataMap.put(shapeID, data);
        }
        return data;
    }

    private static int cleanupCounter = 20;

    public static void cleanupOldRenderers() {
        cleanupCounter--;
        if (cleanupCounter >= 0) {
            return;
        }
        cleanupCounter = 20;
        Set<ShapeID> toRemove = new HashSet<>();
        for (Map.Entry<ShapeID, RenderData> entry : renderDataMap.entrySet()) {
            if (entry.getValue().tooOld()) {
                System.out.println("Removing id = " + entry.getKey());
                toRemove.add(entry.getKey());
            }
        }
        for (ShapeID id : toRemove) {
            RenderData data = renderDataMap.get(id);
            data.cleanup();
            renderDataMap.remove(id);
        }
    }


    private static class WorkUnit {
        private final ShapeID shapeID;
        private final List<EntityPlayerMP> players = new ArrayList<>();
        private ItemStack stack;
        private int offsetY;
        private IFormula formula;

        public WorkUnit(ShapeID shapeID, ItemStack stack, int offsetY, IFormula formula, EntityPlayerMP player) {
            this.shapeID = shapeID;
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

        public ShapeID getShapeID() {
            return shapeID;
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

    // Server-side
    private static final ArrayDeque<WorkUnit> workQueue = new ArrayDeque<>();
    private static final Map<Pair<ShapeID, Integer>, WorkUnit> workingOn = new HashMap<>();

    public static void pushWork(ShapeID shapeID, ItemStack stack, int offsetY, IFormula formula, EntityPlayerMP player) {
        Pair<ShapeID, Integer> key = Pair.of(shapeID, offsetY);
        if (workingOn.containsKey(key)) {
            workingOn.get(key).update(stack, offsetY, formula, player);
        } else {
            WorkUnit unit = new WorkUnit(shapeID, stack, offsetY, formula, player);
            workQueue.addLast(unit);
            workingOn.put(key, unit);
        }
    }

    public static int maxworkqueue = 0;

    public static void handleWork() {
        int pertick = 200*200;
        while (!workQueue.isEmpty()) {
            if (workQueue.size() > maxworkqueue) {
                maxworkqueue = workQueue.size();
                System.out.println("maxworkqueue = " + maxworkqueue);
            }
            WorkUnit unit = workQueue.removeFirst();
            Pair<ShapeID, Integer> key = Pair.of(unit.shapeID, unit.getOffsetY());
            workingOn.remove(key);

            ItemStack card = unit.getStack();
            boolean solid = ShapeCardItem.isSolid(card);
            BlockPos dimension = ShapeCardItem.getDimension(card);

            RLE positions = new RLE();
            StatePalette statePalette = new StatePalette();
            int cnt = ShapeCardItem.getRenderPositions(card, solid, positions, statePalette, unit.getFormula(), unit.getOffsetY());

            for (EntityPlayerMP player : unit.getPlayers()) {
                RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(unit.getShapeID(), positions, statePalette, dimension, cnt, unit.getOffsetY(), ""), player);
            }
            if (cnt > 0) {
                pertick -= dimension.getX() * dimension.getZ();
                if (pertick <= 0) {
                    break;
                }
            }
        }
    }

}
