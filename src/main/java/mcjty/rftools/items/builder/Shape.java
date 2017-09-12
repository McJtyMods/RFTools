package mcjty.rftools.items.builder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum Shape {
    SHAPE_BOX(0, "Box", false, Formulas.FORMULA_BOX, 0),
    SHAPE_TOPDOME(1, "Top Dome", false, Formulas.FORMULA_SPHERE, 1),
    SHAPE_BOTTOMDOME(2, "Bottom Dome", false, Formulas.FORMULA_SPHERE, -1),
    SHAPE_SPHERE(3, "Sphere", false, Formulas.FORMULA_SPHERE, 0),
    SHAPE_CYLINDER(4, "Cylinder", false, Formulas.FORMULA_CYLINDER, 0),
    SHAPE_CAPPEDCYLINDER(5, "Capped Cylinder", false, Formulas.FORMULA_CAPPED_CYLINDER, 0),
    SHAPE_PRISM(6, "Prism", false, Formulas.FORMULA_PRISM, 0),
    SHAPE_TORUS(7, "Torus", false, Formulas.FORMULA_TORUS, 0),
    SHAPE_HEART(8, "Heart", false, Formulas.FORMULA_HEART, 0),
    SHAPE_CUSTOM(50, "Custom", false, Formulas.FORMULA_CUSTOM, 0),
    SHAPE_SOLIDBOX(100, "Solid Box", true, Formulas.FORMULA_BOX, 0),
    SHAPE_SOLIDSPHERE(103, "Solid Sphere", true, Formulas.FORMULA_SPHERE, 0),
    SHAPE_SOLIDCYLINDER(104, "Solid Cylinder", true, Formulas.FORMULA_CYLINDER, 0),
    SHAPE_SOLIDCAPPEDCYLINDER(105, "Solid Capped Cylinder", true, Formulas.FORMULA_CAPPED_CYLINDER, 0),
    SHAPE_SOLIDTORUS(107, "Solid Torus", true, Formulas.FORMULA_TORUS, 0),
    SHAPE_SOLIDTOPDOME(101, "Solid Top Dome", true, Formulas.FORMULA_SPHERE, 1),
    SHAPE_SOLIDBOTTOMDOME(102, "Solid Bottom Dome", true, Formulas.FORMULA_SPHERE, -1),
    SHAPE_SOLIDPRISM(106, "Solid Prim", true, Formulas.FORMULA_PRISM, 0),
    SHAPE_SOLIDHEART(108, "Solid Heart", true, Formulas.FORMULA_HEART, 0),
    SHAPE_SOLIDCUSTOM(150, "Solid Custom", true, Formulas.FORMULA_CUSTOM, 0);

    private final int index;
    private final String description;
    private final boolean solid;
    private final IFormula formula;
    private final int side;

    private static final Map<Integer, Shape> SHAPES;
    private static final Map<String, Shape> SHAPES_BY_DESCRIPTION;

    static {
        SHAPES_BY_DESCRIPTION = new HashMap<>();
        SHAPES = new HashMap<>();
        for (Shape shape : values()) {
            SHAPES.put(shape.getIndex(), shape);
            SHAPES_BY_DESCRIPTION.put(shape.getDescription(), shape);
        }
    }

    // Return the hollow version of the shape.
    public Shape makeHollow() {
        switch (this) {
            case SHAPE_SOLIDBOX:
                return SHAPE_BOX;
            case SHAPE_SOLIDSPHERE:
                return SHAPE_SPHERE;
            case SHAPE_SOLIDCYLINDER:
                return SHAPE_CAPPEDCYLINDER;
            case SHAPE_SOLIDTORUS:
                return SHAPE_TORUS;
            case SHAPE_SOLIDHEART:
                return SHAPE_HEART;
            case SHAPE_SOLIDTOPDOME:
                return SHAPE_TOPDOME;
            case SHAPE_SOLIDBOTTOMDOME:
                return SHAPE_BOTTOMDOME;
            case SHAPE_SOLIDPRISM:
                return SHAPE_PRISM;
            case SHAPE_SOLIDCUSTOM:
                return SHAPE_CUSTOM;
        }
        return this;
    }

    public Shape makeSolid() {
        switch (this) {
            case SHAPE_BOX:
                return SHAPE_SOLIDBOX;
            case SHAPE_SPHERE:
                return SHAPE_SOLIDSPHERE;
            case SHAPE_CAPPEDCYLINDER:
                return SHAPE_SOLIDCAPPEDCYLINDER;
            case SHAPE_CYLINDER:
                return SHAPE_SOLIDCYLINDER;
            case SHAPE_TORUS:
                return SHAPE_SOLIDBOX;
            case SHAPE_HEART:
                return SHAPE_SOLIDHEART;
            case SHAPE_TOPDOME:
                return SHAPE_SOLIDTOPDOME;
            case SHAPE_BOTTOMDOME:
                return SHAPE_SOLIDBOTTOMDOME;
            case SHAPE_PRISM:
                return SHAPE_SOLIDPRISM;
            case SHAPE_CUSTOM:
                return SHAPE_SOLIDCUSTOM;
        }
        return this;
    }

    Shape(int index, String description, boolean solid, @Nonnull IFormula formula, int side) {
        this.index = index;
        this.description = description;
        this.solid = solid;
        this.formula = formula;
        this.side = side;
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

    @Nonnull
    public IFormula getFormula() {
        return formula;
    }

    public int getSide() {
        return side;
    }

    public static Shape getShape(int index) {
        return SHAPES.get(index);
    }

    public static Shape getShape(String description) {
        return SHAPES_BY_DESCRIPTION.get(description);
    }
}
