package mcjty.rftools.blocks.shards;

public class DimensionalPattern1Block extends AbstractDirectionalBlock {

    public DimensionalPattern1Block() {
        super();
        setHardness(2.0f);
        setResistance(4.0f);
        setBlockName("dimensionalPattern1Block");
    }

    @Override
    protected String getHorizTexture() {
        return "dimblock_pattern7";
    }

    @Override
    protected String getVertTexture() {
        return "dimblock_pattern5";
    }

    @Override
    protected String getTopBottomTexture() {
        return "dimblock_blank_stone";
    }
}
