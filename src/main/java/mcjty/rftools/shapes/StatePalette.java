package mcjty.rftools.shapes;

import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatePalette {

    private List<IBlockState> palette = new ArrayList<>();
    private Map<IBlockState, Integer> paletteIndex = new HashMap<>();

    public int alloc(IBlockState state) {
        if (paletteIndex.containsKey(state)) {
            return paletteIndex.get(state);
        }
        int idx = palette.size();
        if (idx > 253) {
            // @todo overflow. How to handle gracefully?
            System.out.println("Scanner palette overflow!");
            return idx;
        }
        palette.add(state);
        paletteIndex.put(state, idx);
        return idx;
    }

    public List<IBlockState> getPalette() {
        return palette;
    }
}
