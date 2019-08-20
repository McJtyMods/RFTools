package mcjty.rftools.blocks.screens;

import mcjty.lib.api.Infusable;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

public class ScreenControllerBlock extends GenericRFToolsBlock<ScreenControllerTileEntity, ScreenControllerContainer> implements Infusable {

    public ScreenControllerBlock() {
        super(Material.IRON, ScreenControllerTileEntity.class, ScreenControllerContainer::new, "screen_controller", false);
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_SCREENCONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<ScreenControllerTileEntity, ScreenControllerContainer, GenericGuiContainer<? super ScreenControllerTileEntity>> getGuiFactory() {
        return GuiScreenController::new;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Before screens can work they need to get power from");
            list.add(TextFormatting.WHITE + "this controller. Even a screen that has only modules");
            list.add(TextFormatting.WHITE + "that require no power will need to have a controller.");
            list.add(TextFormatting.WHITE + "One controller can power many screens as long as they");
            list.add(TextFormatting.WHITE + "are in range.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: increased range for screens.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ScreenControllerTileEntity) {
                ((ScreenControllerTileEntity) tileEntity).detach();
            }
        }
        super.breakBlock(world, pos, state);
    }
}
