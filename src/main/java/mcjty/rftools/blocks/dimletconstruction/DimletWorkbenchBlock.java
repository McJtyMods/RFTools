package mcjty.rftools.blocks.dimletconstruction;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DimletWorkbenchBlock extends GenericRFToolsBlock implements Infusable {
    private IIcon iconTop;
    private IIcon iconBottom;

    public DimletWorkbenchBlock() {
        super(Material.iron, DimletWorkbenchTileEntity.class, true);
        setBlockName("dimletWorkbenchBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With this workbench you can deconstruct dimlets");
            list.add(EnumChatFormatting.WHITE + "into individual parts and also reconstruct new dimlets");
            list.add(EnumChatFormatting.WHITE + "out of these parts.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: increased chance of getting");
            list.add(EnumChatFormatting.YELLOW + "all parts out of the deconstructed dimlet.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMLET_WORKBENCH;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimletWorkbenchTileEntity dimletWorkbenchTileEntity = (DimletWorkbenchTileEntity) tileEntity;
        DimletWorkbenchContainer dimletWorkbenchContainer = new DimletWorkbenchContainer(entityPlayer, dimletWorkbenchTileEntity);
        return new GuiDimletWorkbench(dimletWorkbenchTileEntity, dimletWorkbenchContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimletWorkbenchContainer(entityPlayer, (DimletWorkbenchTileEntity) tileEntity);
    }


    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":machineDimletWorkbenchTop");
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + getSideIconName());
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":machineDimletWorkbenchSide");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }
}
