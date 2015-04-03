package mcjty.rftools.blocks.spaceprojector;

import mcjty.rftools.render.DefaultISBRH;
import mcjty.rftools.render.TesseleratorAccessHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

public class ProxyBlockRenderer  extends DefaultISBRH {
    @Override
    public int getRenderId() {
        return ProxyBlock.RENDERID_PROXYBLOCK;
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        ProxyBlockTileEntity proxyBlockTileEntity = (ProxyBlockTileEntity) world.getTileEntity(x, y, z);
        Block camoBlock = proxyBlockTileEntity.getBlock();
        if (camoBlock == null) {
            try {
                int addedVertices = TesseleratorAccessHelper.getAddedVertices(Tessellator.instance);
                boolean rc = renderer.renderBlockByRenderType(camoBlock, x, y, z);
                if (!rc) {
                    return false;
                }
                int newAddedVertices = TesseleratorAccessHelper.getAddedVertices(Tessellator.instance);
                return addedVertices != newAddedVertices;
            } catch (Exception e) {
                // Ignore this error. Nothing is rendered  in this case.
                return false;
            }
        }
        return true;
    }
}
