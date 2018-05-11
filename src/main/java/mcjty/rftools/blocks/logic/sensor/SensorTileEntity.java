package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftools.RFTools;
import mcjty.rftools.varia.NamedEnum;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Function;

public class SensorTileEntity extends LogicTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SETNUMBER = "sensor.setNumber";
    public static final String CMD_SETTYPE = "sensor.setType";
    public static final String CMD_SETAREA = "sensor.setArea";
    public static final String CMD_SETGROUP = "sensor.setGroup";

    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_ITEMMATCH = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/sensor.gui"));

    private int number = 0;
    private SensorType sensorType = SensorType.SENSOR_BLOCK;
    private AreaType areaType = AreaType.AREA_1;
    private GroupType groupType = GroupType.GROUP_ONE;

    private int checkCounter = 0;
    private AxisAlignedBB cachedBox = null;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }


    public SensorTileEntity() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        cachedBox = null;
        markDirtyClient();
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
        cachedBox = null;
        markDirtyClient();
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
        cachedBox = null;
        markDirtyClient();
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
        cachedBox = null;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        checkCounter--;
        if (checkCounter > 0) {
            return;
        }
        checkCounter = 10;

        setRedstoneState(checkSensor() ? 15 : 0);
    }

    public boolean checkSensor() {
        boolean newout;
        LogicFacing facing = getFacing(getWorld().getBlockState(getPos()));
        EnumFacing inputSide = facing.getInputSide();
        BlockPos newpos = getPos().offset(inputSide);

        switch (sensorType) {
            case SENSOR_BLOCK:
                newout = checkBlockOrFluid(newpos, facing, inputSide, this::checkBlock);
                break;
            case SENSOR_FLUID:
                newout = checkBlockOrFluid(newpos, facing, inputSide, this::checkFluid);
                break;
            case SENSOR_GROWTHLEVEL:
                newout = checkGrowthLevel(newpos, facing, inputSide);
                break;
            case SENSOR_ENTITIES:
                newout = checkEntities(newpos, facing, inputSide, Entity.class);
                break;
            case SENSOR_PLAYERS:
                newout = checkEntities(newpos, facing, inputSide, EntityPlayer.class);
                break;
            case SENSOR_HOSTILE:
                newout = checkEntitiesHostile(newpos, facing, inputSide);
                break;
            case SENSOR_PASSIVE:
                newout = checkEntitiesPassive(newpos, facing, inputSide);
                break;
            default:
                newout = false;
        }
        return newout;
    }

    private boolean checkBlockOrFluid(BlockPos newpos, LogicFacing facing, EnumFacing dir, Function<BlockPos, Boolean> blockChecker) {
        int blockCount = areaType.getBlockCount();
        if (blockCount > 0) {
            Boolean x = checkBlockOrFluidRow(newpos, dir, blockChecker, blockCount);
            if (x != null) return x;
        } else if (blockCount < 0) {
            // Area
            EnumFacing downSide = facing.getSide();
            EnumFacing inputSide = facing.getInputSide();
            EnumFacing rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            EnumFacing leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);

            blockCount = -blockCount;
            Boolean x = checkBlockOrFluidRow(newpos, dir, blockChecker, blockCount);
            if (x != null) return x;

            for (int i = 1 ; i <= (blockCount-1)/2 ; i++) {
                BlockPos p = newpos.offset(leftSide, i);
                x = checkBlockOrFluidRow(p, dir, blockChecker, blockCount);
                if (x != null) return x;
                p = newpos.offset(rightSide, i);
                x = checkBlockOrFluidRow(p, dir, blockChecker, blockCount);
                if (x != null) return x;
            }
        }

        return groupType == GroupType.GROUP_ALL;
    }

    private Boolean checkBlockOrFluidRow(BlockPos newpos, EnumFacing dir, Function<BlockPos, Boolean> blockChecker, int count) {
        for (int i = 0; i < count; i++) {
            boolean result = blockChecker.apply(newpos);
            if (result && groupType == GroupType.GROUP_ONE) {
                return true;
            }
            if ((!result) && groupType == GroupType.GROUP_ALL) {
                return false;
            }
            newpos = newpos.offset(dir);
        }
        return null;
    }

    private boolean checkBlock(BlockPos newpos) {
        IBlockState state = getWorld().getBlockState(newpos);
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        if (matcher.isEmpty()) {
            return state.getBlock().isFullBlock(state);
        }
        ItemStack stack = state.getBlock().getItem(getWorld(), newpos, state);
        if (!stack.isEmpty()) {
            return matcher.getItem() == stack.getItem();
        } else {
            return matcher.getItem() == Item.getItemFromBlock(state.getBlock());
        }
    }

    private boolean checkFluid(BlockPos newpos) {
        IBlockState state = getWorld().getBlockState(newpos);
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        Block block = state.getBlock();
        if (matcher.isEmpty()) {
            if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
                return !block.isAir(state, getWorld(), newpos);
            }

            return false;
        }
        ItemStack stack = block.getItem(getWorld(), newpos, state);
        Item matcherItem = matcher.getItem();

        FluidStack matcherFluidStack = null;
