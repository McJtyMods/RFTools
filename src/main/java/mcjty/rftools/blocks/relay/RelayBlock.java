package mcjty.rftools.blocks.relay;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class RelayBlock extends GenericRFToolsBlock /* implements IRedstoneConnectable */ {

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    public RelayBlock() {
        super(Material.IRON, RelayTileEntity.class, EmptyContainer.class, "relay", false);
    }

    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiRelay.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_RELAY;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        checkRedstoneWithTE(world, pos);
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
//        return true;
//    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This device can (based on a redstone signal) limit");
            list.add(TextFormatting.WHITE + "the amount of RF that can go through this. Using this");
            list.add(TextFormatting.WHITE + "you can throttle down (or even disable) a number of");
            list.add(TextFormatting.WHITE + "machines in case power is low.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean enabled = false;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof RelayTileEntity) {
            enabled = ((RelayTileEntity)te).isPowered();
        }
        return state.withProperty(ENABLED, enabled);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ENABLED);
    }
}
