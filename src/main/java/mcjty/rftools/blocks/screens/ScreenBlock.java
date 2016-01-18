package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScreenBlock extends GenericRFToolsBlock<ScreenTileEntity, ScreenContainer> {

    public ScreenBlock() {
        super(Material.iron, ScreenTileEntity.class, ScreenContainer.class, "screen", true);
        float width = 0.5F;
        float height = 1.0F;
        this.setBlockBounds(0.5F - width, 0.0F, 0.5F - width, 0.5F + width, height, 0.5F + width);
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

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTileEntity.class, new ScreenRenderer());
        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(this), 0, ScreenTileEntity.class);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world.isRemote) {
            MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
            screenTileEntity.hitScreenClient(mouseOver.hitVec.xCoord - pos.getX(), mouseOver.hitVec.yCoord - pos.getY(), mouseOver.hitVec.zCoord - pos.getZ(), mouseOver.sideHit);
        }
    }

    private void setInvisibleBlockSafe(World world, BlockPos pos, int dx, int dy, int dz, int meta) {
        int yy = pos.getY() + dy;
        if (yy < 0 || yy >= world.getHeight()) {
            return;
        }
        int xx = pos.getX() + dx;
        int zz = pos.getZ() + dz;
        BlockPos posO = new BlockPos(xx, yy, zz);
        if (world.isAirBlock(posO)) {
            world.setBlockState(posO, ScreenSetup.screenHitBlock.getStateFromMeta(meta), 3);
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(posO);
            screenHitTileEntity.setRelativeLocation(-dx, -dy, -dz);
        }
    }

    private void setInvisibleBlocks(World world, BlockPos pos, int size) {
        IBlockState state = world.getBlockState(pos);
        int meta = state.getBlock().getMetaFromState(state);

        if (meta == 2) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, pos, -i, -j, 0, meta);
                    }
                }
            }
        }

        if (meta == 3) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, pos, i, -j, 0, meta);
                    }
                }
            }
        }

        if (meta == 4) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, pos, 0, -i, j, meta);
                    }
                }
            }
        }

        if (meta == 5) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        setInvisibleBlockSafe(world, pos, 0, -i, -j, meta);
                    }
                }
            }
        }
    }

    private void clearInvisibleBlockSafe(World world, BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= world.getHeight()) {
            return;
        }
        if (world.getBlockState(pos).getBlock() == ScreenSetup.screenHitBlock) {
            world.setBlockToAir(pos);
        }
    }

    private void clearInvisibleBlocks(World world, BlockPos pos, int meta, int size) {
        if (meta == 2) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, pos.add(-i, -j, 0));
                    }
                }
            }
        }

        if (meta == 3) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, pos.add(i, -j, 0));
                    }
                }
            }
        }

        if (meta == 4) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, j));
                    }
                }
            }
        }

        if (meta == 5) {
            for (int i = 0 ; i <= size ; i++) {
                for (int j = 0 ; j <= size ; j++) {
                    if (i != 0 || j != 0) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, -j));
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
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        int meta = state.getBlock().getMetaFromState(state);
        clearInvisibleBlocks(world, pos, meta, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i+1) % transitions.length];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
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
            int color = ItemDye.dyeColors[damage];
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(new BlockPos(x, y, z));
            screenTileEntity.setColor(color);
            return true;
        } else {
            return super.openGui(world, x, y, z, player);
        }
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        int meta = state.getBlock().getMetaFromState(state);
        if (meta == EnumFacing.NORTH.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
        } else if (meta == EnumFacing.SOUTH.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
        } else if (meta == EnumFacing.WEST.ordinal()) {
            this.setBlockBounds(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        } else if (meta == EnumFacing.EAST.ordinal()) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        this.setBlockBoundsBasedOnState(world, pos);
        return super.getSelectedBoundingBox(world, pos);
    }

    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType() {
        return 2;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }


    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
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
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiScreen.class;
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
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);

//        if (entityLivingBase instanceof EntityPlayer) {
//            Achievements.trigger((EntityPlayer) entityLivingBase, Achievements.clearVision);
//        }
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) tileEntity;
            if (screenTileEntity.getSize() > ScreenTileEntity.SIZE_NORMAL) {
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
            }
        }
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
            if (screenTileEntity.getSize() > ScreenTileEntity.SIZE_NORMAL) {
                int meta = state.getBlock().getMetaFromState(state);
                clearInvisibleBlocks(world, pos, meta, screenTileEntity.getSize());
            }
        }
        super.breakBlock(world, pos, state);
    }
}
