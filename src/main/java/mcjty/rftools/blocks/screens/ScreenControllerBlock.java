package mcjty.rftools.blocks.screens;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenControllerBlock extends GenericRFToolsBlock<ScreenControllerTileEntity, ScreenControllerContainer> implements Infusable {

    public ScreenControllerBlock() {
        super(Material.IRON, ScreenControllerTileEntity.class, ScreenControllerContainer.class, "screen_controller", false);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREENCONTROLLER;
    }

    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiScreenController.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Before screens can work they need to get power from");
            list.add(TextFormatting.WHITE + "this controller. Even a screen that has only modules");
            list.add(TextFormatting.WHITE + "that require no power will need to have a controller.");
            list.add(TextFormatting.WHITE + "One controller can power many screens as long as they");
            list.add(TextFormatting.WHITE + "are in range.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: increased range for screens.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
