package mcjty.rftools.shapes;

import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatePalette {

    private List<IBlockState> palette = new ArrayList<>();
    private Map<IBlockState, Integer> paletteIndex = new HashMap<>();

    public int alloc(IBlockState state, int def) {
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

    public void add(IBlockState state) {
        paletteIndex.put(state, palette.size());
        palette.add(state);
    }

    public List<IBlockState> getPalette() {
        return palette;
    }
}
