package mcjty.rftools.blocks.shards;

public class DimensionalCrossBlock extends AbstractDirectionalBlock {

    public DimensionalCrossBlock() {
        super();
        setHardness(2.0f);
        setResistance(4.0f);
        setBlockName("dimensionalCrossBlock");
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
        return "dimblock_pattern3";
    }
}
