package com.mcjty.rftools.blocks.environmental;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class EnvironmentalControllerBlock extends GenericContainerBlock implements Infusable {

    public EnvironmentalControllerBlock() {
        super(Material.iron, EnvironmentalControllerTileEntity.class);
        setBlockName("environmentalControllerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            EnvironmentalControllerTileEntity tileEntity = (EnvironmentalControllerTileEntity) accessor.getTileEntity();
            int rfPerTick = tileEntity.getTotalRfPerTick();
            currenttip.add(EnumChatFormatting.GREEN + "Consuming " + rfPerTick + " RF/tick");
            int radius = tileEntity.getRadius();
            int miny = tileEntity.getMiny();
            int maxy = tileEntity.getMaxy();
            currenttip.add(EnumChatFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
            int volume = tileEntity.getVolume();
            currenttip.add(EnumChatFormatting.GREEN + "Volume " + volume + " blocks");
        }
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int radius = tagCompound.getInteger("radius");
            int miny = tagCompound.getInteger("miny");
            int maxy = tagCompound.getInteger("maxy");
            list.add(EnumChatFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Control the environment around you with various");
            list.add(EnumChatFormatting.WHITE + "modules. There are modules for things like regeneration,");
            list.add(EnumChatFormatting.WHITE + "speed...");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        EnvironmentalControllerTileEntity environmentalControllerTileEntity = (EnvironmentalControllerTileEntity) tileEntity;
        EnvironmentalControllerContainer environmentalControllerContainer = new EnvironmentalControllerContainer(entityPlayer, environmentalControllerTileEntity);
        return new GuiEnvironmentalController(environmentalControllerTileEntity, environmentalControllerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new EnvironmentalControllerContainer(entityPlayer, (EnvironmentalControllerTileEntity) tileEntity);
    }


    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return 13;
//        int meta = world.getBlockMetadata(x, y, z);
//        int state = BlockTools.getState(meta);
//        if (state == 0) {
//            return 10;
//        } else {
//            return getLightValue();
//        }
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnvironmentalController";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENVIRONMENTAL_CONTROLLER;
    }
}
