package mcjty.rftools.blocks.ores;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class DimensionalShardItemBlock extends ItemBlock {
    public DimensionalShardItemBlock(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
