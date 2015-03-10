package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.EmptyContainer;
import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
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

public class MatterReceiverBlock extends GenericContainerBlock implements Infusable {

    private IIcon iconTop;

    public MatterReceiverBlock() {
        super(Material.iron, MatterReceiverTileEntity.class);
        setBlockName("matterReceiverBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            int id = tagCompound.getInteger("destinationId");
            list.add(EnumChatFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "If you place this block anywhere in the world then");
            list.add(EnumChatFormatting.WHITE + "you can dial to it using a Dialing Device. Before");
            list.add(EnumChatFormatting.WHITE + "teleporting to this block make sure to give it power!");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
            String name = matterReceiverTileEntity.getName();
            int id = matterReceiverTileEntity.getId();
            currenttip.add(EnumChatFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_RECEIVER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;
        EmptyContainer matterReceiverContainer = new EmptyContainer(entityPlayer);
        return new GuiMatterReceiver(matterReceiverTileEntity, matterReceiverContainer);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        return onBlockActivatedDefaultWrench(world, x, y, z, player);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float sx, float sy, float sz, int meta) {
        int rc = super.onBlockPlaced(world, x, y, z, side, sx, sy, sz, meta);
        if (world.isRemote) {
            return rc;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);

        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId);

        destinations.getNewId(gc);
        destinations.addDestination(gc);
        destinations.save(world);

        return rc;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        // We don't want what GenericContainerBlock does.
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        restoreBlockFromNBT(world, x, y, z, itemStack);
        if (!world.isRemote) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getTileEntity(x, y, z);
            int id = matterReceiverTileEntity.getId();
            if (id == -1) {
                TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
                GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId);
                id = destinations.getNewId(gc);

                destinations.save(world);
                matterReceiverTileEntity.setId(id);
            }
            matterReceiverTileEntity.updateDestination();
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (world.isRemote) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestination(new Coordinate(x, y, z), world.provider.dimensionId);
        destinations.save(world);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineReceiver");
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
