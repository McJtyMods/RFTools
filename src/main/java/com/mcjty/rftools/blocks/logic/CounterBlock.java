package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CounterBlock extends LogicSlabBlock {

    public CounterBlock() {
        super(Material.iron, "counterBlock", CounterTileEntity.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int delay = tagCompound.getInteger("counter");
            list.add(EnumChatFormatting.GREEN + "Counter: " + delay);
            int current = tagCompound.getInteger("current");
            list.add(EnumChatFormatting.GREEN + "Current: " + current);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This logic block counts redstone pulses and emits");
            list.add(EnumChatFormatting.WHITE + "a signal once a certain number has been reached.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        TileEntity counterTileEntity = world.getTileEntity(x, y, z);
        counterTileEntity.updateEntity();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int current = tagCompound.getInteger("current");
            currenttip.add(EnumChatFormatting.GREEN + "Current: " + current);
        }
        return currenttip;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_COUNTER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        CounterTileEntity counterTileEntity = (CounterTileEntity) tileEntity;
        return new GuiCounter(counterTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineCounterTop";
    }
}
