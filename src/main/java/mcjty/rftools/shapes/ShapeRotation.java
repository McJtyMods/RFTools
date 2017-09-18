package mcjty.rftools.shapes;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public enum ShapeRotation {
    NONE("None"),
    X("X"),
    Y("Y"),
    Z("Z");

    private final String code;

    private static final Map<String, ShapeRotation> MAP = new HashMap<>();

    static {
        for (ShapeRotation operation : ShapeRotation.values()) {
            MAP.put(operation.getCode(), operation);
        }
    }


    ShapeRotation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ShapeRotation getByName(String name) {
        return MAP.get(name);
    }

    // Bounds is already transformed
    public BlockPos transformDimension(BlockPos p) {
        switch (this) {
            case NONE:
                return p;
            case X:
                return new BlockPos(p.getX(), p.getZ(), p.getY());
            case Y:
                return new BlockPos(p.getZ(), p.getY(), p.getX());
            case Z:
                return new BlockPos(p.getY(), p.getX(), p.getZ());
        }
        return p;
    }
}
