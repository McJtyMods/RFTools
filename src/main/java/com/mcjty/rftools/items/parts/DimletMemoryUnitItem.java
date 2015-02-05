package com.mcjty.rftools.items.parts;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DimletMemoryUnitItem extends Item {
    private final IIcon[] icons = new IIcon[3];

    public DimletMemoryUnitItem() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0 ; i < 3 ; i++) {
            icons[i] = iconRegister.registerIcon(RFTools.MODID + ":parts/dimletMemoryUnit" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Every dimlet needs a memory unit. You can get");
            list.add(EnumChatFormatting.WHITE + "this by deconstructing other dimlets in the Dimlet");
            list.add(EnumChatFormatting.WHITE + "Workbench. In that same workbench you can also use");
            list.add(EnumChatFormatting.WHITE + "this item to make new dimlets. The basic memory unit");
            list.add(EnumChatFormatting.WHITE + "is used for dimlets of rarity 0 and 1, the regular for");
            list.add(EnumChatFormatting.WHITE + "rarity 2 and 3 and the advanced for the higher rarities.");
        } else {
            list.add(EnumChatFormatting.WHITE + "Press Shift for more");
        }
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return icons[damage];
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (int i = 0 ; i < 3 ; i++) {
            list.add(new ItemStack(ModItems.dimletMemoryUnitItem, 1, i));
        }
    }
}
