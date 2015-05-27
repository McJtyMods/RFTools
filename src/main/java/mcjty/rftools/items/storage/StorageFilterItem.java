package mcjty.rftools.items.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageFilterItem extends Item {

    public StorageFilterItem() {
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This filter module is for the Modular Storage block.");
            list.add(EnumChatFormatting.WHITE + "This module can make sure the storage block only accepts");
            list.add(EnumChatFormatting.WHITE + "certain types of items");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_STORAGE_FILTER, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

}
