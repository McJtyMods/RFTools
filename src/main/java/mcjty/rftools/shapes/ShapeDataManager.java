package mcjty.rftools.shapes;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// Server side handling for shape data
public class ShapeDataManager {

    private static final Map<ShapeID, RenderData> renderDataMap = new HashMap<>();

    // The client calls this to request shape data from the server
    public static void requestShape(ShapeID shapeID, ItemStack card) {
        if (renderDataMap.containsKey(shapeID)) {
            // Already present
            return;
        }

        // @todo
    }

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



}
