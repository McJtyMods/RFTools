package com.mcjty.rftools.blocks.spawner;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.varia.Coordinate;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SpawnerTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SpawnerContainer.factory, 1);

    private float matter[] = new float[] { 0, 0, 0 };
    private boolean checkSyringe = true;
    private String mobName = "";

    public SpawnerTileEntity() {
        super(SpawnerConfiguration.SPAWNER_MAXENERGY, SpawnerConfiguration.SPAWNER_RECEIVEPERTICK);
    }

    private void testSyringe() {
        if (!checkSyringe) {
            return;
        }
        checkSyringe = false;
        mobName = "";
        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            return;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            return;
        }

        String mob = tagCompound.getString("mobName");
        if (mob == null) {
            return;
        }
        int level = tagCompound.getInteger("level");
        if (level < DimletConstructionConfiguration.maxMobInjections) {
            return;
        }
        mobName = mob;
    }

    public int addMatter(ItemStack stack, int m) {
        testSyringe();
        if (mobName.isEmpty()) {
            return 0;       // No matter was added.
        }
        int materialType = 0;
        Float factor = null;
        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = SpawnerConfiguration.mobSpawnAmounts.get(mobName);
        for (SpawnerConfiguration.MobSpawnAmount spawnAmount : spawnAmounts) {
            factor = spawnAmount.match(stack);
            if (factor != null) {
                break;
            }
            materialType++;
        }
        if (factor == null) {
            // This type of material is not supported by the spawner.
            return 0;
        }

        matter[materialType] += m * factor;
//        if (matter > SpawnerConfiguration.maxMatterStorage) {
//            matter = SpawnerConfiguration.maxMatterStorage;
//        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return m;
    }

    public float[] getMatter() {
        return matter;
    }

    @Override
    protected void checkStateServer() {
        testSyringe();
        if (mobName.isEmpty()) {
            return;
        }

        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = SpawnerConfiguration.mobSpawnAmounts.get(mobName);
        for (int i = 0 ; i < 3 ; i++) {
            if (matter[i] < spawnAmounts.get(i).getAmount()) {
                return;     // Not enough material yet.
            }
        }

        for (int i = 0 ; i < 3 ; i++) {
            matter[i] -= spawnAmounts.get(i).getAmount();
        }

        markDirty();

        // @todo for now, later we may want to support mobs that have no dimlets.
        DimletKey key = new DimletKey(DimletType.DIMLET_MOBS, mobName);

        MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(key);
        EntityLiving entityLiving;
        try {
            entityLiving = descriptor.getEntityClass().getConstructor(World.class).newInstance(worldObj);
        } catch (InstantiationException e) {
            RFTools.logError("Fail to spawn mob: " + mobName);
            return;
        } catch (IllegalAccessException e) {
            RFTools.logError("Fail to spawn mob: " + mobName);
            return;
        } catch (InvocationTargetException e) {
            RFTools.logError("Fail to spawn mob: " + mobName);
            return;
        } catch (NoSuchMethodException e) {
            RFTools.logError("Fail to spawn mob: " + mobName);
            return;
        }

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection k = BlockTools.getOrientation(meta);
        int sx = xCoord;
        int sy = yCoord;
        int sz = zCoord;
        sx += k.offsetX;
        sy += k.offsetY;
        sz += k.offsetZ;
        if (k == ForgeDirection.DOWN) {
            sy -= entityLiving.height - 1;
        }

        entityLiving.setLocationAndAngles(sx + 0.5D, (double) sy, sz + 0.5D, 0.0F, 0.0F);
        worldObj.spawnEntityInWorld(entityLiving);
    }

    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        Coordinate coord = RFTools.instance.clientInfo.getSelectedTE();
        if (coord == null) {
            return; // Nothing to do.
        }
        Coordinate thisCoord = new Coordinate(xCoord, yCoord, zCoord);
        TileEntity tileEntity = worldObj.getTileEntity(coord.getX(), coord.getY(), coord.getZ());

        if (tileEntity instanceof MatterBeamerTileEntity) {
            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) tileEntity;
            matterBeamerTileEntity.setDestination(thisCoord);
            RFTools.message(player, "Destination set!");
        }

        RFTools.instance.clientInfo.setSelectedTE(null);
        RFTools.instance.clientInfo.setDestinationTE(null);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        matter[0] = tagCompound.getFloat("matter0");
        matter[1] = tagCompound.getFloat("matter1");
        matter[2] = tagCompound.getFloat("matter2");
        checkSyringe = tagCompound.getBoolean("checkSyringe");
        mobName = tagCompound.getString("mobName");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setFloat("matter0", matter[0]);
        tagCompound.setFloat("matter1", matter[1]);
        tagCompound.setFloat("matter2", matter[2]);
        tagCompound.setBoolean("checkSyringe", checkSyringe);
        tagCompound.setString("mobName", mobName);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return SpawnerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return SpawnerContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return SpawnerContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        checkSyringe = true;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        checkSyringe = true;
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }


    @Override
    public String getInventoryName() {
        return "Spawner Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
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
