package com.mcjty.rftools.items;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;

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
