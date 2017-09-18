package mcjty.rftools.shapes;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum Shape {
    SHAPE_BOX("Box", Formulas.FORMULA_BOX),
    SHAPE_TOPDOME("Top Dome", Formulas.FORMULA_TOPDOME),
    SHAPE_BOTTOMDOME("Bottom Dome", Formulas.FORMULA_BOTTOMDOME),
    SHAPE_SPHERE("Sphere", Formulas.FORMULA_SPHERE),
    SHAPE_CYLINDER("Cylinder", Formulas.FORMULA_CYLINDER),
    SHAPE_CAPPEDCYLINDER("Capped Cylinder", Formulas.FORMULA_CAPPED_CYLINDER),
    SHAPE_PRISM("Prism", Formulas.FORMULA_PRISM),
    SHAPE_TORUS("Torus", Formulas.FORMULA_TORUS),
    SHAPE_HEART("Heart", Formulas.FORMULA_HEART),
    SHAPE_CUSTOM("Custom", Formulas.FORMULA_CUSTOM),
    SHAPE_SCHEME("Scheme", Formulas.FORMULA_SCHEME);

    private final String description;
    private final IFormulaFactory formulaFactory;

    private static final Map<String, Shape> SHAPES_BY_DESCRIPTION;

    static {
        SHAPES_BY_DESCRIPTION = new HashMap<>();
        for (Shape shape : values()) {
            SHAPES_BY_DESCRIPTION.put(shape.getDescription(), shape);
        }
    }

    Shape(String description, @Nonnull IFormulaFactory formulaFactory) {
        this.description = description;
        this.formulaFactory = formulaFactory;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCustom() {
        return this == SHAPE_CUSTOM;
    }

    public boolean isScheme() {
        return this == SHAPE_SCHEME;
    }

    @Nonnull
    public IFormulaFactory getFormulaFactory() {
        return formulaFactory;
    }

    public static Shape getShape(String description) {
        return SHAPES_BY_DESCRIPTION.get(description);
    }
}
