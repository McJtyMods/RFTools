package mcjty.rftools.shapes;

import java.util.HashMap;
import java.util.Map;

public enum ShapeOperation {
    UNION("+", "Add (union) this shape to the previous one"),
    SUBTRACT("-", "Subtract (difference) this shape from the previous one"),
    INTERSECT("^", "Intersect this shape with the previous one");

    private final String code;
    private final String description;

    private static final Map<String, ShapeOperation> MAP = new HashMap<>();

    static {
        for (ShapeOperation operation : ShapeOperation.values()) {
            MAP.put(operation.getCode(), operation);
        }
    }

    ShapeOperation(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ShapeOperation getByName(String name) {
        return MAP.get(name);
    }
}
