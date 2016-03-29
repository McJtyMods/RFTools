package mcjty.rftools.blocks.shield;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.blocks.shield.filters.*;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.smartwrench.SmartWrenchSelector;
import mcjty.rftools.varia.CustomSidedInvWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class ShieldTEBase extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, SmartWrenchSelector, ITickable { // @todo }, SimpleComponent, IPeripheral {

    public static final String CMD_SHIELDVISMODE = "shieldVisMode";
    public static final String CMD_APPLYCAMO = "applyCamo";
    public static final String CMD_DAMAGEMODE = "damageMode";
    public static final String CMD_RSMODE = "rsMode";
    public static final String CMD_ADDFILTER = "addFilter";
    public static final String CMD_DELFILTER = "delFilter";
    public static final String CMD_UPFILTER = "upFilter";
    public static final String CMD_DOWNFILTER = "downFilter";
    public static final String CMD_GETFILTERS = "getFilters";
    public static final String CMD_SETCOLOR = "setColor";
    public static final String CLIENTCMD_GETFILTERS = "getFilters";

    public static final String COMPONENT_NAME = "shield_projector";

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // The meta value for the template blocks that were used.
    private int templateMeta = 0;
    // If true the shield is currently active.
    private boolean shieldActive = false;
    // Timeout in case power is low. Here we wait a bit before trying again.
    private int powerTimeout = 0;

    private int powered = 0;

    private int shieldColor;

    // Render pass for the camo block.
    private int camoRenderPass = 0;

    private int supportedBlocks;
    private float damageFactor = 1.0f;
    private float costFactor = 1.0f;

    // Filter list.
    private final List<ShieldFilter> filters = new ArrayList<ShieldFilter>();

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    private List<RelCoordinate> shieldBlocks = new ArrayList<RelCoordinate>();

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ShieldContainer.factory, ShieldContainer.BUFFER_SIZE);

    public ShieldTEBase(int maxEnergy, int maxReceive) {
        super(maxEnergy, maxReceive);
    }

    public void setSupportedBlocks(int supportedBlocks) {
        this.supportedBlocks = supportedBlocks;
    }

    public void setDamageFactor(float factor) {
        this.damageFactor = factor;
    }
    public void setCostFactor(float factor) {
        this.costFactor = factor;
    }

