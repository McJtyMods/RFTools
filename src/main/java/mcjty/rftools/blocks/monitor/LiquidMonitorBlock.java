package mcjty.rftools.blocks.monitor;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class LiquidMonitorBlock extends GenericRFToolsBlock<LiquidMonitorBlockTileEntity, EmptyContainer> {

    @Deprecated
    public static PropertyBool OUTPUTPOWER = PropertyBool.create("output");

    public static PropertyInteger LEVEL = PropertyInteger.create("level", 0, 5);

    public LiquidMonitorBlock() {
        super(Material.IRON, LiquidMonitorBlockTileEntity.class, EmptyContainer.class, "liquid_monitor", false);
    }

    @Override
    public boolean hasRedstoneOutput() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        StateMap.Builder ignorePower = new StateMap.Builder().ignore(OUTPUTPOWER);
        ModelLoader.setCustomStateMapper(this, ignorePower.build());
    }

    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiLiquidMonitor.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_LIQUID_MONITOR;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing direction = state.getValue(FACING);
        if (side == direction) {
            TileEntity te = world.getTileEntity(pos);
            if (state.getBlock() instanceof LiquidMonitorBlock && te instanceof LiquidMonitorBlockTileEntity) {
                LiquidMonitorBlockTileEntity monitorTileEntity = (LiquidMonitorBlockTileEntity) te;
                return monitorTileEntity.isPowered() ? 15 : 0;
            }
        }
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        int level = 0;
        if (te instanceof LiquidMonitorBlockTileEntity) {
            level = ((LiquidMonitorBlockTileEntity) te).getFluidLevel();
        }
        return state.withProperty(LEVEL, level);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, getFacing(meta & 7)).withProperty(OUTPUTPOWER, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() + (state.getValue(OUTPUTPOWER) ? 8 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, OUTPUTPOWER, LEVEL);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This device monitors the amount of liquid in an adjacent");
            list.add(TextFormatting.WHITE + "machine (select it with the GUI). It can also send");
            list.add(TextFormatting.WHITE + "out a redstone signal if the liquid goes above or below");
            list.add(TextFormatting.WHITE + "some value.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
