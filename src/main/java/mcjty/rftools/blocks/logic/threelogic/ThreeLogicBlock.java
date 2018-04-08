package mcjty.rftools.blocks.logic.threelogic;

import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.generic.LogicFacing;
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreeLogicBlock extends LogicSlabBlock<ThreeLogicTileEntity, EmptyContainer> {

    public ThreeLogicBlock() {
        super(Material.IRON, "logic_block", ThreeLogicTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiThreeLogic> getGuiClass() {
        return GuiThreeLogic.class;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block can do various logical");
            list.add(TextFormatting.WHITE + "operations on three inputs.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private Set<BlockPos> loopDetector = new HashSet<>();

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof LogicSlabBlock && te instanceof ThreeLogicTileEntity && loopDetector.add(pos)) {
            try {
                ThreeLogicTileEntity tileEntity = (ThreeLogicTileEntity)te;
                LogicFacing facing = tileEntity.getFacing(state);
                EnumFacing downSide = facing.getSide();
                EnumFacing inputSide = facing.getInputSide();
                EnumFacing leftSide = rotateLeft(downSide, inputSide);
                EnumFacing rightSide = rotateRight(downSide, inputSide);

                int powered1 = getInputStrength(world, pos, leftSide) > 0 ? 1 : 0;
                int powered2 = getInputStrength(world, pos, inputSide) > 0 ? 2 : 0;
                int powered3 = getInputStrength(world, pos, rightSide) > 0 ? 4 : 0;
                tileEntity.setPowerInput(powered1 + powered2 + powered3);
                tileEntity.checkRedstone();
            } finally {
                loopDetector.remove(pos);
            }
        }
    }

    public static EnumFacing rotateLeft(EnumFacing downSide, EnumFacing inputSide) {
        switch (downSide) {
            case DOWN:
                return inputSide.rotateY();
            case UP:
                return inputSide.rotateYCCW();
            case NORTH:
                return inputSide.rotateAround(EnumFacing.Axis.Z);
            case SOUTH:
                return inputSide.getOpposite().rotateAround(EnumFacing.Axis.Z);
            case WEST:
                return inputSide.rotateAround(EnumFacing.Axis.X);
            case EAST:
                return inputSide.getOpposite().rotateAround(EnumFacing.Axis.X);
        }
        return inputSide;
    }

    public static EnumFacing rotateRight(EnumFacing downSide, EnumFacing inputSide) {
        return rotateLeft(downSide.getOpposite(), inputSide);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_THREE_LOGIC;
    }
}
