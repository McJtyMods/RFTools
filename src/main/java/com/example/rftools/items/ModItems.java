package com.example.rftools.items;

import com.example.rftools.RFTools;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ModItems {
    public static NetworkMonitorItem networkMonitorItem;


    public static final void init() {
        networkMonitorItem = new NetworkMonitorItem();
        networkMonitorItem.setUnlocalizedName("NetworkMonitor");
        networkMonitorItem.setCreativeTab(CreativeTabs.tabMisc);
        networkMonitorItem.setTextureName(RFTools.MODID + ":networkMonitorItem");
        GameRegistry.registerItem(networkMonitorItem, "networkMonitorItem");
    }
}
