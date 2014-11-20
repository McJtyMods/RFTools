package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.*;

public class KnownDimlet extends Item {
    private final Map<DimletType, IIcon> icons = new HashMap<DimletType, IIcon>();
    private static Random random = new Random();

    public KnownDimlet() {
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public static int getRandomDimlet() {
        int idx = random.nextInt(KnownDimletConfiguration.dimletIds.size());
        return KnownDimletConfiguration.dimletIds.get(idx);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (DimletType type : DimletType.values()) {
            IIcon icon = iconRegister.registerIcon(RFTools.MODID + ":dimlets/" + type.getTextureName());
            icons.put(type, icon);
        }
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(damage);
        DimletType type = entry.getType();
        return icons.get(type);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(itemStack.getItemDamage());
        DimletType type = entry.getType();
        return type.getName() + " " + entry.getName() + " Dimlet";
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return getUnlocalizedName(itemStack);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (Map.Entry<Integer,DimletEntry> me : KnownDimletConfiguration.idToDimlet.entrySet()) {
            list.add(new ItemStack(ModItems.knownDimlet, 1, me.getKey()));
        }
    }

}
