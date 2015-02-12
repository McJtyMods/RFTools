package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenBlock extends GenericContainerBlock {

    public ScreenBlock() {
        super(Material.iron, ScreenTileEntity.class);
        float width = 0.5F;
        float height = 1.0F;
        this.setBlockBounds(0.5F - width, 0.0F, 0.5F - width, 0.5F + width, height, 0.5F + width);
        setBlockName("screenBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            boolean connected = tagCompound.getBoolean("connected");
            if (connected) {
                currenttip.add(EnumChatFormatting.YELLOW + "[CONNECTED]");
            }
            boolean power = tagCompound.getBoolean("powerOn");
            if (power) {
                currenttip.add(EnumChatFormatting.YELLOW + "[POWER]");
            }
            int rfPerTick = ((ScreenTileEntity) accessor.getTileEntity()).getTotalRfPerTick();
            currenttip.add(EnumChatFormatting.GREEN + (power ? "Consuming " : "Needs ") + rfPerTick + " RF/tick");
        }
        return currenttip;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getTileEntity(x, y, z);

        if (screenTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, screenTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getTileEntity(x, y, z);

        if (screenTileEntity != null) {
            for (int i = 0 ; i < screenTileEntity.getSizeInventory() ; i++) {
                screenTileEntity.setInventorySlotContents(i, null);
            }
        }
    }


    @Override
    public String getSideIconName() {
        return "screenFrame_icon";
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        float f = 0.0F;
        float f1 = 1.0F;
        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.125F;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        if (meta == 2) {
            this.setBlockBounds(f2, f, 1.0F - f4, f3, f1, 1.0F);
        }

        if (meta == 3) {
            this.setBlockBounds(f2, f, 0.0F, f3, f1, f4);
        }

        if (meta == 4) {
            this.setBlockBounds(1.0F - f4, f, f2, 1.0F, f1, f3);
        }

        if (meta == 5) {
            this.setBlockBounds(0.0F, f, f2, f4, f1, f3);
        }
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType() {
        return -1;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREEN;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This is a modular screen. As such it doesn't show anything.");
            list.add(EnumChatFormatting.WHITE + "You must insert modules to control what you can see.");
            list.add(EnumChatFormatting.WHITE + "This screen cannot be directly powered. It has to be remotely");
            list.add(EnumChatFormatting.WHITE + "powered by a nearby Screen Controller.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) tileEntity;
        ScreenContainer screenContainer = new ScreenContainer(entityPlayer, screenTileEntity);
        return new GuiScreen(screenTileEntity, screenContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ScreenContainer(entityPlayer, (ScreenTileEntity) tileEntity);
    }

}
