//Mr. Pyro 2018 :)
package mcjty.rftools.shapes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeRegistry {

    private static List<IFormula> shapes = new ArrayList<>();
    private static Map<String, IFormula> descriptionMap = new HashMap<>();
    private static Map<String, IFormula> nameMap = new HashMap<>();

    //Enum with names of rftools shapes
    public enum CommonNames {

        SHAPE_BOX { public String toString() {return "box";}},
        SHAPE_TOPDOME { public String toString() {return "top_dome";}},
        SHAPE_BOTTOMDOME { public String toString() {return "bottom_dome";}},
        SHAPE_SPHERE { public String toString() {return "sphere";}},
        SHAPE_CYLINDER { public String toString() {return "cylinder";}},
        SHAPE_CAPPEDCYLINDER { public String toString() {return "capped_cylinder";}},
        SHAPE_PYRAMID { public String toString() {return "pyramid";}},
        SHAPE_TORUS { public String toString() {return "torus";}},
        SHAPE_HEART { public String toString() {return "heart";}},
        SHAPE_CONE { public String toString() {return "cone";}},
        SHAPE_COMPOSITION { public String toString() {return "composition";}},
        SHAPE_SCAN { public String toString() {return "scan";}},

    }

    //registers rftools shapes
    public static void registerCommonShapes() {

        registerShape(new Formulas.FormulaBox());
        registerShape(new Formulas.FormulaTopDome());
        registerShape(new Formulas.FormulaBottomDome());
        registerShape(new Formulas.FormulaSphere());
        registerShape(new Formulas.FormulaCylinder());
        registerShape(new Formulas.FormulaCappedCylinder());
        registerShape(new Formulas.FormulaPrism());
        registerShape(new Formulas.FormulaTorus());
        registerShape(new Formulas.FormulaHeart());
        registerShape(new Formulas.FormulaCone());
        registerShape(new Formulas.FormulaComposition());
        registerShape(new Formulas.FormulaScan());

    }

    //registers a shape with the registry
    public static void registerShape(IFormula shape) {

        shapes.add(shape);
        descriptionMap.put(shape.getDescription(), shape);
        nameMap.put(shape.getShapeName(), shape);

    }

    //gets a string array of the description (names ex "Sphere") of all shapes in the registry (for gui)
    public static String[] getAllDescriptions() {

        String[] result = new String[shapes.size()];

        for (int i = 0; i < shapes.size(); i++) {

            result[i] = (shapes.get(i).getDescription());

        }

        return result;

    }

    //returns a list of all shapes registered in the registry
    @Nonnull
    public static List<IFormula> getAllShapes() {

        return shapes;

    }

    //allows you to get a shape by its description (Name)
    public static IFormula getShapebyDescription(String description) {

        return descriptionMap.get(description);

    }

    //get a shape by its name (name as in registry name)
    public static IFormula getShapebyName(String name) {

        return nameMap.get(name);

    }

    //get a shape by its name (name as in registry name) via common names enum
    public static IFormula getShapebyName(CommonNames name) {

        return nameMap.get(name.toString());

    }

    //returns true if the shape passed in is a composition
    public static boolean isComposition(IFormula shape) {

        return shape instanceof Formulas.FormulaComposition ? true : false;

    }

    //returns true if the shape passed in is a scan
    public static boolean isScan(IFormula shape) {

        return shape instanceof Formulas.FormulaScan ? true : false;

    }

    //returns the "ordinal" in this case its index in the registry list
    public static int ordinal(IFormula shape) {

        return shapes.indexOf(shape);

    }

}