//        if (matcherItem instanceof IFluidContainerItem) {
//            matcherFluidStack = ((IFluidContainerItem)matcherItem).getFluid(matcher);
//            return checkFluid(block, matcherFluidStack, state, newpos);
//        }
        if (matcherItem instanceof ItemBucket || matcherItem instanceof UniversalBucket) {
            matcherFluidStack = new FluidBucketWrapper(matcher).getFluid();
            return checkFluid(block, matcherFluidStack, state, newpos);
        }

        return false;
    }

    private boolean checkFluid(Block block, FluidStack matcherFluidStack, IBlockState state, BlockPos newpos) {
        if (matcherFluidStack == null) {
            return block.isAir(state,  getWorld(), newpos);
        }

        Fluid matcherFluid = matcherFluidStack.getFluid();
        if (matcherFluid == null) {
            return false;
        }

        Block matcherFluidBlock = matcherFluid.getBlock();
        if (matcherFluidBlock == null) {
            return false;
        }

        String matcherBlockName = matcherFluidBlock.getUnlocalizedName();
        String blockName = block.getUnlocalizedName();
        return blockName.equals(matcherBlockName);
    }

    private boolean checkGrowthLevel(BlockPos newpos, LogicFacing facing, EnumFacing dir) {
        int blockCount = areaType.getBlockCount();
        if (blockCount > 0) {
            Boolean x = checkGrowthLevelRow(newpos, dir, blockCount);
            if (x != null) return x;
        } else if (blockCount < 0) {
            // Area
            EnumFacing downSide = facing.getSide();
            EnumFacing inputSide = facing.getInputSide();
            EnumFacing rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            EnumFacing leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);

            blockCount = -blockCount;
            Boolean x = checkGrowthLevelRow(newpos, dir, blockCount);
            if (x != null) return x;

            for (int i = 1 ; i <= (blockCount-1)/2 ; i++) {
                BlockPos p = newpos.offset(leftSide, i);
                x = checkGrowthLevelRow(p, dir, blockCount);
                if (x != null) return x;
                p = newpos.offset(rightSide, i);
                x = checkGrowthLevelRow(p, dir, blockCount);
                if (x != null) return x;
            }
        }
        return groupType == GroupType.GROUP_ALL;
    }

    private Boolean checkGrowthLevelRow(BlockPos newpos, EnumFacing dir, int blockCount) {
        for (int i = 0; i < blockCount; i++) {
            boolean result = checkGrowthLevel(newpos);
            if (result && groupType == GroupType.GROUP_ONE) {
                return true;
            }
            if ((!result) && groupType == GroupType.GROUP_ALL) {
                return false;
            }
            newpos = newpos.offset(dir);
        }
        return null;
    }

    private boolean checkGrowthLevel(BlockPos newpos) {
        IBlockState state = getWorld().getBlockState(newpos);
        Block block = state.getBlock();
        int pct;
        if (block instanceof BlockCrops) {
            BlockCrops crops = (BlockCrops) block;
            int age = crops.getAge(state);
            int maxAge = crops.getMaxAge();
            pct = (age * 100) / maxAge;
        } else if (block instanceof BlockNetherWart) {
            BlockNetherWart wart = (BlockNetherWart) block;
            int age = state.getValue(BlockNetherWart.AGE);
            int maxAge = 3;
            pct = (age * 100) / maxAge;
        } else {
            pct = 0;
        }
        return pct >= number;
    }

    public void invalidateCache() {
        cachedBox = null;
    }

    private AxisAlignedBB getCachedBox(BlockPos pos1, LogicFacing facing, EnumFacing dir) {
        if (cachedBox == null) {
            int n = areaType.getBlockCount();

            if (n > 0) {
                cachedBox = new AxisAlignedBB(pos1);
                if (n > 1) {
                    BlockPos pos2 = pos1.offset(dir, n - 1);
                    cachedBox = cachedBox.union(new AxisAlignedBB(pos2));
                }
                cachedBox = cachedBox.expand(.1, .1, .1);
            } else {
                n = -n;
                cachedBox = new AxisAlignedBB(pos1);

                // Area
                EnumFacing downSide = facing.getSide();
                EnumFacing inputSide = facing.getInputSide();
                EnumFacing rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
                EnumFacing leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);
                if (n > 1) {
                    BlockPos pos2 = pos1.offset(dir, n - 1);
                    cachedBox = cachedBox.union(new AxisAlignedBB(pos2));
                }
                BlockPos pos2 = pos1.offset(leftSide, (n-1)/2);
                cachedBox = cachedBox.union(new AxisAlignedBB(pos2));
                pos2 = pos1.offset(rightSide, (n-1)/2);
                cachedBox = cachedBox.union(new AxisAlignedBB(pos2));
            }
        }
        return cachedBox;
    }

    private boolean checkEntities(BlockPos pos1, LogicFacing facing, EnumFacing dir, Class<? extends Entity> clazz) {
        List<Entity> entities = getWorld().getEntitiesWithinAABB(clazz, getCachedBox(pos1, facing, dir));
        return entities.size() >= number;
    }

    private boolean checkEntitiesHostile(BlockPos pos1, LogicFacing facing, EnumFacing dir) {
        List<Entity> entities = getWorld().getEntitiesWithinAABB(EntityCreature.class, getCachedBox(pos1, facing, dir));
        int cnt = 0;
        for (Entity entity : entities) {
            if (entity instanceof IMob) {
                cnt++;
                if (cnt >= number) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkEntitiesPassive(BlockPos pos1, LogicFacing facing, EnumFacing dir) {
        List<Entity> entities = getWorld().getEntitiesWithinAABB(EntityCreature.class, getCachedBox(pos1, facing, dir));
        int cnt = 0;
        for (Entity entity : entities) {
            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
                cnt++;
                if (cnt >= number) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        number = tagCompound.getInteger("number");
        sensorType = SensorType.values()[tagCompound.getByte("sensor")];
        areaType = AreaType.values()[tagCompound.getByte("area")];
        groupType = GroupType.values()[tagCompound.getByte("group")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("number", number);
        tagCompound.setByte("sensor", (byte) sensorType.ordinal());
        tagCompound.setByte("area", (byte) areaType.ordinal());
        tagCompound.setByte("group", (byte) groupType.ordinal());
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETAREA.equals(command)) {
            AreaType type = NamedEnum.getEnumByName(params.get(ChoiceLabel.PARAM_CHOICE), AreaType.values());
            setAreaType(type);
            return true;
        } else if (CMD_SETTYPE.equals(command)) {
            SensorType type = NamedEnum.getEnumByName(params.get(ChoiceLabel.PARAM_CHOICE), SensorType.values());
            setSensorType(type);
            return true;
        } else if (CMD_SETGROUP.equals(command)) {
            GroupType type = NamedEnum.getEnumByName(params.get(ChoiceLabel.PARAM_CHOICE), GroupType.values());
            setGroupType(type);
            return true;
        } else if (CMD_SETNUMBER.equals(command)) {
            int number;
            try {
                number = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                number = 1;
            }
            setNumber(number);
            return true;
        }
        return false;
    }

    @Override
    public void rotateBlock(EnumFacing axis) {
        invalidateCache();
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        SensorType sensorType = getSensorType();
        if (sensorType.isSupportsNumber()) {
            probeInfo.text("Type: " + sensorType.getName() + " (" + getNumber() + ")");
        } else {
            probeInfo.text("Type: " + sensorType.getName());
        }
        int blockCount = getAreaType().getBlockCount();
        if (blockCount == 1) {
            probeInfo.text("Area: 1 block");
        } else {
            probeInfo.text("Area: " + blockCount + " blocks");
        }
        boolean rc = checkSensor();
        probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + (rc ? "on" : "off"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
    }

}
