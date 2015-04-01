package mcjty.rftools.blocks.shards;

public class DimensionalCross2Block extends AbstractDirectionalBlock {

    public DimensionalCross2Block() {
        super();
        setHardness(2.0f);
        setResistance(4.0f);
        setBlockName("dimensionalCross2Block");
    }

    @Override
    protected String getHorizTexture() {
        return "dimblock_pattern2";
    }

    @Override
    protected String getVertTexture() {
        return "dimblock_pattern1";
    }

    @Override
    protected String getTopBottomTexture() {
        return "dimblock_pattern4";
    }
}
