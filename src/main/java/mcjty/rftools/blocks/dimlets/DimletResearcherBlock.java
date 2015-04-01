package mcjty.rftools.blocks.dimlets;

import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcjty.api.Infusable;
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

public class DimletResearcherBlock extends GenericContainerBlock implements Infusable {

    public DimletResearcherBlock() {
        super(Material.iron, DimletResearcherTileEntity.class);
        setBlockName("dimletResearcherBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMLET_RESEARCHER;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Put unknown dimlets into this machine and it will");
            list.add(EnumChatFormatting.WHITE + "return a random known dimlet.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimletResearcherTileEntity dimletResearcherTileEntity = (DimletResearcherTileEntity) tileEntity;
        DimletResearcherContainer dimletResearcherContainer = new DimletResearcherContainer(entityPlayer, dimletResearcherTileEntity);
        return new GuiDimletResearcher(dimletResearcherTileEntity, dimletResearcherContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimletResearcherContainer(entityPlayer, (DimletResearcherTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineDimletResearcher";
    }
}
