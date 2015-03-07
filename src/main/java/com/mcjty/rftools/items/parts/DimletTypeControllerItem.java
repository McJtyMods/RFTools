package com.mcjty.rftools.items.parts;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimletTypeControllerItem extends Item {
    private final Map<DimletType,IIcon> icons = new HashMap<DimletType, IIcon>();

    public DimletTypeControllerItem() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons.put(DimletType.DIMLET_BIOME, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerBiome"));
        icons.put(DimletType.DIMLET_SKY, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerSky"));
        icons.put(DimletType.DIMLET_FEATURE, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerFeature"));
        icons.put(DimletType.DIMLET_CONTROLLER, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerController"));
        icons.put(DimletType.DIMLET_EFFECT, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerEffect"));
        icons.put(DimletType.DIMLET_LIQUID, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerLiquid"));
        icons.put(DimletType.DIMLET_MATERIAL, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerMaterial"));
        icons.put(DimletType.DIMLET_MOBS, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerMobs"));
        icons.put(DimletType.DIMLET_STRUCTURE, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerStructures"));
        icons.put(DimletType.DIMLET_SPECIAL, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerSpecial"));
        icons.put(DimletType.DIMLET_TERRAIN, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerTerrain"));
        icons.put(DimletType.DIMLET_TIME, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerTime"));
        icons.put(DimletType.DIMLET_WEATHER, iconRegister.registerIcon(RFTools.MODID + ":parts/dimletTypeControllerWeather"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Every dimlet needs a type specific controller. You can");
            list.add(EnumChatFormatting.WHITE + "get this by deconstructing other dimlets in the Dimlet");
            list.add(EnumChatFormatting.WHITE + "Workbench. In that same workbench you can also use");
            list.add(EnumChatFormatting.WHITE + "this item to make new dimlets.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return icons.get(DimletType.values()[damage]);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + DimletType.values()[itemStack.getItemDamage()].getName();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (DimletType type : DimletType.values()) {
            if (icons.containsKey(type)) {
                list.add(new ItemStack(ModItems.dimletTypeControllerItem, 1, type.ordinal()));
            }
        }
    }
}
