package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ProxyBlockTERenderer extends TileEntitySpecialRenderer {

    private static Map<Integer,WorldClient> cachedWorlds = new HashMap<Integer, WorldClient>();

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
            World world = tileEntity.getWorldObj();
            if (dimension != world.provider.dimensionId) {
                WorldClient wc = cachedWorlds.get(dimension);
                if (wc == null) {
                    WorldSettings.GameType gameType = world.getWorldInfo().getGameType();
                    WorldSettings settings = new WorldSettings(0L, gameType, false, false /* @@@ for now! hardcore mode*/, world.getWorldInfo().getTerrainType());
                    wc = new WorldClient(Minecraft.getMinecraft().getNetHandler(), settings, dimension, Minecraft.getMinecraft().gameSettings.difficulty, Minecraft.getMinecraft().mcProfiler);
                    cachedWorlds.put(dimension, wc);
                }
                world = wc;
            }

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
//            int meta = world.getBlockMetadata(oc.getX(), oc.getY(), oc.getZ());
//            tileEntity.getWorldObj().setBlockMetadataWithNotify(oc.getX(), oc.getY(), oc.getZ(), meta, 3);
//            tileEntity.getWorldObj().markBlockForUpdate((int) x, (int) y, (int) z);


//                    GL11.glPushMatrix();
//                    GL11.glTranslatef((float) (oc.getX() - x), (float) (oc.getY() - y), (float) (oc.getZ() - z));
//                    TileEntityRendererDispatcher.instance.renderTileEntity(te, f);
//                    ((TileEntitySpecialRenderer) renderer).renderTileEntityAt(te, oc.getX(), oc.getY(), oc.getZ(), f);
//                    GL11.glPopMatrix();
