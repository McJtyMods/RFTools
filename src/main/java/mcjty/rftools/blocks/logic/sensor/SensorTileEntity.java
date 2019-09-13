package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftools.varia.NamedEnum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_SENSOR;

public class SensorTileEntity extends LogicTileEntity implements ITickableTileEntity {

    public static final String CMD_SETNUMBER = "sensor.setNumber";
    public static final String CMD_SETTYPE = "sensor.setType";
    public static final String CMD_SETAREA = "sensor.setArea";
    public static final String CMD_SETGROUP = "sensor.setGroup";

    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_ITEMMATCH = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(1) {
        @Override
        protected void setup() {
            slot(SlotDefinition.ghost(), CONTAINER_CONTAINER, SLOT_ITEMMATCH, 154, 24);
            playerSlots(10, 70);
        }
    };


    private int number = 0;
    private SensorType sensorType = SensorType.SENSOR_BLOCK;
    private AreaType areaType = AreaType.AREA_1;
    private GroupType groupType = GroupType.GROUP_ONE;

    private int checkCounter = 0;
    private AxisAlignedBB cachedBox = null;

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);

    public SensorTileEntity() {
        super(TYPE_SENSOR);
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
    public void tick() {
        if (world.isRemote) {
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
        LogicFacing facing = getFacing(world.getBlockState(getPos()));
        Direction inputSide = facing.getInputSide();
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
                newout = checkEntities(newpos, facing, inputSide, PlayerEntity.class);
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

    private boolean checkBlockOrFluid(BlockPos newpos, LogicFacing facing, Direction dir, Function<BlockPos, Boolean> blockChecker) {
        int blockCount = areaType.getBlockCount();
        if (blockCount > 0) {
            Boolean x = checkBlockOrFluidRow(newpos, dir, blockChecker, blockCount);
            if (x != null) return x;
        } else if (blockCount < 0) {
            // Area
            Direction downSide = facing.getSide();
            Direction inputSide = facing.getInputSide();
            Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);

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

    private Boolean checkBlockOrFluidRow(BlockPos newpos, Direction dir, Function<BlockPos, Boolean> blockChecker, int count) {
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
        return itemHandler.map(h -> {
            BlockState state = world.getBlockState(newpos);
            ItemStack matcher = h.getStackInSlot(SLOT_ITEMMATCH);
            if (matcher.isEmpty()) {
                return true; // @todo 1.14, how to do this? state.getBlock().isFullBlock(state);
            }
            ItemStack stack = state.getBlock().getItem(world, newpos, state);
            if (!stack.isEmpty()) {
                return matcher.getItem() == stack.getItem();
            } else {
                return matcher.getItem() == Item.getItemFromBlock(state.getBlock());
            }
        }).orElse(false);
    }

    private boolean checkFluid(BlockPos newpos) {
        return itemHandler.map(h -> {
            BlockState state = world.getBlockState(newpos);
            ItemStack matcher = h.getStackInSlot(SLOT_ITEMMATCH);
            Block block = state.getBlock();
            if (matcher.isEmpty()) {
                if (block instanceof FlowingFluidBlock || block instanceof IFluidBlock) {
                    return !block.isAir(state, world, newpos);
                }

                return false;
            }
            ItemStack stack = block.getItem(world, newpos, state);
            Item matcherItem = matcher.getItem();

            FluidStack matcherFluidStack = null;
//        if (matcherItem instanceof IFluidContainerItem) {
//            matcherFluidStack = ((IFluidContainerItem)matcherItem).getFluid(matcher);
//            return checkFluid(block, matcherFluidStack, state, newpos);
//        }
            if (matcherItem instanceof BucketItem ) { // @todo || matcherItem instanceof UniversalBucket) {
                matcherFluidStack = new FluidBucketWrapper(matcher).getFluid();
                return checkFluid(block, matcherFluidStack, state, newpos);
            }

            return false;
        }).orElse(false);
    }

    private boolean checkFluid(Block block, FluidStack matcherFluidStack, BlockState state, BlockPos newpos) {
        if (matcherFluidStack == null) {
            return block.isAir(state,  world, newpos);
        }

        Fluid matcherFluid = matcherFluidStack.getFluid();
        if (matcherFluid == null) {
            return false;
        }

        BlockState matcherFluidBlock = matcherFluid.getDefaultState().getBlockState();
        if (matcherFluidBlock == null) {
            return false;
        }

        ResourceLocation matcherBlockName = matcherFluidBlock.getBlock().getRegistryName(); // @todo 1.14 match on blockstate and not block
        ResourceLocation blockName = block.getRegistryName();
        return blockName.equals(matcherBlockName);
    }

    private boolean checkGrowthLevel(BlockPos newpos, LogicFacing facing, Direction dir) {
        int blockCount = areaType.getBlockCount();
        if (blockCount > 0) {
            Boolean x = checkGrowthLevelRow(newpos, dir, blockCount);
            if (x != null) return x;
        } else if (blockCount < 0) {
            // Area
            Direction downSide = facing.getSide();
            Direction inputSide = facing.getInputSide();
            Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);

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

    private Boolean checkGrowthLevelRow(BlockPos newpos, Direction dir, int blockCount) {
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
        BlockState state = world.getBlockState(newpos);
        int pct = 0;
        for (IProperty<?> property : state.getProperties()) {
            if(!"age".equals(property.getName())) continue;
            if(property.getValueClass() == Integer.class) {
                IProperty<Integer> integerProperty = (IProperty<Integer>)property;
                int age = state.get(integerProperty);
                int maxAge = Collections.max(integerProperty.getAllowedValues());
                pct = (age * 100) / maxAge;
            }
            break;
        }
        return pct >= number;
    }

    public void invalidateCache() {
        cachedBox = null;
    }

    private AxisAlignedBB getCachedBox(BlockPos pos1, LogicFacing facing, Direction dir) {
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
                Direction downSide = facing.getSide();
                Direction inputSide = facing.getInputSide();
                Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
                Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);
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

    private boolean checkEntities(BlockPos pos1, LogicFacing facing, Direction dir, Class<? extends Entity> clazz) {
        List<Entity> entities = world.getEntitiesWithinAABB(clazz, getCachedBox(pos1, facing, dir));
        return entities.size() >= number;
    }

    private boolean checkEntitiesHostile(BlockPos pos1, LogicFacing facing, Direction dir) {
        List<Entity> entities = world.getEntitiesWithinAABB(CreatureEntity.class, getCachedBox(pos1, facing, dir));
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

    private boolean checkEntitiesPassive(BlockPos pos1, LogicFacing facing, Direction dir) {
        List<Entity> entities = world.getEntitiesWithinAABB(CreatureEntity.class, getCachedBox(pos1, facing, dir));
        int cnt = 0;
        for (Entity entity : entities) {
            if (entity instanceof AnimalEntity && !(entity instanceof IMob)) {
                cnt++;
                if (cnt >= number) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        number = tagCompound.getInt("number");
        sensorType = SensorType.values()[tagCompound.getByte("sensor")];
        areaType = AreaType.values()[tagCompound.getByte("area")];
        groupType = GroupType.values()[tagCompound.getByte("group")];
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("number", number);
        tagCompound.putByte("sensor", (byte) sensorType.ordinal());
        tagCompound.putByte("area", (byte) areaType.ordinal());
        tagCompound.putByte("group", (byte) groupType.ordinal());
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
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
    public void rotateBlock(Rotation axis) {
        invalidateCache();
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        SensorType sensorType = getSensorType();
//        if (sensorType.isSupportsNumber()) {
//            probeInfo.text("Type: " + sensorType.getName() + " (" + getNumber() + ")");
//        } else {
//            probeInfo.text("Type: " + sensorType.getName());
//        }
//        int blockCount = getAreaType().getBlockCount();
//        if (blockCount == 1) {
//            probeInfo.text("Area: 1 block");
//        } else if (blockCount < 0) {
//            probeInfo.text("Area: " + (-blockCount) + "x" + (-blockCount) + " blocks");
//        } else {
//            probeInfo.text("Area: " + blockCount + " blocks");
//        }
//        boolean rc = checkSensor();
//        probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + (rc ? "on" : "off"));
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//    }


    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(SensorTileEntity.this, CONTAINER_FACTORY) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return false;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        return super.getCapability(cap, facing);
    }
}
