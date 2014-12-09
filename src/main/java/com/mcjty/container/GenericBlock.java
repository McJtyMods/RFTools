package com.mcjty.container;

import cofh.api.item.IToolHammer;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.apideps.WrenchChecker;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.Infusable;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public abstract class GenericBlock extends Block implements ITileEntityProvider {

    protected IIcon iconInd;        // The identifying face of the block (front by default but can be different).
    protected IIcon iconSide;
    protected final Class<? extends TileEntity> tileEntityClass;

    // Set this to true in case horizontal rotation is used (2 bits rotation as opposed to 3).
    protected boolean horizRotation = false;

    public GenericBlock(Material material, Class<? extends TileEntity> tileEntityClass) {
        super(material);
        this.tileEntityClass = tileEntityClass;
    }

    public boolean isHorizRotation() {
        return horizRotation;
    }

    public void setHorizRotation(boolean horizRotation) {
        this.horizRotation = horizRotation;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        if (block instanceof Infusable) {
            NBTTagCompound tagCompound = accessor.getNBTData();
            if (tagCompound != null) {
                int infused = tagCompound.getInteger("infused");
                int pct = infused * 100 / DimletConfiguration.maxInfuse;
                currenttip.add(EnumChatFormatting.YELLOW + "Infused: " + pct + "%");
            }
        }
        return currenttip;
    }


    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            if (tagCompound.hasKey("Energy")) {
                int energy = tagCompound.getInteger("Energy");
                list.add(EnumChatFormatting.GREEN + "Energy: " + energy + " rf");
            }
            if (this instanceof Infusable) {
                int infused = tagCompound.getInteger("infused");
                int pct = infused * 100 / DimletConfiguration.maxInfuse;
                list.add(EnumChatFormatting.YELLOW + "Infused: " + pct + "%");
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        try {
            return tileEntityClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // This if this block was activated with a wrench
    protected WrenchUsage testWrenchUsage(int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = player.getHeldItem();
        WrenchUsage wrenchUsed = WrenchUsage.NOT;
        if (itemStack != null) {
            Item item = itemStack.getItem();
            if (item != null) {
                if (item instanceof IToolHammer) {
                    IToolHammer hammer = (IToolHammer) item;
                    hammer.toolUsed(itemStack, player, x, y, z);
                    wrenchUsed = WrenchUsage.NORMAL;
                } else if (WrenchChecker.isAWrench(item)) {
                    wrenchUsed = WrenchUsage.NORMAL;
//                } else if (BuildCraftChecker.isBuildcraftPresent() && BuildCraftChecker.isBuildcraftWrench(item)) {
//                    BuildCraftChecker.useBuildcraftWrench(item, player, x, y, z);
//                    wrenchUsed = WrenchUsage.NORMAL;
                }
            }
        }
        if (wrenchUsed == WrenchUsage.NORMAL && player.isSneaking()) {
            wrenchUsed = WrenchUsage.SNEAKING;
        }
        return wrenchUsed;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        return onBlockActivatedDefaultWrench(world, x, y, z, player);
    }

    protected boolean openGui(World world, int x, int y, int z, EntityPlayer player) {
        if (getGuiID() != -1) {
            if (isBlockContainer) {
                TileEntity te = world.getTileEntity(x, y, z);
                if (!tileEntityClass.isInstance(te)) {
                    return true;
                }
                if (world.isRemote) {
                    return true;
                }
                player.openGui(RFTools.instance, getGuiID(), world, x, y, z);

            } else {
                if (world.isRemote) {
                    player.openGui(RFTools.instance, getGuiID(), player.worldObj, x, y, z);
                }
            }
        }
        return true;
    }

    /**
     * In your onBlockActivated implementation you can use this method to get the default wrench usage (rotate/pick up with
     * remembering).
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player
     * @return
     */
    protected boolean onBlockActivatedDefaultWrench(World world, int x, int y, int z, EntityPlayer player) {
        WrenchUsage wrenchUsed = testWrenchUsage(x, y, z, player);
        if (wrenchUsed == WrenchUsage.NORMAL) {
            rotateBlock(world, x, y, z);
            return true;
        } else if (wrenchUsed == WrenchUsage.SNEAKING) {
            breakAndRemember(world, x, y, z);
            return true;
        } else {
            return openGui(world, x, y, z, player);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        if (horizRotation) {
            ForgeDirection dir = BlockTools.determineOrientationHoriz(entityLivingBase);
            int meta = world.getBlockMetadata(x, y, z);
            int power = world.isBlockProvidingPowerTo(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.ordinal());
            meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientationHoriz(meta, dir), 2);
        } else {
            ForgeDirection dir = BlockTools.determineOrientation(x, y, z, entityLivingBase);
            int meta = world.getBlockMetadata(x, y, z);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, dir), 2);
        }
        restoreBlockFromNBT(world, x, y, z, itemStack);
    }

    /**
     * Rotate this block. Typically when a wrench is used on this block.
     * @param world
     * @param x
     * @param y
     * @param z
     */
    protected void rotateBlock(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (horizRotation) {
            ForgeDirection dir = BlockTools.getOrientationHoriz(meta);
            dir = dir.getRotation(ForgeDirection.UP);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientationHoriz(meta, dir), 2);
        } else {
            ForgeDirection dir = BlockTools.getOrientation(meta);
            dir = dir.getRotation(ForgeDirection.UP);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, dir), 2);
        }
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    /**
     * Check the redstone level reaching this block. Correctly checks for horizRotation mode.
     * @param world
     * @param x
     * @param y
     * @param z
     */
    protected void checkRedstone(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
//        int powered = world.getStrongestIndirectPower(x, y, z);
        int powered = world.getBlockPowerInput(x, y, z);
        if (horizRotation) {
            meta = BlockTools.setRedstoneSignalIn(meta, powered > 0);
        } else {
            meta = BlockTools.setRedstoneSignal(meta, powered > 0);
        }
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    /**
     * Override this method if you want to get notified right before this block is
     * broken with a wrench (possibly to avoid spilling contents since it will be remembered).
     */
    protected void breakWithWrench(World world, int x, int y, int z) {
    }

    /**
     * Break a block in the world, convert it to an entity and remember all the settings
     * for this block in the itemstack.
     * @param world
     * @param x
     * @param y
     * @param z
     */
    protected void breakAndRemember(World world, int x, int y, int z) {
        if (!world.isRemote) {
            Block block = world.getBlock(x, y, z);
            TileEntity te = world.getTileEntity(x, y, z);
            ItemStack stack = new ItemStack(block);
            if (te instanceof GenericTileEntity) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                ((GenericTileEntity)te).writeRestorableToNBT(tagCompound);
                stack.setTagCompound(tagCompound);
            }
            breakWithWrench(world, x, y, z);
            world.setBlockToAir(x, y, z);
            world.spawnEntityInWorld(new EntityItem(world, x, y, z, stack));
        }
    }

    /**
     * Restore a block from an itemstack (with NBT).
     * @param world
     * @param x
     * @param y
     * @param z
     * @param itemStack
     */
    protected void restoreBlockFromNBT(World world, int x, int y, int z, ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof GenericTileEntity) {
                ((GenericTileEntity)te).readRestorableFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        if (getIdentifyingIconName() != null) {
            iconInd = iconRegister.registerIcon(RFTools.MODID + ":" + getIdentifyingIconName());
        }
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    /**
     * Return the name of the icon to be used for the front side of the machine.
     * @return
     */
    public String getIdentifyingIconName() {
        return null;
    }

    /**
     * Return the id of the gui to use for this block.
     * @return
     */
    public abstract int getGuiID();

    /**
     * Return a server side container for opening the GUI.
     */
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new EmptyContainer(entityPlayer);
    }

    /**
     * Return a client side gui for this block.
     */
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return null;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientation(meta);
        if (iconInd != null && side == k.ordinal()) {
            return iconInd;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (iconInd != null && side == ForgeDirection.SOUTH.ordinal()) {
            return iconInd;
        } else {
            return iconSide;
        }
    }
}
