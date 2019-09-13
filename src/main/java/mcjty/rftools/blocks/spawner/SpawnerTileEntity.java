package mcjty.rftools.blocks.spawner;

import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.machineinfo.CapabilityMachineInformation;
import mcjty.lib.api.machineinfo.IMachineInformation;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.EntityTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleSupport;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.items.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.rftools.blocks.spawner.SpawnerSetup.TYPE_SPAWNER;

//import net.minecraft.entity.monster.SkeletonType;

public class SpawnerTileEntity extends GenericTileEntity implements ITickableTileEntity {

    public static final String CMD_GET_SPAWNERINFO = "getSpawnerInfo";
    public static final Key<Double> PARAM_MATTER0 = new Key<>("matter0", Type.DOUBLE);
    public static final Key<Double> PARAM_MATTER1 = new Key<>("matter1", Type.DOUBLE);
    public static final Key<Double> PARAM_MATTER2 = new Key<>("matter2", Type.DOUBLE);

    // Client side for CMD_GET_SPAWNERINFO
    public static float matterReceived0 = -1;
    public static float matterReceived1 = -1;
    public static float matterReceived2 = -1;

    public static final int SLOT_SYRINGE = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(1) {
        @Override
        protected void setup() {
            slot(SlotDefinition.specific(new ItemStack(ModItems.syringeItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_SYRINGE, 22, 8);
            playerSlots(10, 70);
        }
    };
//    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, SpawnerConfiguration.SPAWNER_MAXENERGY, SpawnerConfiguration.SPAWNER_RECEIVEPERTICK));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(SpawnerTileEntity.this));
    private LazyOptional<IMachineInformation> infoHandler = LazyOptional.of(() -> createMachineInfo());

    static final ModuleSupport MODULE_SUPPORT = new ModuleSupport(SLOT_SYRINGE) {
        @Override
        public boolean isModule(ItemStack itemStack) {
            return itemStack.getItem() == ModItems.syringeItem;
        }
    };


    private float matter[] = new float[]{0, 0, 0};
    private boolean checkSyringe = true;
    private String prevMobId = null;
    private String mobId = "";

    private AxisAlignedBB entityCheckBox = null;

    public SpawnerTileEntity() {
        super(TYPE_SPAWNER);
    }

    private void testSyringe() {
        if (!checkSyringe) {
            return;
        }
        checkSyringe = false;
        mobId = null;
        ItemStack itemStack = itemHandler.map(h -> h.getStackInSlot(SLOT_SYRINGE)).orElse(ItemStack.EMPTY);
        if (itemStack.isEmpty()) {
            clearMatter();
            return;
        }

        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound == null) {
            clearMatter();
            return;
        }

        mobId = tagCompound.getString("mobId");
        if (mobId == null) {
            clearMatter();
            return;
        }
        mobId = EntityTools.fixEntityId(mobId);
        int level = tagCompound.getInt("level");
        if (level < GeneralConfiguration.maxMobInjections.get()) {
            clearMatter();
            return;
        }
        if (prevMobId != null && !prevMobId.equals(mobId)) {
            clearMatter();
        }
    }

    private void clearMatter() {
        if (matter[0] != 0 || matter[1] != 0 || matter[2] != 0) {
            matter[0] = matter[1] = matter[2] = 0;
            markDirty();
        }
    }

    public boolean addMatter(ItemStack stack, int m, float beamerInfusionFactor) {
        testSyringe();
        if (mobId == null || mobId.isEmpty()) {
            return false;       // No matter was added.
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
            return false;
        }

        float mm = matter[materialType];
        mm += m * factor * 3.0f / (3.0f - beamerInfusionFactor);
        if (mm > SpawnerConfiguration.maxMatterStorage) {
            mm = SpawnerConfiguration.maxMatterStorage;
        }
        matter[materialType] = mm;
        markDirty();
        return true;
    }

    private List<SpawnerConfiguration.MobSpawnAmount> getSpawnAmounts() {
        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = SpawnerConfiguration.mobSpawnAmounts.get(mobId);
        boolean isDefault = spawnAmounts == null;
        if (isDefault) {
            spawnAmounts = SpawnerConfiguration.defaultSpawnAmounts;
        }
        if(spawnAmounts.size() != 3) {
            throw new IllegalStateException("The mob spawn amounts list for mob " + mobId + (isDefault ? " (the default list)" : "") + " is the wrong size. Instead of 3 elements, it contained " + spawnAmounts.size());
        }
        return spawnAmounts;
    }

    public float[] getMatter() {
        return matter;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        testSyringe();
        if (mobId == null || mobId.isEmpty()) {
            return;
        }

        List<SpawnerConfiguration.MobSpawnAmount> spawnAmounts = getSpawnAmounts();
        for (int i = 0; i < 3; i++) {
            if (matter[i] < spawnAmounts.get(i).getAmount()) {
                return;     // Not enough material yet.
            }
        }

        energyHandler.ifPresent(h -> {
            // We have enough materials. Check power.
            Integer rf = SpawnerConfiguration.mobSpawnRf.get(mobId);
            if (rf == null) {
                rf = SpawnerConfiguration.defaultMobSpawnRf;
            }

            float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(0.0f);
            rf = (int) (rf * (2.0f - factor) / 2.0f);
            if (h.getEnergy() < rf) {
                return;
            }
            h.consumeEnergy(rf);
        });

        for (int i = 0; i < 3; i++) {
            matter[i] -= spawnAmounts.get(i).getAmount();
        }

        markDirty();

        BlockState state = world.getBlockState(getPos());
        Direction k = OrientationTools.getOrientation(state);
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


        MobEntity entityLiving = null; // @todo 1.14EntityTools.createEntity(world, mobId);
        if (entityLiving == null) {
            Logging.logError("Fail to spawn mob: " + mobId);
            return;
        }

        if (entityLiving instanceof EnderDragonEntity) {
            // Ender dragon needs to be spawned with an additional NBT key set
            CompoundNBT dragonTag = new CompoundNBT();
            entityLiving.writeWithoutTypeId(dragonTag); // @todo 1.14 correct?
            dragonTag.putShort("DragonPhase", (short) 0);
            entityLiving.read(dragonTag);
        }

        if (k == Direction.DOWN) {
            sy -= entityLiving.getEyeHeight() - 1;  // @todo right? (used to be height)
        }

        entityLiving.setLocationAndAngles(sx + 0.5D, sy, sz + 0.5D, 0.0F, 0.0F);
        world.addEntity(entityLiving);
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
//                if (world.getChunkProvider().chunkExists(i1, j1)) {
//                    cnt += countEntitiesWithinChunkAABB(world.getChunkFromChunkCoords(i1, j1), aabb);
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
    public void useWrench(PlayerEntity player) {
        BlockPos coord = RFTools.instance.clientInfo.getSelectedTE();
        if (coord == null) {
            return; // Nothing to do.
        }
        TileEntity tileEntity = world.getTileEntity(coord);

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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        matter[0] = tagCompound.getFloat("matter0");
        matter[1] = tagCompound.getFloat("matter1");
        matter[2] = tagCompound.getFloat("matter2");
        if (tagCompound.contains("mobId")) {
            mobId = EntityTools.fixEntityId(tagCompound.getString("mobId"));
        } else {
            mobId = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putFloat("matter0", matter[0]);
        tagCompound.putFloat("matter1", matter[1]);
        tagCompound.putFloat("matter2", matter[2]);
        if (mobId != null && !mobId.isEmpty()) {
            tagCompound.putString("mobId", mobId);
        }
    }

    @Override
    public boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.note.pling")), SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            useWrench(player);
        }
        return true;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof SpawnerTileEntity) {
//            float[] matter = getMatter();
//            DecimalFormat fmt = new DecimalFormat("#.##");
//            fmt.setRoundingMode(RoundingMode.DOWN);
//            probeInfo.text(TextFormatting.GREEN + "Key Matter: " + fmt.format(matter[0]));
//            probeInfo.text(TextFormatting.GREEN + "Bulk Matter: " + fmt.format(matter[1]));
//            probeInfo.text(TextFormatting.GREEN + "Living Matter: " + fmt.format(matter[2]));
//        }
//    }
//
//    private static long lastTime = 0;
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        TileEntity te = accessor.getTileEntity();
//        if (te instanceof SpawnerTileEntity) {
//            if (System.currentTimeMillis() - lastTime > 500) {
//                lastTime = System.currentTimeMillis();
//                ((SpawnerTileEntity) te).requestDataFromServer(RFTools.MODID, SpawnerTileEntity.CMD_GET_SPAWNERINFO, TypedMap.EMPTY);
//            }
//
//            if (matterReceived0 >= 0) {
//                DecimalFormat fmt = new DecimalFormat("#.##");
//                fmt.setRoundingMode(RoundingMode.DOWN);
//                currenttip.add(TextFormatting.GREEN + "Key Matter: " + fmt.format(matterReceived0));
//                currenttip.add(TextFormatting.GREEN + "Bulk Matter: " + fmt.format(matterReceived1));
//                currenttip.add(TextFormatting.GREEN + "Living Matter: " + fmt.format(matterReceived2));
//            }
//        }
//    }

    @Nullable
    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GET_SPAWNERINFO.equals(command)) {
            return TypedMap.builder()
                    .put(PARAM_MATTER0, (double)matter[0])
                    .put(PARAM_MATTER1, (double)matter[1])
                    .put(PARAM_MATTER2, (double)matter[2])
                    .build();
        }
        return null;
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap result) {
        boolean rc = super.receiveDataFromServer(command, result);
        if (rc) {
            return rc;
        }
        if (CMD_GET_SPAWNERINFO.equals(command)) {
            matterReceived0 = result.get(PARAM_MATTER0).floatValue();
            matterReceived1 = result.get(PARAM_MATTER1).floatValue();
            matterReceived2 = result.get(PARAM_MATTER2).floatValue();
            return true;
        }
        return false;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(SpawnerTileEntity.this, CONTAINER_FACTORY) {
            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                checkSyringe = true;
                prevMobId = mobId;
            }
        };
    }

    private IMachineInformation createMachineInfo() {
        return new IMachineInformation() {
            private final String[] TAGS = new String[]{"matter1", "matter2", "matter3", "mob"};
            private final String[] TAG_DESCRIPTIONS = new String[]{"The amount of matter in the first slot", "The amount of matter in the second slot",
                    "The amount of matter in the third slot", "The name of the mob being spawned"};

            @Override
            public int getTagCount() {
                return TAGS.length;
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
                    case 0:
                        return Float.toString(matter[0]);
                    case 1:
                        return Float.toString(matter[1]);
                    case 2:
                        return Float.toString(matter[2]);
                    case 3:
                        return mobId;
                }
                return null;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        if (cap == CapabilityInfusable.INFUSABLE_CAPABILITY) {
            return infusableHandler.cast();
        }
        if (cap == CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY) {
            return infoHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
