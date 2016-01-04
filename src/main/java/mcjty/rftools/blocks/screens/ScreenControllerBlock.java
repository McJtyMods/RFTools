package mcjty.rftools.blocks.screens;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenControllerBlock extends GenericRFToolsBlock<ScreenControllerTileEntity, EmptyContainer> implements Infusable {

    public ScreenControllerBlock() {
        super(Material.iron, ScreenControllerTileEntity.class, EmptyContainer.class, "screen_controller", false);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREENCONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Before screens can work they need to get power from");
            list.add(EnumChatFormatting.WHITE + "this controller. Even a screen that has only modules");
            list.add(EnumChatFormatting.WHITE + "that require no power will need to have a controller.");
            list.add(EnumChatFormatting.WHITE + "One controller can power many screens as long as they");
            list.add(EnumChatFormatting.WHITE + "are in range.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: increased range for screens.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ScreenControllerTileEntity) {
                ((ScreenControllerTileEntity) tileEntity).detach();
            }
        }
        super.breakBlock(world, pos, state);
    }
}
