package mcjty.rftools.shapes;

import java.util.HashMap;
import java.util.Map;

public enum ShapeDeprecated {
    SHAPE_BOX(0, "Box", false, 0, Shape.SHAPE_BOX),
    SHAPE_TOPDOME(1, "Top Dome", false, 1, Shape.SHAPE_TOPDOME),
    SHAPE_BOTTOMDOME(2, "Bottom Dome", false, -1, Shape.SHAPE_BOTTOMDOME),
    SHAPE_SPHERE(3, "Sphere", false, 0, Shape.SHAPE_SPHERE),
    SHAPE_CYLINDER(4, "Cylinder", false, 0, Shape.SHAPE_CYLINDER),
    SHAPE_CAPPEDCYLINDER(5, "Capped Cylinder", false, 0, Shape.SHAPE_CAPPEDCYLINDER),
    SHAPE_PRISM(6, "Prism", false, 0, Shape.SHAPE_PRISM),
    SHAPE_TORUS(7, "Torus", false, 0, Shape.SHAPE_TORUS),
    SHAPE_HEART(8, "Heart", false, 0, Shape.SHAPE_HEART),
    SHAPE_CUSTOM(50, "Custom", false, 0, Shape.SHAPE_CUSTOM),
    SHAPE_SOLIDBOX(100, "Solid Box", true, 0, Shape.SHAPE_BOX),
    SHAPE_SOLIDSPHERE(103, "Solid Sphere", true, 0, Shape.SHAPE_SPHERE),
    SHAPE_SOLIDCYLINDER(104, "Solid Cylinder", true, 0, Shape.SHAPE_CYLINDER),
    SHAPE_SOLIDCAPPEDCYLINDER(105, "Solid Capped Cylinder", true, 0, Shape.SHAPE_CAPPEDCYLINDER),
    SHAPE_SOLIDTORUS(107, "Solid Torus", true, 0, Shape.SHAPE_TORUS),
    SHAPE_SOLIDTOPDOME(101, "Solid Top Dome", true, 1, Shape.SHAPE_TOPDOME),
    SHAPE_SOLIDBOTTOMDOME(102, "Solid Bottom Dome", true, -1, Shape.SHAPE_BOTTOMDOME),
    SHAPE_SOLIDPRISM(106, "Solid Prim", true, 0, Shape.SHAPE_PRISM),
    SHAPE_SOLIDHEART(108, "Solid Heart", true, 0, Shape.SHAPE_HEART),
    SHAPE_SOLIDCUSTOM(150, "Solid Custom", true, 0, Shape.SHAPE_CUSTOM);

    private final int index;
    private final String description;
    private final boolean solid;
    private final int side;
    private final Shape newshape;

    private static final Map<Integer, ShapeDeprecated> SHAPES;
    private static final Map<String, ShapeDeprecated> SHAPES_BY_DESCRIPTION;

    static {
        SHAPES_BY_DESCRIPTION = new HashMap<>();
        SHAPES = new HashMap<>();
        for (ShapeDeprecated shape : values()) {
            SHAPES.put(shape.getIndex(), shape);
            SHAPES_BY_DESCRIPTION.put(shape.getDescription(), shape);
        }
    }

    ShapeDeprecated(int index, String description, boolean solid, int side, Shape newshape) {
        this.index = index;
        this.description = description;
        this.solid = solid;
        this.side = side;
        this.newshape = newshape;
    }

    public Shape getNewshape() {
        return newshape;
    }

    public int getIndex() {
        return index;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSolid() {
        return solid;
    }

    public int getSide() {
        return side;
    }

    public static ShapeDeprecated getShape(int index) {
        return SHAPES.get(index);
    }

    public static ShapeDeprecated getShape(String description) {
        return SHAPES_BY_DESCRIPTION.get(description);
    }
}
