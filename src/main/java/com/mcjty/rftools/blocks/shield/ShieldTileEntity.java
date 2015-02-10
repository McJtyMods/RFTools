package com.mcjty.rftools.blocks.shield;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.entity.SyncedValueList;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.RedstoneMode;
import com.mcjty.rftools.blocks.shield.filters.*;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

@Optional.InterfaceList(@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"))
public class ShieldTileEntity extends GenericEnergyHandlerTileEntity implements IInventory, SimpleComponent {

    public static final String CMD_SHIELDVISMODE = "shieldVisMode";
    public static final String CMD_APPLYCAMO = "applyCamo";
    public static final String CMD_DAMAGEMODE = "damageMode";
    public static final String CMD_RSMODE = "rsMode";
    public static final String CMD_ADDFILTER = "addFilter";
    public static final String CMD_DELFILTER = "delFilter";
    public static final String CMD_UPFILTER = "upFilter";
    public static final String CMD_DOWNFILTER = "downFilter";
    public static final String CMD_GETFILTERS = "getFilters";
    public static final String CLIENTCMD_GETFILTERS = "getFilters";

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private DamageTypeMode damageMode = DamageTypeMode.DAMAGETYPE_GENERIC;

    // If true the shield is currently made.
    private boolean shieldComposed = false;
    // If true the shield is currently active.
    private boolean shieldActive = false;
    // Timeout in case power is low. Here we wait a bit before trying again.
    private int powerTimeout = 0;

    // Render pass for the camo block.
    private int camoRenderPass = 0;

    // Filter list.
    private final List<ShieldFilter> filters = new ArrayList<ShieldFilter>();

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    private SyncedValueList<Coordinate> shieldBlocks = new SyncedValueList<Coordinate>() {
        @Override
        public Coordinate readElementFromNBT(NBTTagCompound tagCompound) {
            return Coordinate.readFromNBT(tagCompound, "c");
        }

        @Override
        public NBTTagCompound writeElementToNBT(Coordinate element) {
            return Coordinate.writeToNBT(element);
        }
    };

    private ItemStack stacks[] = new ItemStack[ShieldContainer.BUFFER_SIZE];

    public ShieldTileEntity() {
        super(ShieldConfiguration.MAXENERGY, ShieldConfiguration.RECEIVEPERTICK);
        registerSyncedObject(shieldBlocks);
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return "shield_projector";
    }


    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getDamageMode(Context context, Arguments args) throws Exception {
        return new Object[] { getDamageMode().getDescription() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setDamageMode(Context context, Arguments args) throws Exception {
        String mode = args.checkString(0);
        DamageTypeMode damageMode = DamageTypeMode.getMode(mode);
        if (damageMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setDamageMode(damageMode);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
        return new Object[] { getRedstoneMode().getDescription() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
        String mode = args.checkString(0);
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }


    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getShieldRenderingMode(Context context, Arguments args) throws Exception {
        return new Object[] { getShieldRenderingMode().getDescription() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setShieldRenderingMode(Context context, Arguments args) throws Exception {
        String mode = args.checkString(0);
        ShieldRenderingMode renderingMode = ShieldRenderingMode.getMode(mode);
        if (renderingMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setShieldRenderingMode(renderingMode);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] isShieldActive(Context context, Arguments args) throws Exception {
        return new Object[] { isShieldActive() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] isShieldComposed(Context context, Arguments args) throws Exception {
        return new Object[] { isShieldComposed() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] composeShield(Context context, Arguments args) throws Exception {
        boolean done = false;
        if (!isShieldComposed()) {
            composeShield();
            done = true;
        }
        return new Object[] { done };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] decomposeShield(Context context, Arguments args) throws Exception {
        boolean done = false;
        if (isShieldComposed()) {
            decomposeShield();
            done = true;
        }
        return new Object[] { done };
    }

    public List<ShieldFilter> getFilters() {
        return filters;
    }

    private void delFilter(int selected) {
        filters.remove(selected);
        updateShield();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private void upFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected-1);
        ShieldFilter filter2 = filters.get(selected);
        filters.set(selected-1, filter2);
        filters.set(selected, filter1);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private void downFilter(int selected) {
        ShieldFilter filter1 = filters.get(selected);
        ShieldFilter filter2 = filters.get(selected+1);
        filters.set(selected, filter2);
        filters.set(selected + 1, filter1);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public DamageTypeMode getDamageMode() {
        return damageMode;
    }

    public void setDamageMode(DamageTypeMode damageMode) {
        this.damageMode = damageMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;

        if (shieldComposed) {
            updateShield();
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private Block calculateCamoBlock() {
        ItemStack stack = stacks[0];
        if (stack != null && stack.getItem() != null) {
            return Block.getBlockFromItem(stack.getItem());
        }
        return null;
    }

    private int[] calculateCamoId() {
        ItemStack stack = stacks[0];
        int camoId = -1;
        int meta = 0;
        int te = 0;

        if (ShieldRenderingMode.MODE_SOLID.equals(shieldRenderingMode) && stack != null && stack.getItem() != null) {
            Block block = Block.getBlockFromItem(stack.getItem());
            camoId = Block.getIdFromBlock(block);
            meta = stack.getItemDamage();
            if (block.hasTileEntity(meta)) {
                te = 1;
            }
        }
        return new int[] { camoId, meta, te };
    }

    private Block calculateShieldBlock() {
        if (!shieldActive || powerTimeout > 0) {
            return Blocks.air;
        }
        if (ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
            return ModBlocks.invisibleShieldBlock;
        }
        if (ShieldRenderingMode.MODE_SHIELD.equals(shieldRenderingMode)) {
            return ModBlocks.solidShieldBlock;
        }

        Block camoBlock = calculateCamoBlock();
        if (camoBlock != null) {
            if (camoBlock.isOpaqueCube()) {
                if (camoRenderPass == 1) {
                    if (camoBlock.renderAsNormalBlock()) {
                        return ModBlocks.shieldBlockOpaquePass1;
                    } else {
                        return ModBlocks.shieldBlockOpaquePass1NN;
                    }
                } else {
                    if (camoBlock.renderAsNormalBlock()) {
                        return ModBlocks.shieldBlockOpaquePass0;
                    } else {
                        return ModBlocks.shieldBlockOpaquePass0NN;
                    }
                }
            } else {
                if (camoRenderPass == 1) {
                    if (camoBlock.renderAsNormalBlock()) {
                        return ModBlocks.shieldBlockNOpaquePass1;
                    } else {
                        return ModBlocks.shieldBlockNOpaquePass1NN;
                    }
                } else {
                    if (camoBlock.renderAsNormalBlock()) {
                        return ModBlocks.shieldBlockNOpaquePass0;
                    } else {
                        return ModBlocks.shieldBlockNOpaquePass0NN;
                    }
                }
            }
        }

        return ModBlocks.solidShieldBlock;
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
        int rf = ShieldConfiguration.rfBase;
        if (ShieldRenderingMode.MODE_SHIELD.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfShield;
        } else if (ShieldRenderingMode.MODE_SOLID.equals(shieldRenderingMode)) {
            rf += ShieldConfiguration.rfCamo;
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

        rf = (int) (rf * (4.0f - getInfusedFactor()) / 4.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            // Not enough RF to do damage.
            return;
        }
        extractEnergy(ForgeDirection.DOWN, rf, false);

        float damage = ShieldConfiguration.damage;
        damage = damage * (1.0f + getInfusedFactor() / 2.0f);

        entity.attackEntityFrom(source, damage);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

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
            if (getEnergyStored(ForgeDirection.DOWN) < rf) {
                powerTimeout = 100;     // Wait 5 seconds before trying again.
                needsUpdate = true;
            } else {
                if (checkPower) {
                    needsUpdate = true;
                }
                extractEnergy(ForgeDirection.DOWN, rf, false);
            }
        }

        boolean newShieldActive = shieldActive;

        if (redstoneMode == RedstoneMode.REDSTONE_IGNORED) {
            newShieldActive = true;         // Always active in this mode.
        } else {
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            boolean rs = BlockTools.getRedstoneSignal(meta);
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



    public void composeDecomposeShield() {
        if (shieldComposed) {
            // Shield is already composed. Break it into template blocks again.
            decomposeShield();
        } else {
            // Shield is not composed. Find all nearby template blocks and form a shield.
            composeShield();
        }
    }

    public void composeShield() {
        Set<Coordinate> coordinateSet = new HashSet<Coordinate>();
        findTemplateBlocks(coordinateSet, xCoord, yCoord, zCoord);
        shieldBlocks.clear();
        for (Coordinate c : coordinateSet) {
            shieldBlocks.add(c);
        }
        shieldComposed = true;
        updateShield();
    }

    /**
     * Update all shield blocks. Possibly creating the shield.
     */
    private void updateShield() {
        Coordinate thisCoordinate = new Coordinate(xCoord, yCoord, zCoord);
        int[] camoId = calculateCamoId();
        int cddata = calculateShieldCollisionData();
        Block block = calculateShieldBlock();
        int damageBits = calculateDamageBits();
        for (Coordinate c : shieldBlocks) {
            if (Blocks.air.equals(block)) {
                worldObj.setBlockToAir(c.getX(), c.getY(), c.getZ());
            } else {
                worldObj.setBlock(c.getX(), c.getY(), c.getZ(), block, camoId[1], 2);
                TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof ShieldBlockTileEntity) {
                    ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) te;
                    shieldBlockTileEntity.setCamoBlock(camoId[0], camoId[2]);
                    shieldBlockTileEntity.setShieldBlock(thisCoordinate);
                    shieldBlockTileEntity.setDamageBits(damageBits);
                    shieldBlockTileEntity.setCollisionData(cddata);
                }
            }
        }
        markDirty();
        notifyBlockUpdate();
    }

    public void decomposeShield() {
        for (Coordinate c : shieldBlocks) {
            Block block = worldObj.getBlock(c.getX(), c.getY(), c.getZ());
            if (worldObj.isAirBlock(c.getX(), c.getY(), c.getZ()) || block instanceof AbstractShieldBlock) {
                worldObj.setBlock(c.getX(), c.getY(), c.getZ(), ModBlocks.shieldTemplateBlock, 0, 2);
            } else {
                // No room, just spawn the block
                BlockTools.spawnItemStack(worldObj, c.getX(), c.getY(), c.getZ(), new ItemStack(ModBlocks.shieldTemplateBlock));
            }
        }
        shieldComposed = false;
        shieldActive = false;
        shieldBlocks.clear();
        markDirty();
        notifyBlockUpdate();
    }

    /**
     * Find all template blocks recursively.
     * @param coordinateSet the set with coordinates to update during the search
     * @param x current block
     * @param y current block
     * @param z current block
     */
    private void findTemplateBlocks(Set<Coordinate> coordinateSet, int x, int y, int z) {
        if (coordinateSet.size() >= ShieldConfiguration.maxShieldSize) {
            return;
        }
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int xx = x + dir.offsetX;
            int yy = y + dir.offsetY;
            int zz = z + dir.offsetZ;
            if (yy >= 0 && yy < worldObj.getHeight()) {
                Coordinate c = new Coordinate(xx, yy, zz);
                if (!coordinateSet.contains(c)) {
                    if (ModBlocks.shieldTemplateBlock.equals(worldObj.getBlock(xx, yy, zz))) {
                        coordinateSet.add(c);
                        findTemplateBlocks(coordinateSet, xx, yy, zz);
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldActive = tagCompound.getBoolean("active");
        powerTimeout = tagCompound.getInteger("powerTimeout");
        shieldBlocks.readFromNBT(tagCompound, "coordinates");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        int m = tagCompound.getInteger("visMode");
        shieldRenderingMode = ShieldRenderingMode.values()[m];

        m = tagCompound.getByte("rsMode");
        redstoneMode = RedstoneMode.values()[m];

        m = tagCompound.getByte("damageMode");
        damageMode = DamageTypeMode.values()[m];

        camoRenderPass = tagCompound.getInteger("camoRenderPass");

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

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i+ShieldContainer.SLOT_BUFFER] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("composed", shieldComposed);
        tagCompound.setBoolean("active", shieldActive);
        tagCompound.setInteger("powerTimeout", powerTimeout);
        shieldBlocks.writeToNBT(tagCompound, "coordinates");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
        tagCompound.setByte("rsMode", (byte) redstoneMode.ordinal());
        tagCompound.setByte("damageMode", (byte) damageMode.ordinal());

        tagCompound.setInteger("camoRenderPass", camoRenderPass);

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

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : stacks) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
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
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks[index] = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "Shield Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }
}
