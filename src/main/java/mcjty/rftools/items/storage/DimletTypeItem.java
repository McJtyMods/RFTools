package mcjty.rftools.items.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.storage.sorters.*;
import mcjty.rftools.items.dimlets.DimletEntry;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DimletTypeItem extends StorageTypeItem {
    private static final Pattern PATTERN = Pattern.compile(" Dimlet", Pattern.LITERAL);
    private List<ItemSorter> sorters = null;

    public DimletTypeItem() {
        setMaxStackSize(16);
    }

    @Override
    public List<ItemSorter> getSorters() {
        if (sorters == null) {
            sorters = new ArrayList<ItemSorter>();
            sorters.add(new NameItemSorter());
            sorters.add(new CountItemSorter());
            sorters.add(new DimletTypeItemSorter());
            sorters.add(new DimletRarityItemSorter());
        }
        return sorters;
    }

    @Override
    public String getLongLabel(ItemStack stack) {
        if (stack.getItem() == DimletSetup.knownDimlet) {
            DimletKey key = KnownDimletConfiguration.getDimletKey(stack, null);
            if (key != null) {
                DimletEntry entry = KnownDimletConfiguration.getEntry(key);
                if (entry != null) {
                    return PATTERN.matcher(stack.getDisplayName()).replaceAll("") + " (R" + entry.getRarity() + ")";
                }
            }
        }
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
            list.add(EnumChatFormatting.WHITE + "with Dimlet specific capabilities");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
