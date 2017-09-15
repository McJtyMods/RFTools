package mcjty.rftools.items.builder;

public class ShapeModifier {
    private final ShapeOperation operation;
    private final boolean flipY;
    private final ShapeRotation rotation;

    public ShapeModifier(ShapeOperation operation, boolean flipY, ShapeRotation rotation) {
        this.operation = operation;
        this.flipY = flipY;
        this.rotation = rotation;
    }

    public ShapeOperation getOperation() {
        return operation;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public ShapeRotation getRotation() {
        return rotation;
    }
}
