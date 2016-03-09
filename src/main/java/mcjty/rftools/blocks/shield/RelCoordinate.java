package mcjty.rftools.blocks.shield;

public class RelCoordinate {
    final private int dx;
    final private int dy;
    final private int dz;

    public RelCoordinate(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelCoordinate that = (RelCoordinate) o;

        if (dx != that.dx) return false;
        if (dy != that.dy) return false;
        return dz == that.dz;

    }

    @Override
    public int hashCode() {
        int result = dx;
        result = 31 * result + dy;
        result = 31 * result + dz;
        return result;
    }
}
