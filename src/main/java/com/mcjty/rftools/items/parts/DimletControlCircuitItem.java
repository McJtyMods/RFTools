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

public class DimletControlCircuitItem extends Item {
    private final IIcon[] icons = new IIcon[7];

    public DimletControlCircuitItem() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0 ; i < 7 ; i++) {
            icons[i] = iconRegister.registerIcon(RFTools.MODID + ":parts/dimletControlCircuit" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Every dimlet needs one control circuit. You can get");
            list.add(EnumChatFormatting.WHITE + "this by deconstructing other dimlets in the Dimlet");
            list.add(EnumChatFormatting.WHITE + "Workbench. In that same workbench you can also use");
            list.add(EnumChatFormatting.WHITE + "this item to make new dimlets. Note that you need a");
            list.add(EnumChatFormatting.WHITE + "control circuit of the right rarity in order to make");
            list.add(EnumChatFormatting.WHITE + "a dimlet of that rarity.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
        for (int i = 0 ; i < 7 ; i++) {
            list.add(new ItemStack(ModItems.dimletControlCircuitItem, 1, i));
        }
    }
}
