package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProjectorBlock extends GenericRFToolsBlock<ProjectorTileEntity, ProjectorContainer> {

    public ProjectorBlock() {
        super(Material.IRON, ProjectorTileEntity.class, ProjectorContainer.class, "projector", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(ProjectorTileEntity.class, new ProjectorRenderer());
    }

    @Override
    public boolean isHorizRotation() {
        return true;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiProjector.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_PROJECTOR;
    }
}
