package mcjty.rftools.blocks.shaper;

import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProjectorRenderer extends TileEntitySpecialRenderer<ProjectorTileEntity> {

    @Override
    public void render(ProjectorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        ItemStack renderStack = te.getRenderStack();
        if (te.isActive() && !renderStack.isEmpty()) {
            ShapeRenderer renderer = te.getShapeRenderer();
            renderer.renderShapeInWorld(renderStack, x, y, z, te.getVerticalOffset(), te.getScale(), te.getAngle());
        }
    }
}
