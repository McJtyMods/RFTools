package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.render.ModRenderers;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public class MatterTransmitterBlock extends GenericContainerBlock implements Infusable {

    private IIcon iconTop;

    public MatterTransmitterBlock() {
        super(Material.iron, MatterTransmitterTileEntity.class);
        setBlockName("matterTransmitterBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            list.add(EnumChatFormatting.GREEN + "Name: " + name);
            Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
            if (c != null && c.getY() >= 0) {
                list.add(EnumChatFormatting.YELLOW + "[DIALED]");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "If you place this block near a Dialing Device then");
            list.add(EnumChatFormatting.WHITE + "you can dial it to a Matter Receiver. Make sure to give");
            list.add(EnumChatFormatting.WHITE + "it sufficient power!");
            list.add(EnumChatFormatting.WHITE + "If a Destination Analyzer is adjacent to this block");
            list.add(EnumChatFormatting.WHITE + "you will also be able to check if the destination");
            list.add(EnumChatFormatting.WHITE + "has enough power to be safe. The teleportation beam");
            list.add(EnumChatFormatting.WHITE + "turns red if there is a problem. If the beam is");
            list.add(EnumChatFormatting.WHITE + "yellow then the status is unknown (usually because");
            list.add(EnumChatFormatting.WHITE + "the destination dimension is not loaded)");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(EnumChatFormatting.YELLOW + "increased teleportation speed.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
//        NBTTagCompound tagCompound = accessor.getNBTData();
//        if (tagCompound != null) {
//            String name = tagCompound.getString("tpName");
//            currenttip.add(EnumChatFormatting.GREEN + "Name: " + name);
//            Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
//            if (c != null && c.getY() >= 0) {
//                currenttip.add(EnumChatFormatting.YELLOW + "[DIALED]");
//            }
//        }
        TileEntity te = accessor.getTileEntity();
        if (te instanceof MatterTransmitterTileEntity) {
            MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;
            currenttip.add(EnumChatFormatting.GREEN + "Name: " + matterTransmitterTileEntity.getName());
            TeleportDestination dest = matterTransmitterTileEntity.getTeleportDestination();
            if (dest != null && dest.getCoordinate().getY() >= 0) {
                currenttip.add(EnumChatFormatting.YELLOW + "[DIALED]");
            }
        }
        return currenttip;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        Block b = world.getBlock(x, y+1, z);
        if (ModBlocks.teleportBeamBlock.equals(b)) {
            world.setBlockToAir(x, y+1, z);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_TRANSMITTER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) tileEntity;
        EmptyContainer matterTransmitterContainer = new EmptyContainer(entityPlayer);
        return new GuiMatterTransmitter(matterTransmitterTileEntity, matterTransmitterContainer);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        // We don't want what GenericContainerBlock does.
        restoreBlockFromNBT(world, x, y, z, itemStack);

        // Restore the transmitter beam if needed.
        MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) world.getTileEntity(x, y, z);
        if (matterTransmitterTileEntity.getTeleportDestination() != null && matterTransmitterTileEntity.getTeleportDestination().isValid()) {
            DialingDeviceTileEntity.makeBeam(new Coordinate(x, y, z), world, 1, 4, 2);
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTransmitter");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else {
            return iconSide;
        }
    }

}
