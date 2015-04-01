package mcjty.rftools.blocks.shards;

public class DimensionalPattern2Block extends AbstractDirectionalBlock {

    public DimensionalPattern2Block() {
        super();
        setHardness(2.0f);
        setResistance(4.0f);
        setBlockName("dimensionalPattern2Block");
    }

    @Override
    protected String getHorizTexture() {
        return "dimblock_pattern8";
    }

    @Override
    protected String getVertTexture() {
        return "dimblock_pattern6";
    }

    @Override
    protected String getTopBottomTexture() {
        return "dimblock_blank_stone";
    }
}
