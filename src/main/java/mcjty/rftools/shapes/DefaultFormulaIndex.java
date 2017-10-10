package mcjty.rftools.shapes;

public class DefaultFormulaIndex implements IFormulaIndex {

    private int x;
    private int y;
    private int z;

    private int startz;

    public DefaultFormulaIndex(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        startz = z;
    }

    @Override
    public void incX() {
        x++;
        z = startz;
    }

    @Override
    public void incZ() {
        z++;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
