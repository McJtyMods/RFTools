package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceProjectorBlock extends GenericContainerBlock {

    private IIcon iconTop;

    public SpaceProjectorBlock() {
        super(Material.iron, SpaceProjectorTileEntity.class);
        setBlockName("spaceProjectorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstoneWithTE(world, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This block is linked to a space chamber and");
            list.add(EnumChatFormatting.WHITE + "can project it on top of this block. Insert");
            list.add(EnumChatFormatting.WHITE + "a chamber card to make a projection");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SPACE_PROJECTOR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        SpaceProjectorTileEntity spaceProjectorTileEntity = (SpaceProjectorTileEntity) tileEntity;
        SpaceProjectorContainer spaceProjectorContainer = new SpaceProjectorContainer(entityPlayer, spaceProjectorTileEntity);
        return new GuiSpaceProjector(spaceProjectorTileEntity, spaceProjectorContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new SpaceProjectorContainer(entityPlayer, (SpaceProjectorTileEntity) tileEntity);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSpaceProjector");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBottom");
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
