package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnownDimlet extends Item {
    private final Map<DimletType, IIcon> icons = new HashMap<DimletType, IIcon>();

    private final List<DimletEntry> entries = new ArrayList<DimletEntry>();

    public KnownDimlet() {
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public void registerBiomeDimlet(String name) {
        entries.add(new DimletEntry(DimletType.DIMLET_BIOME, name));
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
        DimletType type = DimletType.values()[damage];
        return icons.get(type);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        DimletType type = DimletType.values()[itemStack.getItemDamage()];
        return type.getName() + " Dimlet";
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return getUnlocalizedName(itemStack);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (DimletType type : DimletType.values()) {
            list.add(new ItemStack(ModItems.knownDimlet, 1, type.ordinal()));
        }
    }

    public static class DimletEntry {
        private final DimletType type;
        private final String name;

        public DimletEntry(DimletType type, String name) {
            this.type = type;
            this.name = name;
        }

        public DimletType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }
}
