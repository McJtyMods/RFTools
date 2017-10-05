package mcjty.rftools.blocks.shaper;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProjectorRenderer extends TileEntitySpecialRenderer<ProjectorTileEntity> {

    @Override
    public void renderTileEntityAt(ProjectorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);

        ItemStack renderStack = te.getRenderStack();
        if (te.isProjecting() && ItemStackTools.isValid(renderStack)) {
            ShapeRenderer renderer = te.getShapeRenderer();
            boolean doSound = renderer.renderShapeInWorld(renderStack, x, y, z, te.getVerticalOffset(), te.getScale(), te.getAngle(),
                    te.isScanline());
            if (doSound && ScannerConfiguration.baseProjectorVolume > 0.0f) {
                if (!ProjectorSounds.isScanPlaying(te.getWorld(), te.getPos())) {
                    ProjectorSounds.playScan(te.getWorld(), te.getPos());
                }
            }
        }
    }
}
