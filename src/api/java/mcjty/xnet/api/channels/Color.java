package mcjty.xnet.api.channels;

import java.util.HashMap;
import java.util.Map;

public enum Color {
    OFF(0x000000),
    WHITE(0xffffff),
    RED(0xff0000),
    GREEN(0x00ff00),
    BLUE(0x0000ff),
    YELLOW(0xffff00),
    CYAN(0x00ffff),
    PURPLE(0xff00ff);

    private final int color;

    Color(int color) {
        this.color = color;
    }

    private static final Map<Integer, Color> COLOR_MAP = new HashMap<>();
    public static final Integer[] COLORS = new Integer[Color.values().length];

    static {
        for (int i = 0; i < Color.values().length; i++) {
            Color col = Color.values()[i];
            COLORS[i] = col.color;
            COLOR_MAP.put(col.color, col);
        }
    }

    public int getColor() {
        return color;
    }

    public static Color colorByValue(int color) {
        return COLOR_MAP.get(color);
    }
}
