package mcjty.rftools.shapes;

public class IndexedFormulaIndex implements IFormulaIndex {

    private int index;
    private int dy;
    private int dydz;

    private int lastindex;

    public IndexedFormulaIndex(int index, int dy, int dz) {
        this.index = index;
        this.dy = dy;
        this.dydz = dy * dz;
        lastindex = index;
    }

    @Override
    public void incX() {
        index = lastindex;
        index += dydz;
        lastindex = index;
    }

    @Override
    public void incZ() {
        index += dy;
    }

    public int getIndex() {
        return index;
    }
}
