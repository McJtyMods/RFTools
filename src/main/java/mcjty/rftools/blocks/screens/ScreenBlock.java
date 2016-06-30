package mcjty.rftools.blocks.screens;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.ITooltipInfo;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
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
        super(Material.IRON, ScreenTileEntity.class, ScreenContainer.class, ScreenItemBlock.class, "screen", true);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
            boolean connected = screenTileEntity.isConnected();
            if (!connected) {
                probeInfo.text(TextFormatting.YELLOW + "[NOT CONNECTED]");
            }
            boolean power = screenTileEntity.isPowerOn();
            if (!power) {
                probeInfo.text(TextFormatting.YELLOW + "[NO POWER]");
            }
            int rfPerTick = screenTileEntity.getTotalRfPerTick();
            probeInfo.text(TextFormatting.GREEN + (power ? "Consuming " : "Needs ") + rfPerTick + " RF/tick");
            IScreenModule module = screenTileEntity.getHoveringModule();
            if (module instanceof ITooltipInfo) {
                String[] info = ((ITooltipInfo) module).getInfo(world, screenTileEntity.getHoveringX(), screenTileEntity.getHoveringY(), player);
                for (String s : info) {
                    probeInfo.text(s);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            boolean connected = tagCompound.getBoolean("connected");
            if (!connected) {
                currenttip.add(TextFormatting.YELLOW + "[NOT CONNECTED]");
            }
            boolean power = tagCompound.getBoolean("powerOn");
            if (!power) {
                currenttip.add(TextFormatting.YELLOW + "[NO POWER]");
            }
            int rfPerTick = ((ScreenTileEntity) accessor.getTileEntity()).getTotalRfPerTick();
            currenttip.add(TextFormatting.GREEN + (power ? "Consuming " : "Needs ") + rfPerTick + " RF/tick");
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
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(ScreenContainer.SLOT_MODULES, ScreenContainer.SLOT_MODULES + ScreenContainer.SCREEN_MODULES - 1) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return itemStack.getItem() instanceof IModuleProvider;
            }
        };
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world.isRemote) {
            RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
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
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (itemStack != null && itemStack.getItem() == Items.DYE) {
            int damage = itemStack.getItemDamage();
            if (damage < 0) {
                damage = 0;
            } else if (damage > 15) {
                damage = 15;
            }
            int color = ItemDye.DYE_COLORS[damage];
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(new BlockPos(x, y, z));
            screenTileEntity.setColor(color);
            return true;
        } else {
            return super.openGui(world, x, y, z, player);
        }
    }

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.5F - 0.5F, 0.0F, 0.5F - 0.5F, 0.5F + 0.5F, 1.0F, 0.5F + 0.5F);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int meta = state.getBlock().getMetaFromState(state);
        if (meta == EnumFacing.NORTH.ordinal()) {
            return NORTH_AABB;
        } else if (meta == EnumFacing.SOUTH.ordinal()) {
            return SOUTH_AABB;
        } else if (meta == EnumFacing.WEST.ordinal()) {
            return WEST_AABB;
        } else if (meta == EnumFacing.EAST.ordinal()) {
            return EAST_AABB;
        } else {
            return BLOCK_AABB;
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }


    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
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
                list.add(TextFormatting.BLUE + "Huge screen.");
            } else if (size == ScreenTileEntity.SIZE_LARGE) {
                list.add(TextFormatting.BLUE + "Large screen.");
            }
            if (transparent) {
                list.add(TextFormatting.BLUE + "Transparent screen.");
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
            list.add(TextFormatting.BLUE + String.valueOf(rc) + " modules");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This is a modular screen. As such it doesn't show anything.");
            list.add(TextFormatting.WHITE + "You must insert modules to control what you can see.");
            list.add(TextFormatting.WHITE + "This screen cannot be directly powered. It has to be remotely");
            list.add(TextFormatting.WHITE + "powered by a nearby Screen Controller.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);

        if (entityLivingBase instanceof EntityPlayer) {
            Achievements.trigger((EntityPlayer) entityLivingBase, Achievements.clearVision);
        }
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
