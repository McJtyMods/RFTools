package mcjty.rftools.blocks.shield;

import com.mojang.authlib.GameProfile;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.bindings.IValue;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.shield.filters.*;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.shapes.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
@Optional.InterfaceList(@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers"))
public class ShieldTEBase extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, SmartWrenchSelector, ITickable,
        IMachineInformation, SimpleComponent { // @todo }, IPeripheral {

    public static final String CMD_APPLYCAMO = "shield.applyCamo";
    public static final Key<Integer> PARAM_PASS = new Key<>("pass", Type.INTEGER);

    public static final String CMD_ADDFILTER = "shield.addFilter";
    public static final Key<Integer> PARAM_ACTION = new Key<>("action", Type.INTEGER);
    public static final Key<String> PARAM_TYPE = new Key<>("type", Type.STRING);
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);
    public static final Key<Integer> PARAM_SELECTED = new Key<>("selected", Type.INTEGER);

    public static final String CMD_DELFILTER = "shield.delFilter";
    public static final String CMD_UPFILTER = "shield.upFilter";
    public static final String CMD_DOWNFILTER = "shield.downFilter";

    public static final String CMD_GETFILTERS = "getFilters";
    public static final String CLIENTCMD_GETFILTERS = "getFilters";

    public static final String COMPONENT_NAME = "shield_projector";

    public static final Key<Integer> VALUE_SHIELDVISMODE = new Key<>("shieldVisMode", Type.INTEGER);
    public static final Key<Integer> VALUE_DAMAGEMODE = new Key<>("damageMode", Type.INTEGER);
    public static final Key<Integer> VALUE_COLOR = new Key<>("color", Type.INTEGER);
    public static final Key<Boolean> VALUE_LIGHT = new Key<>("light", Type.BOOLEAN);

    @Override
    public IValue[] getValues() {
        return new IValue[]{
                new DefaultValue<>(VALUE_RSMODE, ShieldTEBase::getRSModeInt, ShieldTEBase::setRSModeInt),
                new DefaultValue<>(VALUE_SHIELDVISMODE, te -> ((ShieldTEBase) te).getShieldRenderingMode().ordinal(), (te, value) -> ((ShieldTEBase) te).setShieldRenderingMode(ShieldRenderingMode.values()[value])),
                new DefaultValue<>(VALUE_DAMAGEMODE, te -> ((ShieldTEBase) te).getDamageMode().ordinal(), (te, value) -> ((ShieldTEBase) te).setDamageMode(DamageTypeMode.values()[value])),
                new DefaultValue<>(VALUE_COLOR, ShieldTEBase::getShieldColor, ShieldTEBase::setShieldColor),
                new DefaultValue<>(VALUE_LIGHT, ShieldTEBase::isBlockLight, ShieldTEBase::setBlockLight),
        };
    }

    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // The state for the template blocks that were used.
    private IBlockState templateState = Blocks.AIR.getDefaultState();
    // If true the shield is currently active.
    private boolean shieldActive = false;
    // Timeout in case power is low. Here we wait a bit before trying again.
    private int powerTimeout = 0;

    private int shieldColor;

    // Render pass for the camo block.
    private int camoRenderPass = 0;

    // If true light is blocked
    private boolean blockLight = false;

    private int supportedBlocks;
    private float damageFactor = 1.0f;
    private float costFactor = 1.0f;

    // Filter list.
    private final List<ShieldFilter> filters = new ArrayList<>();

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    private List<RelCoordinateShield> shieldBlocks = new ArrayList<>();
    private List<IBlockState> blockStateTable = new ArrayList<>();

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


    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Callback(doc = "Get or set the current damage mode for the shield. 'Generic' means normal damage while 'Player' means damage like a player would do", getter = true, setter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] damageMode(Context context, Arguments args) {
        if(args.count() == 0) {
            return new Object[] { getDamageMode().getDescription() };
        } else {
            String mode = args.checkString(0);
            return setDamageMode(mode);
        }
    }

    private Object[] setDamageMode(String mode) {
        DamageTypeMode damageMode = DamageTypeMode.getMode(mode);
        if (damageMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setDamageMode(damageMode);
        return null;
    }

    @Callback(doc = "Get or set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true, setter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] redstoneMode(Context context, Arguments args) {
        if(args.count() == 0) {
            return new Object[] { getRSMode().getDescription() };
        } else {
            String mode = args.checkString(0);
            return setRedstoneMode(mode);
        }
    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRSMode(redstoneMode);
        return null;
    }

    @Callback(doc = "Get or set the current shield rendering mode. Values are 'Invisible', 'Shield', or 'Solid'", getter = true, setter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] shieldRenderingMode(Context context, Arguments args) {
        if(args.count() == 0) {
            return new Object[] { getShieldRenderingMode().getDescription() };
        } else {
            String mode = args.checkString(0);
            return setShieldRenderingMode(mode);
        }
    }

    private Object[] setShieldRenderingMode(String mode) {
        ShieldRenderingMode renderingMode = ShieldRenderingMode.getMode(mode);
        if (renderingMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setShieldRenderingMode(renderingMode);
        return null;
    }

    @Callback(doc = "Return true if the shield is active", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isShieldActive(Context context, Arguments args) {
        return new Object[] { isShieldActive() };
    }

    @Callback(doc = "Return true if the shield is composed (i.e. formed)", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isShieldComposed(Context context, Arguments args) {
        return new Object[] { isShieldComposed() };
    }

    @Callback(doc = "Form the shield (compose it)")
    @Optional.Method(modid = "opencomputers")
    public Object[] composeShield(Context context, Arguments args) {
        return composeShieldComp(false);
    }

    @Callback(doc = "Form the shield (compose it). This version works in disconnected mode (template blocks will connect on corners too)")
    @Optional.Method(modid = "opencomputers")
    public Object[] composeShieldDsc(Context context, Arguments args) {
        return composeShieldComp(true);
    }

    private Object[] composeShieldComp(boolean ctrl) {
        boolean done = false;
        if (!isShieldComposed()) {
            composeShield(ctrl);
            done = true;
        }
        return new Object[] { done };
    }

    @Callback(doc = "Break down the shield (decompose it)")
    @Optional.Method(modid = "opencomputers")
    public Object[] decomposeShield(Context context, Arguments args) {
        return decomposeShieldComp();
    }

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

    public boolean isBlockLight() {
        return blockLight;
    }

    public void setBlockLight(boolean blockLight) {
        this.blockLight = blockLight;
        updateShield();
        markDirtyClient();
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

    private Block calculateShieldBlock(int damageBits, int[] camoId, boolean blockLight) {
        if (!shieldActive || powerTimeout > 0) {
            return Blocks.AIR;
        }
        if (ShieldConfiguration.allowInvisibleShield && ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
            if (damageBits == 0) {
                return blockLight ? ShieldSetup.noTickInvisibleShieldBlockOpaque : ShieldSetup.noTickInvisibleShieldBlock;
            } else {
                return blockLight ? ShieldSetup.invisibleShieldBlockOpaque : ShieldSetup.invisibleShieldBlock;
            }
        }

        if (camoId[0] == -1) {
            if (damageBits == 0) {
                return blockLight ? ShieldSetup.noTickSolidShieldBlockOpaque : ShieldSetup.noTickSolidShieldBlock;
            } else {
                return blockLight ? ShieldSetup.solidShieldBlockOpaque : ShieldSetup.solidShieldBlock;
            }
        } else {
            if (damageBits == 0) {
                return blockLight ? ShieldSetup.noTickCamoShieldBlockOpaque : ShieldSetup.noTickCamoShieldBlock;
            } else {
                return blockLight ? ShieldSetup.camoShieldBlockOpaque : ShieldSetup.camoShieldBlock;
            }
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

    private FakePlayer killer = null;
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
                killer = FakePlayerFactory.get(DimensionManager.getWorld(0), new GameProfile(UUID.nameUUIDFromBytes("rftools_shield".getBytes()), "rftools_shield"));
            }
            killer.setWorld(world);
            killer.setPosition(pos.getX(), pos.getY(), pos.getZ());
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
        if (!shieldComposed) {
            // do nothing if the shield is not composed
            return;
        }


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
        blockStateTable.clear();
        Map<BlockPos, IBlockState> coordinates;

        if (isShapedShield()) {
            // Special shaped mode.
            templateState = Blocks.AIR.getDefaultState();

            ItemStack shapeItem = inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE);
            Shape shape = ShapeCardItem.getShape(shapeItem);
            boolean solid = ShapeCardItem.isSolid(shapeItem);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeItem, ShieldConfiguration.maxShieldDimension);
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeItem, ShieldConfiguration.maxShieldOffset);
            Map<BlockPos, IBlockState> col = new HashMap<>();
            ShapeCardItem.composeFormula(shapeItem, shape.getFormulaFactory().get(), getWorld(), getPos(), dimension, offset, col, supportedBlocks, solid, false, null);
            coordinates = col;
        } else {
            if(!findTemplateState()) return;

            Map<BlockPos, IBlockState> col = new HashMap<>();
            findTemplateBlocks(col, templateState, ctrl, getPos());
            coordinates = col;
        }

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        for (Map.Entry<BlockPos, IBlockState> entry : coordinates.entrySet()) {
            BlockPos c = entry.getKey();
            IBlockState state = entry.getValue();
            int st = -1;
            if (state != null) {
                for (int i = 0; i < blockStateTable.size(); i++) {
                    if (state.equals(blockStateTable.get(i))) {
                        st = i;
                        break;
                    }
                }
                if (st == -1) {
                    st = blockStateTable.size();
                    blockStateTable.add(state);
                }
            }
            shieldBlocks.add(new RelCoordinateShield(c.getX() - xCoord, c.getY() - yCoord, c.getZ() - zCoord, st));
            getWorld().setBlockToAir(c);
        }

        shieldComposed = true;
        updateShield();
    }

    private boolean isShapedShield() {
        return !inventoryHelper.getStackInSlot(ShieldContainer.SLOT_SHAPE).isEmpty();
    }

    private boolean findTemplateState() {
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos p = getPos().offset(dir);
            if (p.getY() >= 0 && p.getY() < getWorld().getHeight()) {
                IBlockState state = getWorld().getBlockState(p);
                if (ShieldSetup.shieldTemplateBlock.equals(state.getBlock())) {
                    templateState = state;
                    return true;
                }
            }
        }
        return false;
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
            Map<BlockPos, IBlockState> templateBlocks = new HashMap<>();
            IBlockState state = getWorld().getBlockState(pos);
            templateBlocks.put(pos, null);
            findTemplateBlocks(templateBlocks, state, false, pos);

            int[] camoId = calculateCamoId();
            int cddata = calculateShieldCollisionData();
            int damageBits = calculateDamageBits();
            Block block = calculateShieldBlock(damageBits, camoId, blockLight);

            for (Map.Entry<BlockPos, IBlockState> entry : templateBlocks.entrySet()) {
                BlockPos templateBlock = entry.getKey();
                RelCoordinateShield relc = new RelCoordinateShield(templateBlock.getX() - xCoord, templateBlock.getY() - yCoord, templateBlock.getZ() - zCoord, -1);
                shieldBlocks.add(relc);
                updateShieldBlock(camoId, cddata, damageBits, block, relc);
            }
        } else if (origBlock instanceof AbstractShieldBlock) {
            //@todo
            shieldBlocks.remove(new RelCoordinate(pos.getX() - xCoord, pos.getY() - yCoord, pos.getZ() - zCoord));
            getWorld().setBlockState(pos, templateState, 2);
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
        Block block = calculateShieldBlock(damageBits, camoId, blockLight);
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (RelCoordinateShield c : shieldBlocks) {
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

    private void updateShieldBlock(int[] camoId, int cddata, int damageBits, Block block, RelCoordinateShield c) {
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
            if (c.getState() != -1) {
                IBlockState state = blockStateTable.get(c.getState());
                // @todo VERY DIRTY! Don't use ID
                int id = Block.getIdFromBlock(state.getBlock());
                shieldBlockTileEntity.setCamoBlock(id, state.getBlock().getMetaFromState(state), 0);
            } else {
                shieldBlockTileEntity.setCamoBlock(camoId[0], camoId[1], camoId[2]);
            }
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
                getWorld().setBlockState(new BlockPos(pp), templateState, 2);
            } else if (templateState.getMaterial() != Material.AIR){
                if (!isShapedShield()) {
                    // No room, just spawn the block
                    BlockTools.spawnItemStack(getWorld(), cx, cy, cz, templateState.getBlock().getItem(getWorld(), new BlockPos(cx, cy, cz), templateState));
                }
            }
        }
        shieldComposed = false;
        shieldActive = false;
        shieldBlocks.clear();
        blockStateTable.clear();
        markDirtyClient();
    }

    /**
     * Find all template blocks recursively.
     * @param coordinateSet the set with coordinates to update during the search
     * @param templateState the state for the shield template block we support
     * @param ctrl if true also scan for blocks in corners
     */
    private void findTemplateBlocks(Map<BlockPos, IBlockState> coordinateSet, IBlockState templateState, boolean ctrl, BlockPos start) {
        Deque<BlockPos> todo = new ArrayDeque<>();

        if (ctrl) {
            addToTodoCornered(coordinateSet, todo, start, templateState);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.put(coordinate, null);
                addToTodoCornered(coordinateSet, todo, coordinate, templateState);
            }
        } else {
            addToTodoStraight(coordinateSet, todo, start, templateState);
            while (!todo.isEmpty() && coordinateSet.size() < supportedBlocks) {
                BlockPos coordinate = todo.pollFirst();
                coordinateSet.put(coordinate, null);
                addToTodoStraight(coordinateSet, todo, coordinate, templateState);
            }
        }
    }

    private void addToTodoStraight(Map<BlockPos, IBlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, IBlockState templateState) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos pp = coordinate.offset(dir);
            if (pp.getY() >= 0 && pp.getY() < getWorld().getHeight()) {
                if (!coordinateSet.containsKey(pp)) {
                    IBlockState state = getWorld().getBlockState(pp);
                    if (state == templateState) {
                        if (!todo.contains(pp)) {
                            todo.addLast(pp);
                        }
                    }
                }
            }
        }
    }

    private void addToTodoCornered(Map<BlockPos, IBlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, IBlockState templateState) {
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
                            if (!coordinateSet.containsKey(c)) {
                                IBlockState state = getWorld().getBlockState(c);
                                if (state == templateState) {
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
        if (tagCompound.hasKey("templateColor")) {
            int templateColor = tagCompound.getInteger("templateColor");
            templateState = ShieldSetup.shieldTemplateBlock.getDefaultState().withProperty(ShieldTemplateBlock.COLOR, ShieldTemplateBlock.TemplateColor.values()[templateColor]);
        } else if (tagCompound.hasKey("templateMeta")) {
            // Deprecated @todo remove with 1.13
            int meta = tagCompound.getInteger("templateMeta");
            templateState = ShieldSetup.shieldTemplateBlock.getStateFromMeta(meta);
        } else {
            templateState = Blocks.AIR.getDefaultState();
        }

        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInteger("visMode")];
        rsMode = RedstoneMode.values()[(tagCompound.getByte("rsMode"))];
        damageMode = DamageTypeMode.values()[(tagCompound.getByte("damageMode"))];
        camoRenderPass = tagCompound.getInteger("camoRenderPass");
        blockLight = tagCompound.getBoolean("blocklight");

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
        if (templateState.getMaterial() != Material.AIR) {
            tagCompound.setInteger("templateColor", templateState.getValue(ShieldTemplateBlock.COLOR).ordinal());
        }

        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
        tagCompound.setByte("rsMode", (byte) rsMode.ordinal());
        tagCompound.setByte("damageMode", (byte) damageMode.ordinal());

        tagCompound.setInteger("camoRenderPass", camoRenderPass);
        tagCompound.setBoolean("blocklight", blockLight);
        tagCompound.setInteger("shieldColor", shieldColor);

        writeFiltersToNBT(tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInteger("powerTimeout");
        if (!isShapedShield()) {
            if (tagCompound.hasKey("templateColor")) {
                int templateColor = tagCompound.getInteger("templateColor");
                templateState = ShieldSetup.shieldTemplateBlock.getDefaultState().withProperty(ShieldTemplateBlock.COLOR, ShieldTemplateBlock.TemplateColor.values()[templateColor]);
            } else if (tagCompound.hasKey("templateMeta")) {
                // Deprecated @todo remove with 1.13
                int meta = tagCompound.getInteger("templateMeta");
                templateState = ShieldSetup.shieldTemplateBlock.getStateFromMeta(meta);
            } else {
                templateState = Blocks.AIR.getDefaultState();
            }
        } else {
            templateState = Blocks.AIR.getDefaultState();
        }

        shieldBlocks.clear();
        blockStateTable.clear();
        if (tagCompound.hasKey("relcoordsNew")) {
            byte[] byteArray = tagCompound.getByteArray("relcoordsNew");
            int j = 0;
            for (int i = 0; i < byteArray.length / 8; i++) {
                short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
                short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
                short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
                short st = bytesToShort(byteArray[j + 6], byteArray[j + 7]);
                j += 8;
                shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, st));
            }

            NBTTagList list = tagCompound.getTagList("gstates", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < list.tagCount() ; i++) {
                NBTTagCompound tc = (NBTTagCompound) list.get(i);
                String b = tc.getString("b");
                int m = tc.getInteger("m");
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(b));
                if (block == null) {
                    block = Blocks.STONE;
                    m = 0;
                }
                IBlockState state = block.getStateFromMeta(m);
                blockStateTable.add(state);
            }
        } else {
            byte[] byteArray = tagCompound.getByteArray("relcoords");
            int j = 0;
            for (int i = 0; i < byteArray.length / 6; i++) {
                short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
                short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
                short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
                j += 6;
                shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, -1));
            }
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);

        shieldRenderingMode = ShieldRenderingMode.values()[tagCompound.getInteger("visMode")];
        damageMode = DamageTypeMode.values()[(tagCompound.getByte("damageMode"))];
        camoRenderPass = tagCompound.getInteger("camoRenderPass");
        blockLight = tagCompound.getBoolean("blocklight");

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
        if (templateState.getMaterial() != Material.AIR) {
            tagCompound.setInteger("templateColor", templateState.getValue(ShieldTemplateBlock.COLOR).ordinal());
        }
        byte[] blocks = new byte[shieldBlocks.size() * 8];
        int j = 0;
        for (RelCoordinateShield c : shieldBlocks) {
            blocks[j+0] = shortToByte1((short) c.getDx());
            blocks[j+1] = shortToByte2((short) c.getDx());
            blocks[j+2] = shortToByte1((short) c.getDy());
            blocks[j+3] = shortToByte2((short) c.getDy());
            blocks[j+4] = shortToByte1((short) c.getDz());
            blocks[j+5] = shortToByte2((short) c.getDz());
            blocks[j+6] = shortToByte1((short) c.getState());
            blocks[j+7] = shortToByte2((short) c.getState());
            j += 8;
        }
        tagCompound.setByteArray("relcoordsNew", blocks);

        NBTTagList list = new NBTTagList();
        for (IBlockState state : blockStateTable) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setString("b", state.getBlock().getRegistryName().toString());
            tc.setInteger("m", state.getBlock().getMetaFromState(state));
            list.appendTag(tc);
        }
        tagCompound.setTag("gstates", list);

        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
        tagCompound.setByte("damageMode", (byte) damageMode.ordinal());

        tagCompound.setInteger("camoRenderPass", camoRenderPass);
        tagCompound.setBoolean("blocklight", blockLight);
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
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_APPLYCAMO.equals(command)) {
            camoRenderPass = params.get(PARAM_PASS);
            updateShield();
            return true;
        } else if (CMD_ADDFILTER.equals(command)) {
            int action = params.get(PARAM_ACTION);
            String type = params.get(PARAM_TYPE);
            String player = params.get(PARAM_PLAYER);
            int selected = params.get(PARAM_SELECTED);
            addFilter(action, type, player, selected);
            return true;
        } else if (CMD_DELFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            delFilter(selected);
            return true;
        } else if (CMD_UPFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            upFilter(selected);
            return true;
        } else if (CMD_DOWNFILTER.equals(command)) {
            int selected = params.get(PARAM_SELECTED);
            downFilter(selected);
            return true;
        }

        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
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
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
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
    public ItemStack removeStackFromSlot(int index) {
        if (index == ShieldContainer.SLOT_SHAPE && !inventoryHelper.getStackInSlot(index).isEmpty()) {
            // Restart if we go from having a stack to not having stack or the other way around.
            decomposeShield();
        }
        return getInventoryHelper().removeStackFromSlot(index);
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
        if (index == ShieldContainer.SLOT_SHAPE /* && (stack.isEmpty() != inventoryHelper.getStackInSlot(index).isEmpty()) */ ) { // @todo McJtyLib issue #62
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
