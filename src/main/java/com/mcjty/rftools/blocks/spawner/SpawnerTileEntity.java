package com.mcjty.rftools.blocks.spawner;

import com.mcjty.api.MachineInformation;
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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SpawnerTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory, MachineInformation {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SpawnerContainer.factory, 1);

    private static final String[] TAGS = new String[]{"matter1", "matter2", "matter3", "mob"};
    private static final String[] TAG_DESCRIPTIONS = new String[]{"The amount of matter in the first slot", "The amount of matter in the second slot",
            "The amount of matter in the third slot", "The name of the mob being spawned"};

    private float matter[] = new float[] { 0, 0, 0 };
    private boolean checkSyringe = true;
    private String mobName = "";

    private AxisAlignedBB entityCheckBox = null;


    public SpawnerTileEntity() {
        super(SpawnerConfiguration.SPAWNER_MAXENERGY, SpawnerConfiguration.SPAWNER_RECEIVEPERTICK);
    }

    @Override
    public int getTagCount() {
        return 4;
    }

    @Override
    public String getTagName(int index) {
        return TAGS[index];
    }

    @Override
    public String getTagDescription(int index) {
        return TAG_DESCRIPTIONS[index];
    }

    @Override
    public String getData(int index, long millis) {
        switch (index) {
            case 0: return Float.toString(matter[0]);
            case 1: return Float.toString(matter[1]);
            case 2: return Float.toString(matter[2]);
            case 3: return mobName;
        }
        return null;
    }

    private void testSyringe() {
        if (!checkSyringe) {
            return;
        }
        checkSyringe = false;
        mobName = "";
        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            clearMatter();
            return;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            clearMatter();
            return;
        }

        String mob = tagCompound.getString("mobName");
        if (mob == null) {
            clearMatter();
            return;
        }
        int level = tagCompound.getInteger("level");
        if (level < DimletConstructionConfiguration.maxMobInjections) {
            clearMatter();
            return;
        }
        mobName = mob;
    }

    private void clearMatter() {
        if (matter[0] != 0 || matter[1] != 0 || matter[2] != 0) {
            matter[0] = matter[1] = matter[2] = 0;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void addMatter(ItemStack stack, int m) {
        testSyringe();
        if (mobName.isEmpty()) {
            return;       // No matter was added.
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
            return;
        }

        float mm = matter[materialType];
        mm += m * factor;
        if (mm > SpawnerConfiguration.maxMatterStorage) {
            mm = SpawnerConfiguration.maxMatterStorage;
        }
        matter[materialType] = mm;

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

        // We have enough materials. Check power.
        int rf = SpawnerConfiguration.mobSpawnRf.get(mobName);
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);
        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            return;
        }
        extractEnergy(ForgeDirection.DOWN, rf, false);

        for (int i = 0 ; i < 3 ; i++) {
            matter[i] -= spawnAmounts.get(i).getAmount();
        }

        markDirty();

        // @todo for now, later we may want to support mobs that have no dimlets.
        DimletKey key = new DimletKey(DimletType.DIMLET_MOBS, mobName);

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection k = BlockTools.getOrientation(meta);
        int sx = xCoord;
        int sy = yCoord;
        int sz = zCoord;
        sx += k.offsetX;
        sy += k.offsetY;
        sz += k.offsetZ;

//        if (entityCheckBox == null) {
//            entityCheckBox = AxisAlignedBB.getBoundingBox(xCoord-9, yCoord-9, zCoord-9, xCoord+sx+10, yCoord+sy+10, zCoord+sz+10);
//        }
//
//        int cnt = countEntitiesWithinAABB(entityCheckBox);
//        if (cnt >= SpawnerConfiguration.maxEntitiesAroundSpawner) {
//            return;
//        }
//
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

        if (k == ForgeDirection.DOWN) {
            sy -= entityLiving.height - 1;
        }

        entityLiving.setLocationAndAngles(sx + 0.5D, (double) sy, sz + 0.5D, 0.0F, 0.0F);
        worldObj.spawnEntityInWorld(entityLiving);
    }

//    private int countEntitiesWithinAABB(AxisAlignedBB aabb) {
//        int i = MathHelper.floor_double((aabb.minX - World.MAX_ENTITY_RADIUS) / 16.0D);
//        int j = MathHelper.floor_double((aabb.maxX + World.MAX_ENTITY_RADIUS) / 16.0D);
//        int k = MathHelper.floor_double((aabb.minZ - World.MAX_ENTITY_RADIUS) / 16.0D);
//        int l = MathHelper.floor_double((aabb.maxZ + World.MAX_ENTITY_RADIUS) / 16.0D);
//
//        int cnt = 0;
//        for (int i1 = i; i1 <= j; ++i1) {
//            for (int j1 = k; j1 <= l; ++j1) {
//                if (worldObj.getChunkProvider().chunkExists(i1, j1)) {
//                    cnt += countEntitiesWithinChunkAABB(worldObj.getChunkFromChunkCoords(i1, j1), aabb);
//                }
//            }
//        }
//        return cnt;
//    }
//
//    private int countEntitiesWithinChunkAABB(Chunk chunk, AxisAlignedBB aabb) {
//        int cnt = 0;
//        int i = MathHelper.floor_double((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
//        int j = MathHelper.floor_double((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
//        i = MathHelper.clamp_int(i, 0, chunk.entityLists.length - 1);
//        j = MathHelper.clamp_int(j, 0, chunk.entityLists.length - 1);
//
//        for (int k = i; k <= j; ++k) {
//            List entityList = chunk.entityLists[k];
//            cnt += entityList.size();
//        }
//        return cnt;
//    }
//
//

    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        Coordinate coord = RFTools.instance.clientInfo.getSelectedTE();
        if (coord == null) {
            return; // Nothing to do.
        }
        Coordinate thisCoord = new Coordinate(xCoord, yCoord, zCoord);
        TileEntity tileEntity = worldObj.getTileEntity(coord.getX(), coord.getY(), coord.getZ());

        double d = Vec3.createVectorHelper(coord.getX(), coord.getY(), coord.getZ()).distanceTo(
                Vec3.createVectorHelper(xCoord, yCoord, zCoord));
        if (d > SpawnerConfiguration.maxBeamDistance) {
            RFTools.message(player, "Destination distance is too far!");
        } else if (tileEntity instanceof MatterBeamerTileEntity) {
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
