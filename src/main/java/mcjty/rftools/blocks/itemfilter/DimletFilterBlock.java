package mcjty.rftools.blocks.itemfilter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DimletFilterBlock extends GenericContainerBlock {

    private IIcon icons[] = new IIcon[6];

    public DimletFilterBlock() {
        super(Material.iron, DimletFilterTileEntity.class);
        setBlockName("dimletFilterBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons[ForgeDirection.DOWN.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterD");
        icons[ForgeDirection.UP.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterU");
        icons[ForgeDirection.NORTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterN");
        icons[ForgeDirection.SOUTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterS");
        icons[ForgeDirection.WEST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterW");
        icons[ForgeDirection.EAST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineDimletFilterE");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            list.add(EnumChatFormatting.GREEN + "Contents: " + bufferTagList.tagCount() + " stacks");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With this block you can filter dimlets from any side");
            list.add(EnumChatFormatting.WHITE + "to any other side.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMLET_FILTER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimletFilterTileEntity dimletFilterTileEntity = (DimletFilterTileEntity) tileEntity;
        DimletFilterContainer dimletFilterContainer = new DimletFilterContainer(entityPlayer, dimletFilterTileEntity);
        return new GuiDimletFilter(dimletFilterTileEntity, dimletFilterContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimletFilterContainer(entityPlayer, (DimletFilterTileEntity) tileEntity);
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return icons[side];
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[side];
    }
}