//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public String getType() {
//        return COMPONENT_NAME;
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public String[] getMethodNames() {
//        return new String[] { "getDamageMode", "setDamageMode", "getRedstoneMode", "setRedstoneMode", "getShieldRenderingMode", "setShieldRenderingMode", "isShieldActive", "isShieldComposed",
//            "composeShield", "composeShieldDsc", "decomposeShield" };
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
//        switch (method) {
//            case 0: return new Object[] { getDamageMode().getDescription() };
//            case 1: return setDamageMode((String) arguments[0]);
//            case 2: return new Object[] { getRedstoneMode().getDescription() };
//            case 3: return setRedstoneMode((String) arguments[0]);
//            case 4: return new Object[] { getShieldRenderingMode().getDescription() };
//            case 5: return setShieldRenderingMode((String) arguments[0]);
//            case 6: return new Object[] { isShieldActive() };
//            case 7: return new Object[] { isShieldComposed() };
//            case 8: return composeShieldComp(false);
//            case 9: return composeShieldComp(true);
//            case 10: return decomposeShieldComp();
//        }
//        return new Object[0];
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public void attach(IComputerAccess computer) {
//
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public void detach(IComputerAccess computer) {
//
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public boolean equals(IPeripheral other) {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = "OpenComputers")
//    public String getComponentName() {
//        return COMPONENT_NAME;
//    }
//
//
//    @Callback(doc = "Get the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] getDamageMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getDamageMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", setter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] setDamageMode(Context context, Arguments args) throws Exception {
//        String mode = args.checkString(0);
//        return setDamageMode(mode);
//    }

    private Object[] setDamageMode(String mode) {
        DamageTypeMode damageMode = DamageTypeMode.getMode(mode);
        if (damageMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setDamageMode(damageMode);
        return null;
    }

//    @Callback(doc = "Get the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getRedstoneMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", setter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
//        String mode = args.checkString(0);
//        return setRedstoneMode(mode);
//    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }


//    @Callback(doc = "Get the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] getShieldRenderingMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getShieldRenderingMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", setter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] setShieldRenderingMode(Context context, Arguments args) throws Exception {
//        String mode = args.checkString(0);
//        return setShieldRenderingMode(mode);
//    }

    private Object[] setShieldRenderingMode(String mode) {
        ShieldRenderingMode renderingMode = ShieldRenderingMode.getMode(mode);
        if (renderingMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setShieldRenderingMode(renderingMode);
        return null;
    }

//    @Callback(doc = "Return true if the shield is active", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] isShieldActive(Context context, Arguments args) throws Exception {
//        return new Object[] { isShieldActive() };
//    }
//
//    @Callback(doc = "Return true if the shield is composed (i.e. formed)", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] isShieldComposed(Context context, Arguments args) throws Exception {
//        return new Object[] { isShieldComposed() };
//    }
//
//    @Callback(doc = "Form the shield (compose it)")
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] composeShield(Context context, Arguments args) throws Exception {
//        return composeShieldComp(false);
//    }
//
//    @Callback(doc = "Form the shield (compose it). This version works in disconnected mode (template blocks will connect on corners too)")
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] composeShieldDsc(Context context, Arguments args) throws Exception {
//        return composeShieldComp(true);
//    }

    private Object[] composeShieldComp(boolean ctrl) {
        boolean done = false;
        if (!isShieldComposed()) {
            composeShield(ctrl);
            done = true;
        }
        return new Object[] { done };
    }

//    @Callback(doc = "Break down the shield (decompose it)")
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] decomposeShield(Context context, Arguments args) throws Exception {
//        return decomposeShieldComp();
//    }
//
    private Object[] decomposeShieldComp() {
        boolean done = false;
        if (isShieldComposed()) {
            decomposeShield();
            done = true;
        }
        return new Object[] { done };
    }

    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markDirty();
        }
    }

    public boolean isPowered() {
        return powered > 0;
    }

    public List<ShieldFilter> getFilters() {
        return filters;
    }

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        updateShield();
        markDirtyClient();
    }

    private void delFilter(int selected) {
        filters.remove(selected);
        updateShield();
        markDirtyClient();
    }

    private void upFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected-1);
        ShieldFilter filter2 = filters.get(selected);
        filters.set(selected - 1, filter2);
        filters.set(selected, filter1);
        markDirtyClient();
    }

    private void downFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected);
        ShieldFilter filter2 = filters.get(selected+1);
        filters.set(selected, filter2);
        filters.set(selected + 1, filter1);
        markDirtyClient();
    }

    private void addFilter(int action, String type, String player, int selected) {
        ShieldFilter filter = AbstractShieldFilter.createFilter(type);
        filter.setAction(action);
        if (filter instanceof PlayerFilter) {
            ((PlayerFilter)filter).setName(player);
        }
        if (selected == -1) {
            filters.add(filter);
        } else {
            filters.add(selected, filter);
        }
        updateShield();
        markDirtyClient();
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        markDirtyClient();
    }

    public DamageTypeMode getDamageMode() {
        return damageMode;
    }

    public void setDamageMode(DamageTypeMode damageMode) {
        this.damageMode = damageMode;
        markDirtyClient();
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;

        if (shieldComposed) {
            updateShield();
        }

        markDirtyClient();
    }

    private int[] calculateCamoId() {
        ItemStack stack = getStackInSlot(ShieldContainer.SLOT_BUFFER);
        int camoId = -1;
        int meta = 0;
        int te = 0;

        if (ShieldRenderingMode.MODE_SOLID.equals(shieldRenderingMode) && stack != null && stack.getItem() != null) {
            if (!(stack.getItem() instanceof ItemBlock)) {
                return new int[] { camoId, meta, te };
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            camoId = Block.getIdFromBlock(block);
            meta = stack.getItemDamage();
            if (block.hasTileEntity(block.getStateFromMeta(meta))) {
                te = 1;
            }
        }
        return new int[] { camoId, meta, te };
    }

    private Block calculateShieldBlock(int damageBits) {
        if (!shieldActive || powerTimeout > 0) {
            return Blocks.air;
        }
        if (ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
            if (damageBits == 0) {
                return ShieldSetup.noTickInvisibleShieldBlock;
            } else {
                return ShieldSetup.invisibleShieldBlock;
            }
        }

        if (damageBits == 0) {
            return ShieldSetup.noTickSolidShieldBlock;
        } else {
            return ShieldSetup.solidShieldBlock;
        }
    }

    private int calculateDamageBits() {
        int bits = 0;
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    bits |= AbstractShieldBlock.META_ITEMS;
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    bits |= AbstractShieldBlock.META_PASSIVE;
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    bits |= AbstractShieldBlock.META_HOSTILE;
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    bits |= AbstractShieldBlock.META_PLAYERS;
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    bits |= AbstractShieldBlock.META_ITEMS | AbstractShieldBlock.META_PASSIVE | AbstractShieldBlock.META_HOSTILE | AbstractShieldBlock.META_PLAYERS;
                }
            }
        }
        return bits;
    }

    private int calculateShieldCollisionData() {
        int cd = 0;
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_SOLID) != 0) {
                if (ItemFilter.ITEM.equals(filter.getFilterName())) {
                    cd |= AbstractShieldBlock.META_ITEMS;
                } else if (AnimalFilter.ANIMAL.equals(filter.getFilterName())) {
                    cd |= AbstractShieldBlock.META_PASSIVE;
                } else if (HostileFilter.HOSTILE.equals(filter.getFilterName())) {
                    cd |= AbstractShieldBlock.META_HOSTILE;
                } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    cd |= AbstractShieldBlock.META_PLAYERS;
                } else if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                    cd |= AbstractShieldBlock.META_ITEMS | AbstractShieldBlock.META_PASSIVE | AbstractShieldBlock.META_HOSTILE | AbstractShieldBlock.META_PLAYERS;
                }
            }
        }
        return cd;
    }

    private int calculateRfPerTick() {
        if (!shieldActive) {
            return 0;
        }
        int s = shieldBlocks.size() - 50;
        if (s < 10) {
            s = 10;
        }
        int rf = ShieldConfiguration.rfBase * s / 10;
        if (ShieldRenderingMode.MODE_SHIELD.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfShield * s / 10;
        } else if (ShieldRenderingMode.MODE_SOLID.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfCamo * s / 10;
        }
        return rf;
    }

    public boolean isShieldComposed() {
        return shieldComposed;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public void applyDamageToEntity(Entity entity) {
        DamageSource source;
        int rf;
        if (DamageTypeMode.DAMAGETYPE_GENERIC.equals(damageMode)) {
            rf = ShieldConfiguration.rfDamage;
            source = DamageSource.generic;
        } else {
            rf = ShieldConfiguration.rfDamagePlayer;
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
            source = DamageSource.causePlayerDamage(fakePlayer);
        }

        rf = (int) (rf * costFactor * (4.0f - getInfusedFactor()) / 4.0f);

        if (getEnergyStored(EnumFacing.DOWN) < rf) {
            // Not enough RF to do damage.
            return;
        }
        consumeEnergy(rf);

        float damage = ShieldConfiguration.damage;
        damage *= damageFactor;
        damage = damage * (1.0f + getInfusedFactor() / 2.0f);

        entity.attackEntityFrom(source, damage);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        boolean checkPower = false;
        if (powerTimeout > 0) {
            powerTimeout--;
            markDirty();
            if (powerTimeout > 0) {
                return;
            } else {
                checkPower = true;
            }
        }

        boolean needsUpdate = false;

        int rf = calculateRfPerTick();
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

        if (rf > 0) {
            if (getEnergyStored(EnumFacing.DOWN) < rf) {
                powerTimeout = 100;     // Wait 5 seconds before trying again.
                needsUpdate = true;
            } else {
                if (checkPower) {
                    needsUpdate = true;
                }
                consumeEnergy(rf);
            }
        }

        boolean newShieldActive = shieldActive;

        if (redstoneMode == RedstoneMode.REDSTONE_IGNORED) {
            newShieldActive = true;         // Always active in this mode.
        } else {
            boolean rs = powered > 0;
            if (redstoneMode == RedstoneMode.REDSTONE_OFFREQUIRED) {
                newShieldActive = !rs;
            } else if (redstoneMode == RedstoneMode.REDSTONE_ONREQUIRED) {
                newShieldActive = rs;
            }
        }
        if (newShieldActive != shieldActive) {
            needsUpdate = true;
            shieldActive = newShieldActive;
        }

        if (needsUpdate) {
            updateShield();
            markDirty();
        }
    }

    public void composeDecomposeShield(boolean ctrl) {
        if (shieldComposed) {
            // Shield is already composed. Break it into template blocks again.
            decomposeShield();
        } else {
            // Shield is not composed. Find all nearby template blocks and form a shield.
            composeShield(ctrl);
        }
    }

    public void composeShield(boolean ctrl) {
        shieldBlocks.clear();
        Collection<BlockPos> coordinates;

        if (isShapedShield()) {
            // Special shaped mode.
            ShapeCardItem.Shape shape = ShapeCardItem.getShape(inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE));
            BlockPos dimension = ShapeCardItem.getClampedDimension(inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE), ShieldConfiguration.maxShieldDimension);
            BlockPos offset = ShapeCardItem.getClampedOffset(inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE), ShieldConfiguration.maxShieldOffset);
            coordinates = new ArrayList<>();
            ShapeCardItem.composeShape(shape, worldObj, getPos(), dimension, offset, coordinates, supportedBlocks, false, null);
        } else {
            templateMeta = findTemplateMeta();

            coordinates = new HashSet<>();
            findTemplateBlocks((Set<BlockPos>)coordinates, templateMeta, ctrl, getPos());
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        for (BlockPos c : coordinates) {
            shieldBlocks.add(new RelCoordinate(c.getX() - xCoord, c.getY() - yCoord, c.getZ() - zCoord));
        }

        shieldComposed = true;
        updateShield();
    }

    private boolean isShapedShield() {
        return inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE) != null;
    }

    private int findTemplateMeta() {
        int meta = -1;
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos p = getPos().offset(dir);
            if (p.getY() >= 0 && p.getY() < worldObj.getHeight()) {
                IBlockState state = worldObj.getBlockState(p);
                if (ShieldSetup.shieldTemplateBlock.equals(state.getBlock())) {
                    meta = state.getBlock().getMetaFromState(state);
                    break;
                }
            }
        }
        return meta;
    }

    @Override
    public void selectBlock(EntityPlayer player, BlockPos pos) {
        if (!shieldComposed) {
            Logging.message(player, EnumChatFormatting.YELLOW + "Shield is not composed. Nothing happens!");
            return;
        }

        float squaredDistance = (float) getPos().distanceSq(pos.getX(), pos.getY(), pos.getZ());
        if (squaredDistance > ShieldConfiguration.maxDisjointShieldDistance * ShieldConfiguration.maxDisjointShieldDistance) {
            Logging.message(player, EnumChatFormatting.YELLOW + "This template is too far to connect to the shield!");
            return;
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();

        Block origBlock = worldObj.getBlockState(pos).getBlock();
        BlockPos c = pos;
        if (origBlock == ShieldSetup.shieldTemplateBlock) {
            if (isShapedShield()) {
                Logging.message(player, EnumChatFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
                return;
            }
            Set<BlockPos> templateBlocks = new HashSet<>();
            IBlockState state = worldObj.getBlockState(c);
            findTemplateBlocks(templateBlocks, state.getBlock().getMetaFromState(state), false, c);

            int[] camoId = calculateCamoId();
            int cddata = calculateShieldCollisionData();
            int damageBits = calculateDamageBits();
            Block block = calculateShieldBlock(damageBits);
            for (BlockPos templateBlock : templateBlocks) {
                RelCoordinate relc = new RelCoordinate(templateBlock.getX() - xCoord, templateBlock.getY() - yCoord, templateBlock.getZ() - zCoord);
                shieldBlocks.add(relc);
                updateShieldBlock(camoId, cddata, damageBits, block, relc);
            }
        } else if (origBlock instanceof AbstractShieldBlock) {
            shieldBlocks.remove(new RelCoordinate(c.getX() - xCoord, c.getY() - yCoord, c.getZ() - zCoord));
            if (isShapedShield()) {
                worldObj.setBlockToAir(c);
            } else {
                worldObj.setBlockState(c, ShieldSetup.shieldTemplateBlock.getStateFromMeta(templateMeta), 2);
            }
        } else {
            Logging.message(player, EnumChatFormatting.YELLOW + "The selected shield can't do anything with this block!");
            return;
        }
        markDirtyClient();
    }

    /**
     * Update all shield blocks. Possibly creating the shield.
     */
    private void updateShield() {
        int[] camoId = calculateCamoId();
        int cddata = calculateShieldCollisionData();
        int damageBits = calculateDamageBits();
        Block block = calculateShieldBlock(damageBits);
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        for (RelCoordinate c : shieldBlocks) {
            if (Blocks.air.equals(block)) {
                worldObj.setBlockToAir(new BlockPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz()));
            } else {
                updateShieldBlock(camoId, cddata, damageBits, block, c);
            }
        }
        markDirtyClient();
    }

    private void updateShieldBlock(int[] camoId, int cddata, int damageBits, Block block, RelCoordinate c) {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos pp = new BlockPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
        worldObj.setBlockState(pp, block.getStateFromMeta(camoId[1]), 2);
        TileEntity te = worldObj.getTileEntity(pp);
        if (te instanceof NoTickShieldBlockTileEntity) {
            NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) te;
            shieldBlockTileEntity.setCamoBlock(camoId[0], camoId[2]);
            shieldBlockTileEntity.setShieldBlock(getPos());
            shieldBlockTileEntity.setDamageBits(damageBits);
            shieldBlockTileEntity.setCollisionData(cddata);
            shieldBlockTileEntity.setShieldColor(shieldColor);
        }
    }

    public void decomposeShield() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        for (RelCoordinate c : shieldBlocks) {
            int cx = xCoord + c.getDx();
            int cy = yCoord + c.getDy();
            int cz = zCoord + c.getDz();
            BlockPos pp = new BlockPos(cx, cy, cz);
            Block block = worldObj.getBlockState(pp).getBlock();
            if (worldObj.isAirBlock(pp) || block instanceof AbstractShieldBlock) {
                if (isShapedShield()) {
                    worldObj.setBlockToAir(pp);
                } else {
                    worldObj.setBlockState(pp, ShieldSetup.shieldTemplateBlock.getStateFromMeta(templateMeta), 2);
                }
            } else {
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    BlockTools.spawnItemStack(worldObj, cx, cy, cz, new ItemStack(ShieldSetup.shieldTemplateBlock, 1, templateMeta));
                }
            }
        }
        shieldComposed = false;
        shieldActive = false;
        shieldBlocks.clear();
        markDirtyClient();
    }

    /**
     * Find all template blocks recursively.
     * @param coordinateSet the set with coordinates to update during the search
     * @param meta the metavalue for the shield template block we support
     * @param ctrl if true also scan for blocks in corners
     */
    private void findTemplateBlocks(Set<BlockPos> coordinateSet, int meta, boolean ctrl, BlockPos start) {
        Deque<BlockPos> todo = new ArrayDeque<>();

        if (ctrl) {
            addToTodoCornered(coordinateSet, todo, start, meta);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.add(coordinate);
                addToTodoCornered(coordinateSet, todo, coordinate, meta);
            }
        } else {
            addToTodoStraight(coordinateSet, todo, start, meta);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.add(coordinate);
                addToTodoStraight(coordinateSet, todo, coordinate, meta);
            }
        }
    }

    private void addToTodoStraight(Set<BlockPos> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, int meta) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos pp = coordinate.offset(dir);
            if (pp.getY() >= 0 && pp.getY() < worldObj.getHeight()) {
                if (!coordinateSet.contains(pp)) {
                    IBlockState state = worldObj.getBlockState(pp);
                    if (ShieldSetup.shieldTemplateBlock.equals(state.getBlock())) {
                        int m = state.getBlock().getMetaFromState(state);
                        if (m == meta) {
                            if (!todo.contains(pp)) {
                                todo.addLast(pp);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addToTodoCornered(Set<BlockPos> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, int meta) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        int z = coordinate.getZ();
        for (int xx = x-1 ; xx <= x+1 ; xx++) {
            for (int yy = y-1 ; yy <= y+1 ; yy++) {
                for (int zz = z-1 ; zz <= z+1 ; zz++) {
                    if (xx != x || yy != y || zz != z) {
                        if (yy >= 0 && yy < worldObj.getHeight()) {
                            BlockPos c = new BlockPos(xx, yy, zz);
                            if (!coordinateSet.contains(c)) {
                                IBlockState state = worldObj.getBlockState(c);
                                if (ShieldSetup.shieldTemplateBlock.equals(state.getBlock())) {
                                    int m = state.getBlock().getMetaFromState(state);
                                    if (m == meta) {
                                        if (!todo.contains(c)) {
                                            todo.addLast(c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static short bytesToShort(byte b1, byte b2) {
        short s1 = (short) (b1 & 0xff);
        short s2 = (short) (b2 & 0xff);
        return (short) (s1 * 256 + s2);
    }

    private static byte shortToByte1(short s) {
        return (byte) ((s & 0xff00) >> 8);
    }

    private static byte shortToByte2(short s) {
        return (byte) (s & 0xff);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getByte("powered");
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInteger("powerTimeout");
        templateMeta = tagCompound.getInteger("templateMeta");

        shieldBlocks.clear();;
        if (tagCompound.hasKey("coordinates")) {
            // Support for legacy coordinates field
            NBTTagCompound compound = tagCompound.getCompoundTag("coordinates");
            NBTTagList list = compound.getTagList("list", 10);
            for(int i = 0; i < list.tagCount(); ++i) {
                BlockPos c = BlockPosTools.readFromNBT(list.getCompoundTagAt(i), "c");
                shieldBlocks.add(new RelCoordinate(c.getX() - getPos().getX(), c.getY() - getPos().getY(), c.getZ() - getPos().getZ()));
            }
        } else {
            byte[] byteArray = tagCompound.getByteArray("relcoords");
            int j = 0;
            for (int i = 0 ; i < byteArray.length / 6 ; i++) {
                short dx = bytesToShort(byteArray[j+0], byteArray[j+1]);
                short dy = bytesToShort(byteArray[j+2], byteArray[j+3]);
                short dz = bytesToShort(byteArray[j+4], byteArray[j+5]);
                j += 6;
                shieldBlocks.add(new RelCoordinate(dx, dy, dz));
            }
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        int m = tagCompound.getInteger("visMode");
        shieldRenderingMode = ShieldRenderingMode.values()[m];

        m = tagCompound.getByte("rsMode");
        redstoneMode = RedstoneMode.values()[m];

        m = tagCompound.getByte("damageMode");
        damageMode = DamageTypeMode.values()[m];

        camoRenderPass = tagCompound.getInteger("camoRenderPass");

        shieldColor = tagCompound.getInteger("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }

        readFiltersFromNBT(tagCompound);
    }

    private void readFiltersFromNBT(NBTTagCompound tagCompound) {
        filters.clear();
        NBTTagList filterList = tagCompound.getTagList("filters", Constants.NBT.TAG_COMPOUND);
        if (filterList != null) {
            for (int i = 0 ; i < filterList.tagCount() ; i++) {
                NBTTagCompound compound = filterList.getCompoundTagAt(i);
                filters.add(AbstractShieldFilter.createFilter(compound));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setByte("powered", (byte) powered);
        tagCompound.setBoolean("composed", shieldComposed);
        tagCompound.setBoolean("active", shieldActive);
        tagCompound.setInteger("powerTimeout", powerTimeout);
        tagCompound.setInteger("templateMeta", templateMeta);
        byte[] blocks = new byte[shieldBlocks.size() * 6];
        int j = 0;
        for (RelCoordinate c : shieldBlocks) {
            blocks[j+0] = shortToByte1((short) c.getDx());
            blocks[j+1] = shortToByte2((short) c.getDx());
            blocks[j+2] = shortToByte1((short) c.getDy());
            blocks[j+3] = shortToByte2((short) c.getDy());
            blocks[j+4] = shortToByte1((short) c.getDz());
            blocks[j+5] = shortToByte2((short) c.getDz());
            j += 6;
        }
        tagCompound.setByteArray("relcoords", blocks);

//        shieldBlocksOld.writeToNBT(tagCompound, "coordinates");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
        tagCompound.setByte("rsMode", (byte) redstoneMode.ordinal());
        tagCompound.setByte("damageMode", (byte) damageMode.ordinal());

        tagCompound.setInteger("camoRenderPass", camoRenderPass);
        tagCompound.setInteger("shieldColor", shieldColor);

        writeFiltersToNBT(tagCompound);
    }

    private void writeFiltersToNBT(NBTTagCompound tagCompound) {
        NBTTagList filterList = new NBTTagList();
        for (ShieldFilter filter : filters) {
            NBTTagCompound compound = new NBTTagCompound();
            filter.writeToNBT(compound);
            filterList.appendTag(compound);
        }
        tagCompound.setTag("filters", filterList);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SHIELDVISMODE.equals(command)) {
            String m = args.get("mode").getString();
            setShieldRenderingMode(ShieldRenderingMode.getMode(m));
            return true;
        } else if (CMD_APPLYCAMO.equals(command)) {
            camoRenderPass = args.get("pass").getInteger();
            updateShield();
            return true;
        } else if (CMD_ADDFILTER.equals(command)) {
            int action = args.get("action").getInteger();
            String type = args.get("type").getString();
            String player = args.get("player").getString();
            int selected = args.get("selected").getInteger();
            addFilter(action, type, player, selected);
            return true;
        } else if (CMD_DELFILTER.equals(command)) {
            int selected = args.get("selected").getInteger();
            delFilter(selected);
            return true;
        } else if (CMD_UPFILTER.equals(command)) {
            int selected = args.get("selected").getInteger();
            upFilter(selected);
            return true;
        } else if (CMD_DOWNFILTER.equals(command)) {
            int selected = args.get("selected").getInteger();
            downFilter(selected);
            return true;
        } else if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRedstoneMode(RedstoneMode.getMode(m));
            return true;
        } else if (CMD_DAMAGEMODE.equals(command)) {
            String m = args.get("mode").getString();
            setDamageMode(DamageTypeMode.getMode(m));
            return true;
        } else if (CMD_SETCOLOR.equals(command)) {
            int color = args.get("color").getInteger();
            setShieldColor(color);
            return true;
        }

        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETFILTERS.equals(command)) {
            return getFilters();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETFILTERS.equals(command)) {
            GuiShield.storeFiltersForClient(list);
            return true;
        }
        return false;
    }


    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index == ShieldContainer.SLOT_SHAPE && inventoryHelper.getStackInSlot(index) != null && amount > 0) {
            // Restart if we go from having a stack to not having stack or the other way around.
            decomposeShield();
        }

        ItemStack stackInSlot = inventoryHelper.getStackInSlot(index);
        if (stackInSlot != null) {
            if (stackInSlot.stackSize <= amount) {
                ItemStack old = stackInSlot;
                inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, null);
                markDirty();
                return old;
            }
            ItemStack its = stackInSlot.splitStack(amount);
            if (stackInSlot.stackSize == 0) {
                inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, null);
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ShieldContainer.SLOT_SHAPE && ((stack == null && inventoryHelper.getStackInSlot(index) != null) || (stack != null && inventoryHelper.getStackInSlot(index) == null))) {
            // Restart if we go from having a stack to not having stack or the other way around.
            decomposeShield();
        }

        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    IItemHandler invHandler = new CustomSidedInvWrapper(this);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) invHandler;
        }
        return super.getCapability(capability, facing);
    }
}
