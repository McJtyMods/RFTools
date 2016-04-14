package mcjty.rftools.blocks.spawner;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.CustomSidedInvWrapper;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SpawnerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, MachineInformation, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SpawnerContainer.factory, 1);

    private static final String[] TAGS = new String[]{"matter1", "matter2", "matter3", "mob"};
    private static final String[] TAG_DESCRIPTIONS = new String[]{"The amount of matter in the first slot", "The amount of matter in the second slot",
            "The amount of matter in the third slot", "The name of the mob being spawned"};

    private float matter[] = new float[] { 0, 0, 0 };
    private boolean checkSyringe = true;
    private String mobId = "";

    private AxisAlignedBB entityCheckBox = null;

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

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
            case 3: return mobId;
        }
        return null;
    }

    private void testSyringe() {
        if (!checkSyringe) {
            return;
        }
        checkSyringe = false;
        mobId = null;
        ItemStack itemStack = inventoryHelper.getStackInSlot(0);
        if (itemStack == null || itemStack.stackSize == 0) {
            clearMatter();
            return;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            clearMatter();
            return;
        }

        String mob = tagCompound.getString("mobClass");
        if (mob == null) {
            clearMatter();
            return;
        }
        int level = tagCompound.getInteger("level");
        if (level < GeneralConfiguration.maxMobInjections) {
            clearMatter();
            return;
        }

        try {
            Class<?> clazz = Class.forName(mob);
            mobId = EntityList.classToStringMapping.get(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearMatter() {
        if (matter[0] != 0 || matter[1] != 0 || matter[2] != 0) {
            matter[0] = matter[1] = matter[2] = 0;
            markDirtyClient();
        }
    }

    public void addMatter(ItemStack stack, int m) {
        testSyringe();
        if (mobId == null || mobId.isEmpty()) {
            return;       // No matter was added.
        }
        int materialType = 0;
        Float factor = null;
        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = getSpawnAmounts();
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

        markDirtyClient();
    }

    private List<SpawnerConfiguration.MobSpawnAmount> getSpawnAmounts() {
        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = SpawnerConfiguration.mobSpawnAmounts.get(mobId);
        if (spawnAmounts == null) {
            spawnAmounts = SpawnerConfiguration.defaultSpawnAmounts;
        }
        return spawnAmounts;
    }

    public float[] getMatter() {
        return matter;
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        testSyringe();
        if (mobId == null || mobId.isEmpty()) {
            return;
        }

        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = getSpawnAmounts();
        for (int i = 0 ; i < 3 ; i++) {
            if (matter[i] < spawnAmounts.get(i).getAmount()) {
                return;     // Not enough material yet.
            }
        }

        // We have enough materials. Check power.
        Integer rf = SpawnerConfiguration.mobSpawnRf.get(mobId);
        if (rf == null) {
            rf = SpawnerConfiguration.defaultMobSpawnRf;
        }

        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);
        if (getEnergyStored(EnumFacing.DOWN) < rf) {
            return;
        }
        consumeEnergy(rf);

        for (int i = 0 ; i < 3 ; i++) {
            matter[i] -= spawnAmounts.get(i).getAmount();
        }

        markDirty();

        IBlockState state = worldObj.getBlockState(getPos());
        int meta = state.getBlock().getMetaFromState(state);
        EnumFacing k = BlockTools.getOrientation(meta);
        int sx = getPos().getX();
        int sy = getPos().getY();
        int sz = getPos().getZ();
        Vec3i dir = k.getDirectionVec();
        sx += dir.getX();
        sy += dir.getY();
        sz += dir.getZ();


//        if (entityCheckBox == null) {
//            entityCheckBox = AxisAlignedBB.getBoundingBox(xCoord-9, yCoord-9, zCoord-9, xCoord+sx+10, yCoord+sy+10, zCoord+sz+10);
//        }
//
//        int cnt = countEntitiesWithinAABB(entityCheckBox);
//        if (cnt >= SpawnerConfiguration.maxEntitiesAroundSpawner) {
//            return;
//        }


        EntityLiving entityLiving;
        try {
            Class<? extends Entity> clazz = EntityList.stringToClassMapping.get(mobId);
            entityLiving = (EntityLiving) clazz.getConstructor(World.class).newInstance(worldObj);
        } catch (InstantiationException e) {
            Logging.logError("Fail to spawn mob: " + mobId);
            return;
        } catch (IllegalAccessException e) {
            Logging.logError("Fail to spawn mob: " + mobId);
            return;
        } catch (InvocationTargetException e) {
            Logging.logError("Fail to spawn mob: " + mobId);
            return;
        } catch (NoSuchMethodException e) {
            Logging.logError("Fail to spawn mob: " + mobId);
            return;
        }

        if (k == EnumFacing.DOWN) {
            sy -= entityLiving.getEyeHeight() - 1;  // @todo right? (used to be height)
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
        BlockPos coord = RFTools.instance.clientInfo.getSelectedTE();
        if (coord == null) {
            return; // Nothing to do.
        }
        TileEntity tileEntity = worldObj.getTileEntity(coord);

        double d = new Vec3d(coord).distanceTo(new Vec3d(getPos()));
        if (d > SpawnerConfiguration.maxBeamDistance) {
            Logging.message(player, "Destination distance is too far!");
        } else if (tileEntity instanceof MatterBeamerTileEntity) {
            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) tileEntity;
            matterBeamerTileEntity.setDestination(getPos());
            Logging.message(player, "Destination set!");
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
        readBufferFromNBT(tagCompound, inventoryHelper);
        matter[0] = tagCompound.getFloat("matter0");
        matter[1] = tagCompound.getFloat("matter1");
        matter[2] = tagCompound.getFloat("matter2");
        checkSyringe = tagCompound.getBoolean("checkSyringe");
        if (tagCompound.hasKey("mobId")) {
            mobId = tagCompound.getString("mobId");
        } else {
            mobId = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setFloat("matter0", matter[0]);
        tagCompound.setFloat("matter1", matter[1]);
        tagCompound.setFloat("matter2", matter[2]);
        tagCompound.setBoolean("checkSyringe", checkSyringe);
        if (mobId != null && !mobId.isEmpty()) {
            tagCompound.setString("mobId", mobId);
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SpawnerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        checkSyringe = true;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        checkSyringe = true;
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }


    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
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
