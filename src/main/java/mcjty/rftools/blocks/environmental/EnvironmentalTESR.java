package mcjty.rftools.blocks.environmental;

import mcjty.lib.gui.RenderHelper;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class EnvironmentalTESR extends TileEntitySpecialRenderer<EnvironmentalControllerTileEntity> {

    private ResourceLocation halo = new ResourceLocation(RFTools.MODID, "textures/entities/floatingsphere.png");
    private Random random = new Random();

    public EnvironmentalTESR() {
    }

    @Override
    public void renderTileEntityAt(EnvironmentalControllerTileEntity te, double x, double y, double z, float time, int breakTime) {
        if (te.isActive()) {
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GlStateManager.disableAlpha();

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            this.bindTexture(halo);
            RenderHelper.renderBillboardQuadBright(0.3f + random.nextFloat() * .05f);
            GlStateManager.popMatrix();
        }
    }
}
