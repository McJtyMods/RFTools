package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DialingDeviceBlock extends GenericRFToolsBlock implements Infusable {

    public DialingDeviceBlock() {
        super(Material.iron, DialingDeviceTileEntity.class, EmptyContainer.class, GuiDialingDevice.class, "dialing_device", false);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With the dialing device you can 'dial-up' any");
            list.add(EnumChatFormatting.WHITE + "nearby matter transmitter to any matter receiver");
            list.add(EnumChatFormatting.WHITE + "in the Minecraft universe. This requires power.");
            list.add(EnumChatFormatting.WHITE + "If a Destination Analyzer is adjacent to this block");
            list.add(EnumChatFormatting.WHITE + "you will also be able to check if the destination");
            list.add(EnumChatFormatting.WHITE + "has enough power to be safe.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_DIALING_DEVICE;
    }
}
