package mcjty.rftools.blocks.logic.digit;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.LogicSlabBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DigitBlock extends LogicSlabBlock<DigitTileEntity, EmptyContainer> {

    public static PropertyInteger VALUE = PropertyInteger.create("value", 0, 15);

    public DigitBlock() {
        super(RFTools.instance, Material.IRON, DigitTileEntity.class, EmptyContainer.class, "digit_block", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, player, list, advanced);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block is just a simple");
            list.add(TextFormatting.WHITE + "digit that shows the redstone input value");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    protected void checkRedstone(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof DigitTileEntity) {
            DigitTileEntity logicTileEntity = (DigitTileEntity)te;
            EnumFacing inputSide = logicTileEntity.getFacing(world.getBlockState(pos)).getInputSide();
            int power = getInputStrength(world, pos, inputSide);
            int oldPower = logicTileEntity.getPowerLevel();
            logicTileEntity.setPowerInput(power);
            if (oldPower != power) {
                logicTileEntity.markDirtyClient();
//                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return super.canRenderInLayer(state, layer) || layer == BlockRenderLayer.CUTOUT;
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof DigitTileEntity) {
            DigitTileEntity logicTileEntity = (DigitTileEntity)te;
            return super.getActualState(state, world, pos).withProperty(VALUE, logicTileEntity.getPowerLevel());
        }
        return super.getActualState(state, world, pos).withProperty(VALUE, 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOGIC_FACING, META_INTERMEDIATE, VALUE);
    }
}
