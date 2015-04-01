package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.api.Infusable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenControllerBlock extends GenericBlock implements Infusable {

    public ScreenControllerBlock() {
        super(Material.iron, ScreenControllerTileEntity.class);
        setBlockName("screenControllerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREENCONTROLLER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Before screens can work they need to get power from");
            list.add(EnumChatFormatting.WHITE + "this controller. Even a screen that has only modules");
            list.add(EnumChatFormatting.WHITE + "that require no power will need to have a controller.");
            list.add(EnumChatFormatting.WHITE + "One controller can power many screens as long as they");
            list.add(EnumChatFormatting.WHITE + "are in range.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: increased range for screens.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ScreenControllerTileEntity screenControllerTileEntity = (ScreenControllerTileEntity) tileEntity;
        ScreenControllerContainer screenControllerContainer = new ScreenControllerContainer(entityPlayer, screenControllerTileEntity);
        return new GuiScreenController(screenControllerTileEntity, screenControllerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ScreenControllerContainer(entityPlayer, (ScreenControllerTileEntity) tileEntity);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof ScreenControllerTileEntity) {
                ((ScreenControllerTileEntity) tileEntity).detach();
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineScreenController";
    }
}
