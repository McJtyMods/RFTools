package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CamoBlockId {
    private final String registryName;
    private final int meta;

    public CamoBlockId(BlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
        this.meta = block.getMetaFromState(mimicBlock);
    }

    public BlockState getBlockState() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).getStateFromMeta(meta);
    }

    @Override
    public String toString() {
        return registryName + '@' + meta;
    }
}
