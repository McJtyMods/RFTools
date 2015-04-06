package mcjty.rftools.blocks.dimlets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DimensionEnscriberBlock extends GenericContainerBlock {

    public DimensionEnscriberBlock() {
        super(Material.iron, DimensionEnscriberTileEntity.class);
        setBlockName("dimensionEnscriberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_ENSCRIBER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionEnscriber";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With this device you can construct your dimensions");
            list.add(EnumChatFormatting.WHITE + "by combining specific dimlets into an empty dimension.");
            list.add(EnumChatFormatting.WHITE + "tab. You can also deconstruct dimension tabs to get the");
            list.add(EnumChatFormatting.WHITE + "original dimlets back.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimensionEnscriberTileEntity dimensionEnscriberTileEntity = (DimensionEnscriberTileEntity) tileEntity;
        DimensionEnscriberContainer dimensionEnscriberContainer = new DimensionEnscriberContainer(entityPlayer, dimensionEnscriberTileEntity);
        return new GuiDimensionEnscriber(dimensionEnscriberTileEntity, dimensionEnscriberContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimensionEnscriberContainer(entityPlayer, (DimensionEnscriberTileEntity) tileEntity);
    }



}
