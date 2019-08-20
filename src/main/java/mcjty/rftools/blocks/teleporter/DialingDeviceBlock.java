package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;


import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

public class DialingDeviceBlock extends GenericRFToolsBlock<DialingDeviceTileEntity, EmptyContainer> implements Infusable {

    public DialingDeviceBlock() {
        super(Material.IRON, DialingDeviceTileEntity.class, EmptyContainer::new, "dialing_device", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<DialingDeviceTileEntity, EmptyContainer, GenericGuiContainer<? super DialingDeviceTileEntity>> getGuiFactory() {
        return GuiDialingDevice::new;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "With the dialing device you can 'dial-up' any");
            list.add(TextFormatting.WHITE + "nearby matter transmitter to any matter receiver");
            list.add(TextFormatting.WHITE + "in the Minecraft universe. This requires power.");
            list.add(TextFormatting.WHITE + "If a Destination Analyzer is adjacent to this block");
            list.add(TextFormatting.WHITE + "you will also be able to check if the destination");
            list.add(TextFormatting.WHITE + "has enough power to be safe.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }


    @Override
    public int getGuiID() {
        return GuiProxy.GUI_DIALING_DEVICE;
    }
}
