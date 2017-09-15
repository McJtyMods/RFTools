package mcjty.rftools.blocks.shield;

public class RelCoordinateShield extends RelCoordinate {
    private final int state;

    public RelCoordinateShield(int dx, int dy, int dz, int state) {
        super(dx, dy, dz);
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
