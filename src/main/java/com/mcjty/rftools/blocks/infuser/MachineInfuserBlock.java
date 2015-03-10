package com.mcjty.rftools.blocks.infuser;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MachineInfuserBlock extends GenericContainerBlock implements Infusable {

    public MachineInfuserBlock() {
        super(Material.iron, MachineInfuserTileEntity.class);
        setBlockName("machineInfuserBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MACHINE_INFUSER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With this machine you can improve most other machines");
            list.add(EnumChatFormatting.WHITE + "in RFTools in various ways. This needs dimensional");
            list.add(EnumChatFormatting.WHITE + "shards.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        MachineInfuserTileEntity machineInfuserTileEntity = (MachineInfuserTileEntity) tileEntity;
        MachineInfuserContainer machineInfuserContainer = new MachineInfuserContainer(entityPlayer, machineInfuserTileEntity);
        return new GuiMachineInfuser(machineInfuserTileEntity, machineInfuserContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new MachineInfuserContainer(entityPlayer, (MachineInfuserTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineMachineInfuser";
    }
}
