package mcjty.rftools.blocks.screens;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericItemBlock;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ModuleSupport;
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
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;

public class ScreenBlock extends GenericRFToolsBlock<ScreenTileEntity, ScreenContainer> {
    public static final PropertyDirection HORIZONTAL_FACING = PropertyDirection.create("horizontal_facing", EnumFacing.Plane.HORIZONTAL);

    public ScreenBlock(String name, Class<? extends ScreenTileEntity> clazz) {
        super(Material.IRON, clazz, ScreenContainer::new, GenericItemBlock.class, name, true);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HORIZONTAL_FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if(meta > 5) {
            meta -= 4;
        } else if(meta > 1) {
            EnumFacing facing = EnumFacing.VALUES[meta];
            return getDefaultState().withProperty(FACING, facing).withProperty(HORIZONTAL_FACING, facing);
        }
        EnumFacing horizontalFacing = EnumFacing.VALUES[(meta >> 1) + 2];
        EnumFacing facing = (meta & 1) == 0 ? EnumFacing.DOWN : EnumFacing.UP;
        return getDefaultState().withProperty(HORIZONTAL_FACING, horizontalFacing).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);
        int meta = 0;
        switch(facing) {
        case UP:
            meta = 1;
            //$FALL-THROUGH$
        case DOWN:
            meta += (horizontalFacing.getIndex() << 1);
            if(meta < 6) meta -= 4;
            return meta;
        default:
            return facing.getIndex();
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HORIZONTAL_FACING);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        BlockPos pos = data.getPos();
        addProbeInfoScreen(mode, probeInfo, player, world, pos);
    }

    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfoScreen(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ScreenTileEntity) {
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
            if (!screenTileEntity.isConnected() && screenTileEntity.isControllerNeeded()) {
                probeInfo.text(TextFormatting.YELLOW + "[NOT CONNECTED]");
            }
            if (!isCreative()) {
                boolean power = screenTileEntity.isPowerOn();
                if (!power) {
                    probeInfo.text(TextFormatting.YELLOW + "[NO POWER]");
                }
                if (mode == ProbeMode.EXTENDED) {
                    int rfPerTick = screenTileEntity.getTotalRfPerTick();
                    probeInfo.text(TextFormatting.GREEN + (power ? "Consuming " : "Needs ") + rfPerTick + " RF/tick");
                }
            }
            IScreenModule module = screenTileEntity.getHoveringModule();
            if (module instanceof ITooltipInfo) {
                List<String> info = ((ITooltipInfo) module).getInfo(world, screenTileEntity.getHoveringX(), screenTileEntity.getHoveringY());
                for (String s : info) {
                    probeInfo.text(s);
                }
            }
        }
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof ScreenTileEntity) {
            return getWailaBodyScreen(currenttip, accessor.getPlayer(), (ScreenTileEntity) te);
        } else {
            return Collections.emptyList();
        }
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "waila")
    public List<String> getWailaBodyScreen(List<String> currenttip, EntityPlayer player, ScreenTileEntity te) {
        if (!te.isConnected() && te.isControllerNeeded()) {
            currenttip.add(TextFormatting.YELLOW + "[NOT CONNECTED]");
        }
        if (!isCreative()) {
            boolean power = te.isPowerOn();
            if (!power) {
                currenttip.add(TextFormatting.YELLOW + "[NO POWER]");
            }
            if (player.isSneaking()) {
                int rfPerTick = te.getTotalRfPerTick();
                currenttip.add(TextFormatting.GREEN + (power ? "Consuming " : "Needs ") + rfPerTick + " RF/tick");
            }
        }
        if (System.currentTimeMillis() - lastTime > 500) {
            lastTime = System.currentTimeMillis();
            te.requestDataFromServer(RFTools.MODID, ScreenTileEntity.CMD_SCREEN_INFO, TypedMap.EMPTY);
        }
        currenttip.addAll(ScreenTileEntity.infoReceived);
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTileEntity.class, new ScreenRenderer());
        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(this), 0, ScreenTileEntity.class);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public static boolean hasModuleProvider(ItemStack stack) {
        return stack.getItem() instanceof IModuleProvider || stack.hasCapability(IModuleProvider.CAPABILITY, null);
    }

    public static IModuleProvider getModuleProvider(ItemStack stack) {
        Item item = stack.getItem();
        if(item instanceof IModuleProvider) {
            return (IModuleProvider) item;
        } else {
            return stack.getCapability(IModuleProvider.CAPABILITY, null);
        }
    }

    @Override
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(ScreenContainer.SLOT_MODULES, ScreenContainer.SLOT_MODULES + ScreenContainer.SCREEN_MODULES - 1) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return hasModuleProvider(itemStack);
            }
        };
    }

    public boolean activate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        // Doesn't make sense to rotate a potentially 3x3 screen,
        // and is incompatible with our special wrench actions.
        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world.isRemote) {
            RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
            screenTileEntity.hitScreenClient(mouseOver.hitVec.x - pos.getX(), mouseOver.hitVec.y - pos.getY(), mouseOver.hitVec.z - pos.getZ(), mouseOver.sideHit, world.getBlockState(pos).getValue(ScreenBlock.HORIZONTAL_FACING));
        }
    }

    private void setInvisibleBlockSafe(World world, BlockPos pos, int dx, int dy, int dz, EnumFacing facing) {
        int yy = pos.getY() + dy;
        if (yy < 0 || yy >= world.getHeight()) {
            return;
        }
        int xx = pos.getX() + dx;
        int zz = pos.getZ() + dz;
        BlockPos posO = new BlockPos(xx, yy, zz);
        if (world.isAirBlock(posO)) {
            world.setBlockState(posO, ScreenSetup.screenHitBlock.getDefaultState().withProperty(BaseBlock.FACING, facing), 3);
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(posO);
            screenHitTileEntity.setRelativeLocation(-dx, -dy, -dz);
        }
    }

    private void setInvisibleBlocks(World world, BlockPos pos, int size) {
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);

        for (int i = 0 ; i <= size ; i++) {
            for (int j = 0 ; j <= size ; j++) {
                if (i != 0 || j != 0) {
                    if (facing == EnumFacing.NORTH) {
                        setInvisibleBlockSafe(world, pos, -i, -j, 0, facing);
                    } else if (facing == EnumFacing.SOUTH) {
                        setInvisibleBlockSafe(world, pos, i, -j, 0, facing);
                    } else if (facing == EnumFacing.WEST) {
                        setInvisibleBlockSafe(world, pos, 0, -i, j, facing);
                    } else if (facing == EnumFacing.EAST) {
                        setInvisibleBlockSafe(world, pos, 0, -i, -j, facing);
                    } else if (facing == EnumFacing.UP) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                        }
                    } else if (facing == EnumFacing.DOWN) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                        }
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

    private void clearInvisibleBlocks(World world, BlockPos pos, IBlockState state, int size) {
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);
        for (int i = 0 ; i <= size ; i++) {
            for (int j = 0 ; j <= size ; j++) {
                if (i != 0 || j != 0) {
                    if (facing == EnumFacing.NORTH) {
                        clearInvisibleBlockSafe(world, pos.add(-i, -j, 0));
                    } else if (facing == EnumFacing.SOUTH) {
                        clearInvisibleBlockSafe(world, pos.add(i, -j, 0));
                    } else if (facing == EnumFacing.WEST) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, j));
                    } else if (facing == EnumFacing.EAST) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, -j));
                    } else if (facing == EnumFacing.UP) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, -j));
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, j));
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, j));
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, -j));
                        }
                    } else if (facing == EnumFacing.DOWN) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, j));
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, -j));
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, j));
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, -j));
                        }
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
        cycleSizeTranspMode(world, pos);
        return true;
    }

    public void cycleSizeTranspMode(World world, BlockPos pos) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
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
    }

    public void cycleSizeMode(World world, BlockPos pos) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i+2) % transitions.length];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
                break;
            }
        }
    }

    public void cycleTranspMode(World world, BlockPos pos) {
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i % 2) == 0 ? (i+1) : (i-1)];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
                break;
            }
        }
    }

    @Override
    protected boolean openGui(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!itemStack.isEmpty() && itemStack.getItem() == Items.DYE) {
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
        }
        if (player.isSneaking()) {
            return super.openGui(world, x, y, z, player);
        } else {
            if (world.isRemote) {
                activateOnClient(world, new BlockPos(x, y, z));
            }
            return true;
        }
    }

    private void activateOnClient(World world, BlockPos pos) {
        RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
        ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos);
        screenTileEntity.hitScreenClient(mouseOver.hitVec.x - pos.getX(), mouseOver.hitVec.y - pos.getY(), mouseOver.hitVec.z - pos.getZ(), mouseOver.sideHit, world.getBlockState(pos).getValue(ScreenBlock.HORIZONTAL_FACING));
    }

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.5F - 0.5F, 0.0F, 0.5F - 0.5F, 0.5F + 0.5F, 1.0F, 0.5F + 0.5F);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
    public static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    public static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0F, 1.0F - 0.125F, 0.0F, 1.0F, 1.0F, 1.0F);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        if (facing == EnumFacing.NORTH) {
            return NORTH_AABB;
        } else if (facing == EnumFacing.SOUTH) {
            return SOUTH_AABB;
        } else if (facing == EnumFacing.WEST) {
            return WEST_AABB;
        } else if (facing == EnumFacing.EAST) {
            return EAST_AABB;
        } else if (facing == EnumFacing.UP) {
            return UP_AABB;
        } else if (facing == EnumFacing.DOWN) {
            return DOWN_AABB;
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
    public Class<GuiScreen> getGuiClass() {
        return GuiScreen.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
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
                    ItemStack stack = new ItemStack(tag);
                    if (!stack.isEmpty()) {
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
            // @todo achievements
//            Achievements.trigger((EntityPlayer) entityLivingBase, Achievements.clearVision);
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
                clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
            }
        }
        super.breakBlock(world, pos, state);
    }
}
