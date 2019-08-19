package mcjty.rftools.shapes;

import net.minecraft.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatePalette {

    private List<BlockState> palette = new ArrayList<>();
    private Map<BlockState, Integer> paletteIndex = new HashMap<>();

    public int alloc(BlockState state, int def) {
        if (state == null) {
            return def;
        }
        Integer index = paletteIndex.get(state);
        if (index != null) {
            return index;
        }
        int idx = palette.size();
        if (idx > 253) {
            // Overflow! Return first entry
            return 0;
        }
        palette.add(state);
        paletteIndex.put(state, idx);
        return idx;
    }

    public void add(BlockState state) {
        paletteIndex.put(state, palette.size());
        palette.add(state);
    }

    public List<BlockState> getPalette() {
        return palette;
    }
}
