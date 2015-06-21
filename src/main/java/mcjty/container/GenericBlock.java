package mcjty.container;

import cofh.api.item.IToolHammer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.apideps.WailaInfoProvider;
import mcjty.rftools.apideps.WrenchChecker;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.security.SecurityCardItem;
import mcjty.rftools.blocks.security.SecurityChannels;
import mcjty.rftools.items.smartwrench.SmartWrench;
import mcjty.rftools.items.smartwrench.SmartWrenchMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
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
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericBlock extends Block implements ITileEntityProvider, WailaInfoProvider {

    protected IIcon iconInd;        // The identifying face of the block (front by default but can be different).
    protected IIcon iconSide;
    protected IIcon iconTop;
    protected IIcon iconBottom;
    protected final Class<? extends TileEntity> tileEntityClass;

    private boolean creative;

    // Set this to true in case horizontal rotation is used (2 bits rotation as opposed to 3).
    private boolean horizRotation = false;

    public GenericBlock(Material material, Class<? extends TileEntity> tileEntityClass) {
        super(material);
        this.creative = false;
        this.tileEntityClass = tileEntityClass;
        setHardness(2.0f);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
    }

    public boolean isHorizRotation() {
        return horizRotation;
    }

    public void setHorizRotation(boolean horizRotation) {
        this.horizRotation = horizRotation;
    }

    public boolean isCreative() {
        return creative;
    }

    public void setCreative(boolean creative) {
        this.creative = creative;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        Block block = accessor.getBlock();
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof GenericTileEntity) {
            GenericTileEntity genericTileEntity = (GenericTileEntity) tileEntity;
            if (block instanceof Infusable) {
                int infused = genericTileEntity.getInfused();
                int pct = infused * 100 / DimletConfiguration.maxInfuse;
                currenttip.add(EnumChatFormatting.YELLOW + "Infused: " + pct + "%");
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                if (genericTileEntity.getOwnerUUID() != null) {
                    int securityChannel = genericTileEntity.getSecurityChannel();
                    if (securityChannel == -1) {
                        currenttip.add(EnumChatFormatting.YELLOW + "Owned by: " + genericTileEntity.getOwnerName());
                    } else {
                        currenttip.add(EnumChatFormatting.YELLOW + "Owned by: " + genericTileEntity.getOwnerName() + " (channel " + securityChannel + ")");
                    }
                }
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
            if (tagCompound.hasKey("ownerM")) {
                String owner = tagCompound.getString("owner");
                int securityChannel = -1;
                if (tagCompound.hasKey("secChannel")) {
                    securityChannel = tagCompound.getInteger("secChannel");
                }

                if (securityChannel == -1) {
                    list.add(EnumChatFormatting.YELLOW + "Owned by: " + owner);
                } else {
                    list.add(EnumChatFormatting.YELLOW + "Owned by: " + owner + " (channel " + securityChannel + ")");
                }
            }
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (tileEntity instanceof GenericTileEntity) {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            NBTTagCompound tagCompound = new NBTTagCompound();
            ((GenericTileEntity)tileEntity).writeRestorableToNBT(tagCompound);

            stack.setTagCompound(tagCompound);
            ArrayList<ItemStack> result = new ArrayList<ItemStack>();
            result.add(stack);
            return result;
        } else {
            return super.getDrops(world, x, y, z, metadata, fortune);
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (willHarvest) return true; // If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(world, player, x, y, z, meta);
        world.setBlockToAir(x, y, z);
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
    private WrenchUsage testWrenchUsage(int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = player.getHeldItem();
        WrenchUsage wrenchUsed = WrenchUsage.NOT;
        if (itemStack != null) {
            Item item = itemStack.getItem();
            if (item != null) {
                if (item instanceof IToolHammer) {
                    IToolHammer hammer = (IToolHammer) item;
                    if (hammer.isUsable(itemStack, player, x, y, z)) {
                        hammer.toolUsed(itemStack, player, x, y, z);
                        wrenchUsed = WrenchUsage.NORMAL;
                    } else {
                        // It is still possible it is a smart wrench.
                        if (item instanceof SmartWrench) {
                            SmartWrench smartWrench = (SmartWrench) item;
                            SmartWrenchMode mode = smartWrench.getMode(itemStack);
                            if (mode.equals(SmartWrenchMode.MODE_SELECT)) {
                                if (player.isSneaking()) {
                                    return WrenchUsage.SNEAK_SELECT;
                                } else {
                                    return WrenchUsage.SELECT;
                                }
                            }
                        }
                        wrenchUsed = WrenchUsage.DISABLED;
                    }
                } else if (WrenchChecker.isAWrench(item)) {
                    wrenchUsed = WrenchUsage.NORMAL;
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
        WrenchUsage wrenchUsed = testWrenchUsage(x, y, z, player);
        switch (wrenchUsed) {
            case NOT:          return openGui(world, x, y, z, player);
            case NORMAL:       return wrenchUse(world, x, y, z, player);
            case SNEAKING:     return wrenchSneak(world, x, y, z, player);
            case DISABLED:     return wrenchDisabled(world, x, y, z, player);
            case SELECT:       return wrenchSelect(world, x, y, z, player);
            case SNEAK_SELECT: return wrenchSneakSelect(world, x, y, z, player);
        }
        return false;
    }

    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        rotateBlock(world, x, y, z);
        return true;
    }

    protected boolean wrenchSneak(World world, int x, int y, int z, EntityPlayer player) {
        breakAndRemember(world, player, x, y, z);
        return true;
    }

    protected boolean wrenchDisabled(World world, int x, int y, int z, EntityPlayer player) {
        return false;
    }

    protected boolean wrenchSelect(World world, int x, int y, int z, EntityPlayer player) {
        return false;
    }

    protected boolean wrenchSneakSelect(World world, int x, int y, int z, EntityPlayer player) {
        return false;
    }

    protected boolean openGui(World world, int x, int y, int z, EntityPlayer player) {
        if (getGuiID() != -1) {
            if (world.isRemote) {
                return true;
            }
            TileEntity te = world.getTileEntity(x, y, z);
            if (isBlockContainer && !tileEntityClass.isInstance(te)) {
                return true;
            }
            if (checkAccess(world, player, te)) return true;
            player.openGui(RFTools.instance, getGuiID(), world, x, y, z);
        }
        return true;
    }

    private boolean checkAccess(World world, EntityPlayer player, TileEntity te) {
        if (te instanceof GenericTileEntity) {
            GenericTileEntity genericTileEntity = (GenericTileEntity) te;
            if ((!SecurityCardItem.isAdmin(player)) && (!player.getPersistentID().equals(genericTileEntity.getOwnerUUID()))) {
                int securityChannel = genericTileEntity.getSecurityChannel();
                if (securityChannel != -1) {
                    SecurityChannels securityChannels = SecurityChannels.getChannels(world);
                    SecurityChannels.SecurityChannel channel = securityChannels.getChannel(securityChannel);
                    boolean playerListed = channel.getPlayers().contains(player.getDisplayName());
                    if (channel.isWhitelist() != playerListed) {
                        RFTools.message(player, EnumChatFormatting.RED + "You have no permission to use this block!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected ForgeDirection getOrientation(int x, int y, int z, EntityLivingBase entityLivingBase) {
        if (horizRotation) {
            return BlockTools.determineOrientationHoriz(entityLivingBase);
        } else {
            return BlockTools.determineOrientation(x, y, z, entityLivingBase);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        ForgeDirection dir = getOrientation(x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        if (horizRotation) {
            int power = world.isBlockProvidingPowerTo(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.ordinal());
            meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientationHoriz(meta, dir), 2);
        } else {
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, dir), 2);
        }
        restoreBlockFromNBT(world, x, y, z, itemStack);
        if (!world.isRemote) {
            setOwner(world, x, y, z, entityLivingBase);
        }
    }

    protected void setOwner(World world, int x, int y, int z, EntityLivingBase entityLivingBase) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof GenericTileEntity && entityLivingBase instanceof EntityPlayer) {
            GenericTileEntity genericTileEntity = (GenericTileEntity) te;
            EntityPlayer player = (EntityPlayer) entityLivingBase;
            genericTileEntity.setOwner(player);
        }
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
     * Check the redstone level reaching this block. This version sends the
     * signal directly to the TE.
     * @param world
     * @param x
     * @param y
     * @param z
     */
    protected void checkRedstoneWithTE(World world, int x, int y, int z) {
//        int powered = world.getStrongestIndirectPower(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof GenericTileEntity) {
            int powered = world.getBlockPowerInput(x, y, z);
            GenericTileEntity genericTileEntity = (GenericTileEntity) te;
            genericTileEntity.setPowered(powered);
        }
    }

    /**
     * Break a block in the world, convert it to an entity and remember all the settings
     * for this block in the itemstack.
     */
    protected void breakAndRemember(World world, EntityPlayer player, int x, int y, int z) {
        if (!world.isRemote) {
            harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));
//            world.setBlockToAir(x, y, z);
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
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + getSideIconName());
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + getTopIconName());
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + getBottomIconName());
    }

    public String getSideIconName() {
        if (creative) {
            return "machineSideC";
        } else {
            return "machineSide";
        }
    }

    public String getTopIconName() {
        if (creative) {
            return "machineSideC";
        } else {
            return "machineTop";
        }
    }

    public String getBottomIconName() {
        if (creative) {
            return "machineSideC";
        } else {
            return "machineBottom";
        }
    }
    /**
     * Return the name of the icon to be used for the front side of the machine.
     */
    public String getIdentifyingIconName() {
        return null;
    }

    /**
     * Return the id of the gui to use for this block.
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
        ForgeDirection k = getOrientation(meta);
        if (iconInd != null && side == k.ordinal()) {
            return getIconInd(blockAccess, x, y, z, meta);
        } else if (iconTop != null && side == BlockTools.getTopDirection(k).ordinal()) {
            return iconTop;
        } else if (iconBottom != null && side ==  BlockTools.getBottomDirection(k).ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    public ForgeDirection getOrientation(int meta) {
        ForgeDirection k;
        if (horizRotation) {
            k = BlockTools.getOrientationHoriz(meta);
        } else {
            k = BlockTools.getOrientation(meta);
        }
        return k;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (iconInd != null && side == ForgeDirection.SOUTH.ordinal()) {
            return iconInd;
        } else if (iconTop != null && side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (iconBottom != null && side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    public IIcon getIconInd(IBlockAccess blockAccess, int x, int y, int z, int meta) {
        return iconInd;
    }
}
