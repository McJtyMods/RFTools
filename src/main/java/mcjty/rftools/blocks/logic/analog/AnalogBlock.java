package mcjty.rftools.blocks.logic.analog;

import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.generic.LogicFacing;
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock;
import mcjty.rftools.blocks.logic.threelogic.ThreeLogicBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalogBlock extends LogicSlabBlock<AnalogTileEntity, EmptyContainer> {

    public AnalogBlock() {
        super(Material.IRON, "analog_block", AnalogTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, player, list, advanced);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block can do");
            list.add(TextFormatting.WHITE + "calculations on analog redstone");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ANALOG;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiAnalog> getGuiClass() {
        return GuiAnalog.class;
    }

    private Set<BlockPos> loopDetector = new HashSet<>();

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        if (loopDetector.contains(pos)) {
            // We are in a loop. Do nothing
            return;
        }

        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof LogicSlabBlock && te instanceof AnalogTileEntity) {
            loopDetector.add(pos);
            AnalogTileEntity tileEntity = (AnalogTileEntity)te;
            LogicFacing facing = tileEntity.getFacing(state);
            EnumFacing downSide = facing.getSide();
            EnumFacing inputSide = facing.getInputSide();
            EnumFacing rightSide = ThreeLogicBlock.rotateLeft(downSide, inputSide);
            EnumFacing leftSide = ThreeLogicBlock.rotateRight(downSide, inputSide);

            int outputStrength;
            int inputStrength = getInputStrength(world, pos, inputSide);
            int inputLeft = getInputStrength(world, pos, leftSide);
            int inputRight = getInputStrength(world, pos, rightSide);
            if (inputLeft == inputRight) {
                outputStrength = (int) (inputStrength * tileEntity.getMulEqual() + tileEntity.getAddEqual());
            } else if (inputLeft < inputRight) {
                outputStrength = (int) (inputStrength * tileEntity.getMulLess() + tileEntity.getAddLess());
            } else {
                outputStrength = (int) (inputStrength * tileEntity.getMulGreater() + tileEntity.getAddGreater());
            }

            int oldPower = tileEntity.getPowerLevel();
            tileEntity.setPowerInput(outputStrength);
            if (oldPower != outputStrength) {
                world.notifyNeighborsOfStateChange(pos, this, false);
            }
            loopDetector.remove(pos);
        }
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof AnalogBlock && te instanceof AnalogTileEntity) {
            AnalogTileEntity logicTileEntity = (AnalogTileEntity) te;
            if (side == logicTileEntity.getFacing(state).getInputSide()) {
                return logicTileEntity.getPowerLevel();
            } else {
                return 0;
            }
        }
        return 0;
    }

}
