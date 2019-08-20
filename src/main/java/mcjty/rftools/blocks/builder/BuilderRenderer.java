package mcjty.rftools.blocks.builder;

import mcjty.rftools.hud.HudRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BuilderRenderer extends TileEntityRenderer<BuilderTileEntity> {

    @Override
    public void render(BuilderTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        if (BuilderConfiguration.showProgressHud.get()) {
            HudRenderer.renderHud(te, x, y, z);
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(BuilderTileEntity.class, new BuilderRenderer());
    }
}
