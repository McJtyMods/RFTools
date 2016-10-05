package mcjty.rftools.blocks.builder;

import mcjty.rftools.hud.HudRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BuilderRenderer extends TileEntitySpecialRenderer<BuilderTileEntity> {

    @Override
    public void renderTileEntityAt(BuilderTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
        if (BuilderConfiguration.showProgressHud) {
            HudRenderer.renderHud(te, x, y, z);
        }
    }
}
