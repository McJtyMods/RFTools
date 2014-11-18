package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnownDimlet extends Item {
    private final Map<DimletType, IIcon> icons = new HashMap<DimletType, IIcon>();

    private static final Map<DimletType,Integer> idsPerType = new HashMap<DimletType, Integer>();
    private static final Map<Integer,DimletEntry> entries = new HashMap<Integer, DimletEntry>();

    static {
        for (DimletType type : DimletType.values()) {
            idsPerType.put(type, type.getIdOffset());
        }
    }

    public KnownDimlet() {
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public static void registerDimlet(DimletType type, String name) {
        int id = idsPerType.get(type);
        idsPerType.put(type, id+1);
        entries.put(id, new DimletEntry(type, name));
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
        DimletEntry entry = entries.get(damage);
        DimletType type = entry.getType();
        return icons.get(type);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        DimletEntry entry = entries.get(itemStack.getItemDamage());
        DimletType type = entry.getType();
        return type.getName() + " " + entry.getName() + " Dimlet";
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return getUnlocalizedName(itemStack);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (Map.Entry<Integer,DimletEntry> me : entries.entrySet()) {
            list.add(new ItemStack(ModItems.knownDimlet, 1, me.getKey()));
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
