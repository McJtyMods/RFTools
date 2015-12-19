package mcjty.rftools.items.storage;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.sorters.CountItemSorter;
import mcjty.rftools.blocks.storage.sorters.ItemSorter;
import mcjty.rftools.blocks.storage.sorters.NameItemSorter;
import mcjty.rftools.blocks.storage.sorters.OreTypeItemSorter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class OreDictTypeItem extends StorageTypeItem {
    private List<ItemSorter> sorters = null;

    public OreDictTypeItem() {
        setMaxStackSize(16);
    }

    @Override
    public List<ItemSorter> getSorters() {
        if (sorters == null) {
            sorters = new ArrayList<>();
            sorters.add(new NameItemSorter());
            sorters.add(new CountItemSorter());
            sorters.add(new OreTypeItemSorter());
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
            list.add(EnumChatFormatting.WHITE + "with Ore dictionary specific capabilities");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
