package mcjty.rftools.blocks.logic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ThreeLogicBlock extends LogicSlabBlock<ThreeLogicTileEntity, EmptyContainer> {

    public ThreeLogicBlock() {
        super(Material.IRON, "logic_block", ThreeLogicTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiThreeLogic.class;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block can do various logical");
            list.add(TextFormatting.WHITE + "operations on three inputs.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity)te;
            EnumFacing downSide = logicTileEntity.getFacing().getSide();
            EnumFacing inputSide = logicTileEntity.getFacing().getInputSide();
            EnumFacing leftSide = rotateLeft(downSide, inputSide);
            EnumFacing rightSide = rotateRight(downSide, inputSide);

            int powered1 = getInputStrength(world, pos, leftSide) > 0 ? 1 : 0;
            int powered2 = getInputStrength(world, pos, inputSide) > 0 ? 2 : 0;
            int powered3 = getInputStrength(world, pos, rightSide) > 0 ? 4 : 0;
            logicTileEntity.setPowered(powered1 + powered2 + powered3);
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
