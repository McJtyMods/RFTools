package mcjty.rftools.blocks.shield;

import com.mojang.authlib.GameProfile;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.shield.filters.*;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.typed.Type;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers"),
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class ShieldTEBase extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, SmartWrenchSelector, ITickable,
        IMachineInformation { // @todo }, SimpleComponent, IPeripheral {

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

    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // The meta value for the template blocks that were used.
    private int templateMeta = 0;
    // If true the shield is currently active.
    private boolean shieldActive = false;
    // Timeout in case power is low. Here we wait a bit before trying again.
    private int powerTimeout = 0;

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

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
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
//    @Optional.Method(modid = "opencomputers")
//    public String getComponentName() {
//        return COMPONENT_NAME;
//    }
//
//
//    @Callback(doc = "Get the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", getter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] getDamageMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getDamageMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", setter = true)
//    @Optional.Method(modid = "opencomputers")
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
//    @Optional.Method(modid = "opencomputers")
//    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getRedstoneMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", setter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
//        String mode = args.checkString(0);
//        return setRedstoneMode(mode);
//    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRSMode(redstoneMode);
        return null;
    }


//    @Callback(doc = "Get the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", getter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] getShieldRenderingMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getShieldRenderingMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", setter = true)
//    @Optional.Method(modid = "opencomputers")
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
//    @Optional.Method(modid = "opencomputers")
//    public Object[] isShieldActive(Context context, Arguments args) throws Exception {
//        return new Object[] { isShieldActive() };
//    }
//
//    @Callback(doc = "Return true if the shield is composed (i.e. formed)", getter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] isShieldComposed(Context context, Arguments args) throws Exception {
//        return new Object[] { isShieldComposed() };
//    }
//
//    @Callback(doc = "Form the shield (compose it)")
//    @Optional.Method(modid = "opencomputers")
//    public Object[] composeShield(Context context, Arguments args) throws Exception {
//        return composeShieldComp(false);
//    }
//
//    @Callback(doc = "Form the shield (compose it). This version works in disconnected mode (template blocks will connect on corners too)")
//    @Optional.Method(modid = "opencomputers")
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
//    @Optional.Method(modid = "opencomputers")
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

    public boolean isPowered() {
        return powerLevel > 0;
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

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { ShieldContainer.SLOT_SHARD };
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == ShieldContainer.SLOT_SHAPE && stack.getItem() != BuilderSetup.shapeCardItem) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return index == ShieldContainer.SLOT_SHARD && itemStackIn.getItem() == ModItems.dimensionalShardItem;
    }

    private int[] calculateCamoId() {
        ItemStack stack = getStackInSlot(ShieldContainer.SLOT_BUFFER);
        int camoId = -1;
        int meta = 0;
        int te = 0;

        if (ShieldRenderingMode.MODE_MIMIC.equals(shieldRenderingMode) && !stack.isEmpty() && stack.getItem() != null) {
            if (!(stack.getItem() instanceof ItemBlock)) {
                return new int[] { camoId, meta, te };
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            camoId = Block.getIdFromBlock(block);
            meta = stack.getMetadata();
            if (block.hasTileEntity(block.getStateFromMeta(meta))) {
                te = 1;
            }
        }
        return new int[] { camoId, meta, te };
    }

    private Block calculateShieldBlock(int damageBits) {
        if (!shieldActive || powerTimeout > 0) {
            return Blocks.AIR;
        }
        if (ShieldConfiguration.allowInvisibleShield && ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
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
        } else if (ShieldRenderingMode.MODE_MIMIC.equals(shieldRenderingMode)) {
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

    private static FakePlayer killer = null;
    private ItemStack lootingSword = ItemStack.EMPTY;

    public void applyDamageToEntity(Entity entity) {
        DamageSource source;
        int rf;
        if (DamageTypeMode.DAMAGETYPE_GENERIC.equals(damageMode)) {
            rf = ShieldConfiguration.rfDamage;
            source = DamageSource.GENERIC;
        } else {
            rf = ShieldConfiguration.rfDamagePlayer;
            if (killer == null) {
                killer = FakePlayerFactory.get(DimensionManager.getWorld(0), new GameProfile(new UUID(111, 222), "rftools_shield"));
            }
            FakePlayer fakePlayer = killer;
            ItemStack shards = getStackInSlot(ShieldContainer.SLOT_SHARD);
            if (!shards.isEmpty() && shards.getCount() >= ShieldConfiguration.shardsPerLootingKill) {
                decrStackSize(ShieldContainer.SLOT_SHARD, ShieldConfiguration.shardsPerLootingKill);
                if (lootingSword.isEmpty()) {
                    lootingSword = EnvironmentalSetup.createEnchantedItem(Items.DIAMOND_SWORD, Enchantments.LOOTING, ShieldConfiguration.lootingKillBonus);
                }
                lootingSword.setItemDamage(0);
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, lootingSword);
            } else {
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            }
            source = DamageSource.causePlayerDamage(fakePlayer);
        }

        rf = (int) (rf * costFactor * (4.0f - getInfusedFactor()) / 4.0f);

        if (getEnergyStored() < rf) {
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
    public int getEnergyDiffPerTick() {
        return shieldActive ? getRfPerTick() : 0;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return shieldActive;
    }

    @Override
    public boolean isMachineRunning() {
        return shieldActive;
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return shieldActive ? "active" : "idle";
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
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

        int rf = getRfPerTick();

        if (rf > 0) {
            if (getEnergyStored() < rf) {
                powerTimeout = 100;     // Wait 5 seconds before trying again.
                needsUpdate = true;
            } else {
                if (checkPower) {
                    needsUpdate = true;
                }
                consumeEnergy(rf);
            }
        }

        boolean newShieldActive = isMachineEnabled();
        if (newShieldActive != shieldActive) {
            needsUpdate = true;
            shieldActive = newShieldActive;
        }

        if (needsUpdate) {
            updateShield();
            markDirty();
        }
    }

    private int getRfPerTick() {
        int rf = calculateRfPerTick();
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);
        return rf;
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
            ShapeCardItem.composeShape(shape, getWorld(), getPos(), dimension, offset, coordinates, supportedBlocks, false, null);
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
            getWorld().setBlockToAir(c);
        }

        shieldComposed = true;
        updateShield();
    }

    private boolean isShapedShield() {
        return !inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE).isEmpty();
    }

    private int findTemplateMeta() {
        int meta = -1;
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos p = getPos().offset(dir);
            if (p.getY() >= 0 && p.getY() < getWorld().getHeight()) {
                IBlockState state = getWorld().getBlockState(p);
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
            Logging.message(player, TextFormatting.YELLOW + "Shield is not composed. Nothing happens!");
            return;
        }

        float squaredDistance = (float) getPos().distanceSq(pos.getX(), pos.getY(), pos.getZ());
        if (squaredDistance > ShieldConfiguration.maxDisjointShieldDistance * ShieldConfiguration.maxDisjointShieldDistance) {
            Logging.message(player, TextFormatting.YELLOW + "This template is too far to connect to the shield!");
            return;
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();

        Block origBlock = getWorld().getBlockState(pos).getBlock();
        if (origBlock == ShieldSetup.shieldTemplateBlock) {
            if (isShapedShield()) {
                Logging.message(player, TextFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
                return;
            }
            Set<BlockPos> templateBlocks = new HashSet<>();
            IBlockState state = getWorld().getBlockState(pos);
            templateBlocks.add(pos);
            findTemplateBlocks(templateBlocks, state.getBlock().getMetaFromState(state), false, pos);

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
            shieldBlocks.remove(new RelCoordinate(pos.getX() - xCoord, pos.getY() - yCoord, pos.getZ() - zCoord));
            if (isShapedShield()) {
                getWorld().setBlockToAir(pos);
            } else {
                getWorld().setBlockState(pos, ShieldSetup.shieldTemplateBlock.getStateFromMeta(templateMeta), 2);
            }
        } else {
            Logging.message(player, TextFormatting.YELLOW + "The selected shield can't do anything with this block!");
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
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (RelCoordinate c : shieldBlocks) {
            if (Blocks.AIR.equals(block)) {
                pos.setPos(xCoord + c.getDx(), yCoord + c.getDy(), zCoord + c.getDz());
                IBlockState oldState = getWorld().getBlockState(pos);
                if (oldState.getBlock() instanceof AbstractShieldBlock) {
                    getWorld().setBlockToAir(pos);
                }
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
        IBlockState oldState = getWorld().getBlockState(pp);
        if ((!oldState.getBlock().isReplaceable(getWorld(), pp)) && oldState.getBlock() != ShieldSetup.shieldTemplateBlock) {
            return;
        }
        getWorld().setBlockState(pp, block.getStateFromMeta(camoId[1]), 2);
        TileEntity te = getWorld().getTileEntity(pp);
        if (te instanceof NoTickShieldBlockTileEntity) {
            NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) te;
            shieldBlockTileEntity.setCamoBlock(camoId[0], camoId[2]);
            shieldBlockTileEntity.setShieldBlock(getPos());
            shieldBlockTileEntity.setDamageBits(damageBits);
            shieldBlockTileEntity.setCollisionData(cddata);
            shieldBlockTileEntity.setShieldColor(shieldColor);
            shieldBlockTileEntity.setShieldRenderingMode(shieldRenderingMode);
        }
    }

    public void decomposeShield() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos.MutableBlockPos pp = new BlockPos.MutableBlockPos();
        for (RelCoordinate c : shieldBlocks) {
            int cx = xCoord + c.getDx();
            int cy = yCoord + c.getDy();
            int cz = zCoord + c.getDz();
            pp.setPos(cx, cy, cz);
            Block block = getWorld().getBlockState(pp).getBlock();
            if (getWorld().isAirBlock(pp) || block instanceof AbstractShieldBlock) {
                if (isShapedShield()) {
                    getWorld().setBlockToAir(pp);
                } else {
                    getWorld().setBlockState(pp, ShieldSetup.shieldTemplateBlock.getStateFromMeta(templateMeta), 2);
                }
            } else {
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    BlockTools.spawnItemStack(getWorld(), cx, cy, cz, new ItemStack(ShieldSetup.shieldTemplateBlock, 1, templateMeta));
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
            if (pp.getY() >= 0 && pp.getY() < getWorld().getHeight()) {
                if (!coordinateSet.contains(pp)) {
                    IBlockState state = getWorld().getBlockState(pp);
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
        BlockPos.MutableBlockPos c = new BlockPos.MutableBlockPos();
        for (int xx = x-1 ; xx <= x+1 ; xx++) {
            for (int yy = y-1 ; yy <= y+1 ; yy++) {
                for (int zz = z-1 ; zz <= z+1 ; zz++) {
                    if (xx != x || yy != y || zz != z) {
                        if (yy >= 0 && yy < getWorld().getHeight()) {
                            c.setPos(xx, yy, zz);
                            if (!coordinateSet.contains(c)) {
                                IBlockState state = getWorld().getBlockState(c);
                                if (ShieldSetup.shieldTemplateBlock.equals(state.getBlock())) {
                                    int m = state.getBlock().getMetaFromState(state);
                                    if (m == meta) {
                                        if (!todo.contains(c)) {
                                            todo.addLast(c.toImmutable());
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

//    @Override
//    public Object[] getDataForGUI() {
//        return new Object[] {
//                shieldColor, redstoneMode.ordinal(), shieldRenderingMode.ordinal(), damageMode.ordinal()
//        };
//    }
//
//    @Override
//    public void syncDataForGUI(Object[] data) {
//        shieldColor = (Integer) data[0];
//        redstoneMode = RedstoneMode.values()[(Integer) data[1]];
//        shieldRenderingMode = ShieldRenderingMode.values()[(Integer) data[2]];
//        damageMode = DamageTypeMode.values()[(Integer) data[3]];
//    }
//

    @Override
    public void readClientDataFromNBT(NBTTagCompound tagCompound) {
        powerLevel = tagCompound.getByte("powered");
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInteger("powerTimeout");
        templateMeta = tagCompound.getInteger("templateMeta");

        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInteger("visMode")];
        rsMode = RedstoneMode.values()[(tagCompound.getByte("rsMode"))];
        damageMode = DamageTypeMode.values()[(tagCompound.getByte("damageMode"))];
        camoRenderPass = tagCompound.getInteger("camoRenderPass");

        shieldColor = tagCompound.getInteger("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }

        readFiltersFromNBT(tagCompound);
    }

    @Override
    public void writeClientDataToNBT(NBTTagCompound tagCompound) {
        tagCompound.setByte("powered", (byte) powerLevel);
        tagCompound.setBoolean("composed", shieldComposed);
        tagCompound.setBoolean("active", shieldActive);
        tagCompound.setInteger("powerTimeout", powerTimeout);
        tagCompound.setInteger("templateMeta", templateMeta);

        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
        tagCompound.setByte("rsMode", (byte) rsMode.ordinal());
        tagCompound.setByte("damageMode", (byte) damageMode.ordinal());

        tagCompound.setInteger("camoRenderPass", camoRenderPass);
        tagCompound.setInteger("shieldColor", shieldColor);

        writeFiltersToNBT(tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInteger("powerTimeout");
        templateMeta = tagCompound.getInteger("templateMeta");

        shieldBlocks.clear();
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

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);

        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInteger("visMode")];
        damageMode = DamageTypeMode.values()[(tagCompound.getByte("damageMode"))];
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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
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

        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
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
            setRSMode(RedstoneMode.getMode(m));
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

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, Map<String, Argument> args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETFILTERS.equals(command)) {
            return type.convert(getFilters());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean execute(String command, List<T> list, Type<T> type) {
        boolean rc = super.execute(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETFILTERS.equals(command)) {
            GuiShield.storeFiltersForClient(Type.create(ShieldFilter.class).convert(list));
            return true;
        }
        return false;
    }


    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index == ShieldContainer.SLOT_SHAPE && !inventoryHelper.getStackInSlot(index).isEmpty() && amount > 0) {
            // Restart if we go from having a stack to not having stack or the other way around.
            decomposeShield();
        }

        ItemStack stackInSlot = inventoryHelper.getStackInSlot(index);
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getCount() <= amount) {
                ItemStack old = stackInSlot.copy();
                inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, ItemStack.EMPTY);
                markDirty();
                return old;
            }
            ItemStack its = stackInSlot.splitStack(amount);
            if (stackInSlot.isEmpty()) {
                inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, ItemStack.EMPTY);
            }
            markDirty();
            return its;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ShieldContainer.SLOT_SHAPE && ((stack.isEmpty() && !inventoryHelper.getStackInSlot(index).isEmpty()) || (!stack.isEmpty() && inventoryHelper.getStackInSlot(index).isEmpty()))) {
            // Restart if we go from having a stack to not having stack or the other way around.
            decomposeShield();
        }

        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            int amount = getInventoryStackLimit();
            if (amount <= 0) {
                stack.setCount(0);
            } else {
                stack.setCount(amount);
            }
        }
        markDirty();
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }
}
