package com.mcjty.rftools.apideps;

import com.mcjty.container.GenericBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class WailaCompatibility implements IWailaDataProvider {

    public static final WailaCompatibility INSTANCE = new WailaCompatibility();

    private WailaCompatibility() {}

    public static void load(IWailaRegistrar registrar) {
        registrar.registerHeadProvider(INSTANCE, GenericBlock.class);
        registrar.registerBodyProvider(INSTANCE, GenericBlock.class);
        registrar.registerTailProvider(INSTANCE, GenericBlock.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        NBTTagCompound tagCompound = accessor.getNBTData();
//        if (tagCompound != null && tagCompound.hasKey("Energy")) {
//            int energy = tagCompound.getInteger("Energy");
//            currenttip.add(EnumChatFormatting.GREEN + "Energy: " + energy + " rf");
//        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}