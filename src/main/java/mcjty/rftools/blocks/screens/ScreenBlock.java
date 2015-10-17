package mcjty.rftools.blocks.screens;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.varia.BlockTools;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenBlock extends GenericRFToolsBlock {

    public ScreenBlock(String blockName, Class<? extends ScreenTileEntity> clazz) {
        super(Material.iron, clazz, true);
        float width = 0.5F;
        float height = 1.0F;
        this.setBlockBounds(0.5F - width, 0.0F, 0.5F - width, 0.5F + width, height, 0.5F + width);
        setBlockName(blockName);
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
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(x, y, z);
            screenTileEntity.hitScreenClient(mouseOver.hitVec.xCoord - x, mouseOver.hitVec.yCoord - y, mouseOver.hitVec.zCoord - z, mouseOver.sideHit);
        }
    }

    private void setInvisibleBlockSafe(World world, int x, int y, int z, int dx, int dy, int dz, int meta) {
        int yy = y + dy;
        if (yy < 0 || yy >= world.getHeight()) {
            return;
        }
        int xx = x + dx;
        int zz = z + dz;
        if (world.isAirBlock(xx, yy, zz)) {
            world.setBlock(xx, yy, zz, ScreenSetup.screenHitBlock, meta, 3);
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(xx, yy, zz);
            screenHitTileEntity.setRelativeLocation(-dx, -dy, -dz);
        }
    }

    private void setInvisibleBlocks(World world, int x, int y, int z, int size) {
        int meta = world.getBlockMetadata(x, y, z);

        if (meta == 2) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, x, y, z, -i, -j, 0, meta);
                    }
                }
            }
        }

        if (meta == 3) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, x, y, z, i, -j, 0, meta);
                    }
                }
            }
        }

        if (meta == 4) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, x, y, z, 0, -i, j, meta);
                    }
                }
            }
        }

        if (meta == 5) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, x, y, z, 0, -i, -j, meta);
                    }
                }
            }
        }
    }

    private void clearInvisibleBlockSafe(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getHeight()) {
            return;
        }
        if (world.getBlock(x, y, z) == ScreenSetup.screenHitBlock) {
            world.setBlockToAir(x, y, z);
        }
    }

    private void clearInvisibleBlocks(World world, int x, int y, int z, int meta, int size) {
        if (meta == 2) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, x - i, y - j, z);
                    }
                }
            }
        }

        if (meta == 3) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, x + i, y - j, z);
                    }
                }
            }
        }

        if (meta == 4) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, x, y - i, z + j);
                    }
                }
            }
        }

        if (meta == 5) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, x, y - i, z - j);
                    }
                }
            }
        }
    }

    private static class Setup {
        private final boolean transparent;
        private final int size;

        public Setup(int size, boolean transparent) {
            this.size = size;
            this.transparent = transparent;
        }

        public int getSize() {
            return size;
        }

        public boolean isTransparent() {
            return transparent;
        }
    }

    private static Setup transitions[] = new Setup[] {
            new Setup(ScreenTileEntity.SIZE_NORMAL, false),
            new Setup(ScreenTileEntity.SIZE_NORMAL, true),
            new Setup(ScreenTileEntity.SIZE_LARGE, false),
            new Setup(ScreenTileEntity.SIZE_LARGE, true),
            new Setup(ScreenTileEntity.SIZE_HUGE, false),
            new Setup(ScreenTileEntity.SIZE_HUGE, true),
    };

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        clearInvisibleBlocks(world, x, y, z, meta, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i+1) % transitions.length];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, x, y, z, screenTileEntity.getSize());
                break;
            }
        }
        return true;
    }

    @Override
    protected boolean openGui(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = player.getHeldItem();
        if (itemStack != null && itemStack.getItem() == Items.dye) {
            int damage = itemStack.getItemDamage();
            if (damage < 0) {
                damage = 0;
            } else if (damage > 15) {
                damage = 15;
            }
            int color = ItemDye.field_150922_c[damage];
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(x, y, z);
            screenTileEntity.setColor(color);
            return true;
        } else {
            return super.openGui(world, x, y, z, player);
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + getSideIconName());
    }


    @Override
    public String getSideIconName() {
        return "screenFrame_icon";
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        if (meta == ForgeDirection.NORTH.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
        }

        if (meta == ForgeDirection.SOUTH.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
        }

        if (meta == ForgeDirection.WEST.ordinal()) {
            this.setBlockBounds(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }

        if (meta == ForgeDirection.EAST.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
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

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int size;
            if (tagCompound.hasKey("large")) {
                size = tagCompound.getBoolean("large") ? ScreenTileEntity.SIZE_LARGE : ScreenTileEntity.SIZE_NORMAL;
            } else {
                size = tagCompound.getInteger("size");
            }
            boolean transparent = tagCompound.getBoolean("transparent");
            if (size == ScreenTileEntity.SIZE_HUGE) {
                list.add(EnumChatFormatting.BLUE + "Huge screen.");
            } else if (size == ScreenTileEntity.SIZE_LARGE) {
                list.add(EnumChatFormatting.BLUE + "Large screen.");
            }
            if (transparent) {
                list.add(EnumChatFormatting.BLUE + "Transparent screen.");
            }
            int rc = 0;
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound tag = bufferTagList.getCompoundTagAt(i);
                if (tag != null) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                    if (stack != null) {
                        rc++;
                    }
                }
            }
            list.add(EnumChatFormatting.BLUE + String.valueOf(rc) + " modules");
        }

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
    protected ForgeDirection getOrientation(int x, int y, int z, EntityLivingBase entityLivingBase) {
        return BlockTools.determineOrientationHoriz(entityLivingBase);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);

        if (entityLivingBase instanceof EntityPlayer) {
            Achievements.trigger((EntityPlayer) entityLivingBase, Achievements.clearVision);
        }
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) tileEntity;
            if (screenTileEntity.getSize() > ScreenTileEntity.SIZE_NORMAL) {
                setInvisibleBlocks(world, x, y, z, screenTileEntity.getSize());
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
            if (screenTileEntity.getSize() > ScreenTileEntity.SIZE_NORMAL) {
                clearInvisibleBlocks(world, x, y, z, meta, screenTileEntity.getSize());
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
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
