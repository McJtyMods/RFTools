package mcjty.rftools.shapes;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum Shape {
    SHAPE_BOX("Box", Formulas.FormulaBox::new),
    SHAPE_TOPDOME("Top Dome", Formulas.FormulaTopDome::new),
    SHAPE_BOTTOMDOME("Bottom Dome", Formulas.FormulaBottomDome::new),
    SHAPE_SPHERE("Sphere", Formulas.FormulaSphere::new),
    SHAPE_CYLINDER("Cylinder", Formulas.FormulaCylinder::new),
    SHAPE_CAPPEDCYLINDER("Capped Cylinder", Formulas.FormulaCappedCylinder::new),
    SHAPE_PRISM("Prism", Formulas.FormulaPrism::new),
    SHAPE_TORUS("Torus", Formulas.FormulaTorus::new),
    SHAPE_HEART("Heart", Formulas.FormulaHeart::new),
    SHAPE_CONE("Cone", Formulas.FormulaCone::new),
    SHAPE_COMPOSITION("Composition", Formulas.FormulaComposition::new),
    SHAPE_SCAN("Scan", Formulas.FormulaScan::new);

    private final String description;
    private final Supplier<IFormula> formulaFactory;

    private static final Map<String, Shape> SHAPES_BY_DESCRIPTION;

    static {
        SHAPES_BY_DESCRIPTION = new HashMap<>();
        for (Shape shape : values()) {
            SHAPES_BY_DESCRIPTION.put(shape.getDescription(), shape);
        }
    }

    Shape(String description, @Nonnull Supplier<IFormula> formulaFactory) {
        this.description = description;
        this.formulaFactory = formulaFactory;
    }

    public String getDescription() {
        return description;
    }

    public boolean isComposition() {
        return this == SHAPE_COMPOSITION;
    }

    public boolean isScan() {
        return this == SHAPE_SCAN;
    }

    @Nonnull
    public Supplier<IFormula> getFormulaFactory() {
        return formulaFactory;
    }

    public static Shape getShape(String description) {
        return SHAPES_BY_DESCRIPTION.get(description);
    }
}
