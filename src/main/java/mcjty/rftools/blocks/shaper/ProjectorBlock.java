package mcjty.rftools.blocks.shaper;

import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;

public class ProjectorBlock extends GenericRFToolsBlock<ProjectorTileEntity, ProjectorContainer> {

    public ProjectorBlock() {
        super(Material.IRON, ProjectorTileEntity.class, ProjectorContainer.class, "projector", true);
    }

    @Override
    public boolean isHorizRotation() {
        return true;
    }

    @Override
    public int getGuiID() {
        return -1;
    }
}
