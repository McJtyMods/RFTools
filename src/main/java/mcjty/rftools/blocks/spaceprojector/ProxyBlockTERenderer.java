package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

@SideOnly(Side.CLIENT)
public class ProxyBlockTERenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        ProxyBlockTileEntity proxyBlockTileEntity = (ProxyBlockTileEntity) tileEntity;
        Block camoBlock = proxyBlockTileEntity.getBlock();
        if (camoBlock != null) {
            Coordinate oc = proxyBlockTileEntity.getOrigCoordinate();
            if (oc == null) {
                System.out.println("mcjty.rftools.blocks.spaceprojector.ProxyBlockTERenderer.renderTileEntityAt NULL");
                return;
            }
            int dimension = proxyBlockTileEntity.getDimension();
            World world = DimensionManager.getWorld(dimension);
            TileEntity te = world.getTileEntity(oc.getX(), oc.getY(), oc.getZ());
            if (te != null) {
                Object renderer = TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(te.getClass());
                if (renderer instanceof TileEntitySpecialRenderer) {
                    ((TileEntitySpecialRenderer) renderer).renderTileEntityAt(te, x, y, z, f);
                }
            }
        }
    }
}
