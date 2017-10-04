package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.BlockTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ProjectorBlock && te instanceof ProjectorTileEntity) {
            ProjectorTileEntity projector = (ProjectorTileEntity)te;
            EnumFacing north = BlockTools.reorientHoriz(EnumFacing.NORTH, getMetaFromState(state));
            EnumFacing south = BlockTools.reorientHoriz(EnumFacing.SOUTH, getMetaFromState(state));
            EnumFacing west = BlockTools.reorientHoriz(EnumFacing.WEST, getMetaFromState(state));
            EnumFacing east = BlockTools.reorientHoriz(EnumFacing.EAST, getMetaFromState(state));

            int powered1 = getInputStrength(world, pos, north) > 0 ? 1 : 0;
            int powered2 = getInputStrength(world, pos, south) > 0 ? 2 : 0;
            int powered3 = getInputStrength(world, pos, west) > 0 ? 4 : 0;
            int powered4 = getInputStrength(world, pos, east) > 0 ? 8 : 0;
            projector.setPowerInput(powered1 + powered2 + powered3 + powered4);
        }
    }

    /**
     * Returns the signal strength at one side of the block
     */
    protected int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        int power = world.getRedstonePower(pos.offset(side), side);
        if (power == 0) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // @todo this is a bit of a hack. Don't know how to do it better right now
            IBlockState blockState = world.getBlockState(pos.offset(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = world.isBlockPowered(pos.offset(side)) ? 15 : 0;
            }
        }

        return power;
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
