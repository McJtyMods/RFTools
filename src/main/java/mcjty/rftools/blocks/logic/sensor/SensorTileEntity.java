package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.wrappers.*;

import java.util.List;
import java.util.Map;

import ic2.core.item.ItemFluidCell;

public class SensorTileEntity extends LogicTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SETNUMBER = "setNumber";
    public static final String CMD_SETTYPE = "setType";
    public static final String CMD_SETAREA = "setArea";
    public static final String CMD_SETGROUP = "setGroup";


    private int number = 0;
    private SensorType sensorType = SensorType.SENSOR_BLOCK;
    private AreaType areaType = AreaType.AREA_1;
    private GroupType groupType = GroupType.GROUP_ONE;

    private int checkCounter = 0;
    private AxisAlignedBB cachedBox = null;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SensorContainer.factory, 1);

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
        markDirty();
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
        cachedBox = null;
        markDirty();
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
        cachedBox = null;
        markDirty();
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
        cachedBox = null;
        markDirty();
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        checkCounter--;
        if (checkCounter > 0) {
            return;
        }
        checkCounter = 10;

        setRedstoneState(checkSensor());
    }

    public boolean checkSensor() {
        boolean newout;EnumFacing inputSide = getFacing(worldObj.getBlockState(getPos())).getInputSide();
        BlockPos newpos = getPos().offset(inputSide);

        switch (sensorType) {
            case SENSOR_BLOCK:
                newout = checkBlock(newpos, inputSide);
                break;
            case SENSOR_GROWTHLEVEL:
                newout = checkGrowthLevel(newpos, inputSide);
                break;
            case SENSOR_ENTITIES:
                newout = checkEntities(newpos, inputSide, Entity.class);
                break;
            case SENSOR_PLAYERS:
                newout = checkEntities(newpos, inputSide, EntityPlayer.class);
                break;
            case SENSOR_HOSTILE:
                newout = checkEntitiesHostile(newpos, inputSide);
                break;
            case SENSOR_PASSIVE:
                newout = checkEntitiesPassive(newpos, inputSide);
                break;
            default:
                newout = false;
        }
        return newout;
    }

    private boolean checkBlock(BlockPos newpos, EnumFacing dir) {
        int blockCount = areaType.getBlockCount();
        for (int i = 0 ; i < blockCount ; i++) {
            boolean result = checkBlock(newpos);
            if (result && groupType == GroupType.GROUP_ONE) {
                return true;
            }
            if ((!result) && groupType == GroupType.GROUP_ALL) {
                return false;
            }
            newpos = newpos.offset(dir);
        }
        return groupType == GroupType.GROUP_ALL;
    }

    private boolean checkBlock(BlockPos newpos) {
        IBlockState state = worldObj.getBlockState(newpos);
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        Block block = state.getBlock();
        if (matcher == null) {
        	if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
        		return !block.isAir(state, worldObj, newpos);
        	}
        	
            return block.isFullBlock(state);
        }
        ItemStack stack = block.getItem(worldObj, newpos, state);
        Item matcherItem = matcher.getItem();
        
        FluidStack matcherFluidStack = null;
        if (matcherItem instanceof IFluidContainerItem) {
	    	matcherFluidStack = ((IFluidContainerItem)matcherItem).getFluid(matcher);
	    	return checkFluid(block, matcherFluidStack, state, newpos);
        } else if (matcherItem instanceof ItemBucket || matcherItem instanceof UniversalBucket) {
	    	matcherFluidStack = new FluidBucketWrapper(matcher).getFluid();
	    	return checkFluid(block, matcherFluidStack, state, newpos);
	    } else if (stack != null) {
        	Item stackItem = stack.getItem();
            return matcherItem == stackItem;
        } else {
        	Item blockItem = Item.getItemFromBlock(block);
            return matcherItem == blockItem;
        }
    }

	private boolean checkFluid(Block block, FluidStack matcherFluidStack, IBlockState state, BlockPos newpos) {
	    if (matcherFluidStack == null) {
    		return block.isAir(state,  worldObj, newpos);
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

    private boolean checkGrowthLevel(BlockPos newpos, EnumFacing dir) {
        int blockCount = areaType.getBlockCount();
        for (int i = 0 ; i < blockCount ; i++) {
            boolean result = checkGrowthLevel(newpos);
            if (result && groupType == GroupType.GROUP_ONE) {
                return true;
            }
            if ((!result) && groupType == GroupType.GROUP_ALL) {
                return false;
            }
            newpos = newpos.offset(dir);
        }
        return groupType == GroupType.GROUP_ALL;
    }

    private boolean checkGrowthLevel(BlockPos newpos) {
        IBlockState state = worldObj.getBlockState(newpos);
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

    private AxisAlignedBB getCachedBox(BlockPos pos1, EnumFacing dir) {
        if (cachedBox == null) {
            int n = areaType.getBlockCount();
            cachedBox = new AxisAlignedBB(pos1);
            if (n > 1) {
                BlockPos pos2 = pos1.offset(dir, n-1);
                cachedBox = cachedBox.union(new AxisAlignedBB(pos2));
            }
            cachedBox = cachedBox.expand(.1, .1, .1);
        }
        return cachedBox;
    }

    private boolean checkEntities(BlockPos pos1, EnumFacing dir, Class<? extends Entity> clazz) {
        List<Entity> entities = worldObj.getEntitiesWithinAABB(clazz, getCachedBox(pos1, dir));
        return entities.size() >= number;
    }

    private boolean checkEntitiesHostile(BlockPos pos1, EnumFacing dir) {
        List<Entity> entities = worldObj.getEntitiesWithinAABB(EntityCreature.class, getCachedBox(pos1, dir));
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

    private boolean checkEntitiesPassive(BlockPos pos1, EnumFacing dir) {
        List<Entity> entities = worldObj.getEntitiesWithinAABB(EntityCreature.class, getCachedBox(pos1, dir));
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("rs");
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
        tagCompound.setBoolean("rs", powered);
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
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNUMBER.equals(command)) {
            setNumber(args.get("number").getInteger());
            return true;
        } else if (CMD_SETAREA.equals(command)) {
            setAreaType(AreaType.values()[args.get("type").getInteger()]);
            return true;
        } else if (CMD_SETTYPE.equals(command)) {
            setSensorType(SensorType.values()[args.get("type").getInteger()]);
            return true;
        } else if (CMD_SETGROUP.equals(command)) {
            setGroupType(GroupType.values()[args.get("type").getInteger()]);
            return true;
        }
        return false;
    }
}
