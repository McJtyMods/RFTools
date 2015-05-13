package mcjty.rftools.items.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.sorters.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GenericTypeItem extends StorageTypeItem {
    private List<ItemSorter> sorters = null;

    public GenericTypeItem() {
        setMaxStackSize(16);
    }

    @Override
    public List<ItemSorter> getSorters() {
        if (sorters == null) {
            sorters = new ArrayList<ItemSorter>();
            sorters.add(new NameItemSorter());
            sorters.add(new CountItemSorter());
            sorters.add(new GenericItemSorter());
        }
        return sorters;
    }

    @Override
    public String getLongLabel(ItemStack stack) {
        return stack.getDisplayName();
    }

    @Override
    public String getShortLabel(ItemStack stack) {
        return stack.getDisplayName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This module extends the Modular Storage block");
            list.add(EnumChatFormatting.WHITE + "with support for sorting items in general");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
