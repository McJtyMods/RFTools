package mcjty.rftools.blocks.shaper;

import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;



public class ProjectorRenderer extends TileEntityRenderer<ProjectorTileEntity> {

    @Override
    public void render(ProjectorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);

        boolean sound = false;
        ItemStack renderStack = te.getRenderStack();
        if (te.isProjecting() && !renderStack.isEmpty()) {
            ShapeRenderer renderer = te.getShapeRenderer();
            boolean doSound = renderer.renderShapeInWorld(renderStack, x, y, z, te.getVerticalOffset(), te.getScale(), te.getAngle(),
                    te.isScanline(), te.getShapeID());
            if (ScannerConfiguration.baseProjectorVolume.get() > 0.0f && doSound && te.isSound()) {
                sound = true;
            }
        }
        if (ScannerConfiguration.baseProjectorVolume.get() > 0.0f) {
            if (sound) {
                if (!ProjectorSounds.isScanPlaying(te.getPos())) {
                    ProjectorSounds.playScan(te.getWorld(), te.getPos());
                }
            } else {
                ProjectorSounds.stopSound(te.getPos());
            }
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(ProjectorTileEntity.class, new ProjectorRenderer());
    }
}
