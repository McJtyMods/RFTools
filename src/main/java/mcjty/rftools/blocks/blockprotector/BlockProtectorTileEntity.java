package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static mcjty.rftools.blocks.blockprotector.BlockProtectorSetup.TYPE_PROTECTOR;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
//})
public class BlockProtectorTileEntity extends GenericTileEntity implements SmartWrenchSelector, ITickableTileEntity,
        IMachineInformation /*, IPeripheral*/ {

    public static final String CMD_RSMODE = "protector.setRsMode";
    public static final String COMPONENT_NAME = "block_protector";

    private int id = -1;
    private boolean active = false;

    // Relative coordinates (relative to this tile entity)
    private Set<BlockPos> protectedBlocks = new HashSet<>();

    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true,
            BlockProtectorConfiguration.MAXENERGY.get(), BlockProtectorConfiguration.RECEIVEPERTICK.get()));
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Crafter")
            .containerSupplier((windowId,player) -> new GenericContainer(BlockProtectorSetup.CONTAINER_PROTECTOR, windowId, BlockProtectorTileEntity.CONTAINER_FACTORY, getPos(), BlockProtectorTileEntity.this))
            .energyHandler(energyHandler));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(BlockProtectorTileEntity.this));

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(0) {
        @Override
        protected void setup() {
            playerSlots(10, 70);
        }
    };


//    @Override
//    @Optional.Method(modid = "opencomputers")
//    public String getComponentName() {
//        return COMPONENT_NAME;
//    }
//
//    @Callback(doc = "Get or set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true, setter = true)
//    @Optional.Method(modid = "opencomputers")
//    public Object[] redstoneMode(Context context, Arguments args) {
//        if(args.count() == 0) {
//            return new Object[] { getRSMode().getDescription() };
//        } else {
//            String mode = args.checkString(0);
//            return setRedstoneMode(mode);
//        }
//    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRSMode(redstoneMode);
        return null;
    }

    public BlockProtectorTileEntity() {
        super(TYPE_PROTECTOR);
    }

    @Override
    public long getEnergyDiffPerTick() {
        return active ? -getRfPerTick() : 0;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return active;
    }

    @Override
    public boolean isMachineRunning() {
        return active;
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return active ? "protecting blocks" : "idle";
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    public void consumeEnergy(long amount) {
        energyHandler.ifPresent(h -> {
            h.consumeEnergy(amount);
        });
    }

    private void checkStateServer() {
        if (protectedBlocks.isEmpty()) {
            setActive(false);
            return;
        }

        if (!isMachineEnabled()) {
            setActive(false);
            return;
        } else {
            setActive(true);
        }

        consumeEnergy(getRfPerTick());
    }

    private int getRfPerTick() {
        return protectedBlocks.size() * BlockProtectorConfiguration.rfPerProtectedBlock.get();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean oldActive = active;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (active != oldActive) {
                world.func_225319_b(getPos(), null, null);
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean a) {
        active = a;
        markDirtyClient();
    }


    public boolean attemptHarvestProtection() {
        if (!isMachineEnabled()) {
            return false;
        }
        return energyHandler.map(h -> {
            long rf = h.getEnergyStored();
            if (BlockProtectorConfiguration.rfForHarvestAttempt.get() > rf) {
                return false;
            }
            h.consumeEnergy(BlockProtectorConfiguration.rfForHarvestAttempt.get());
            return true;
        }).orElse(false);
    }

    // Distance is relative with 0 being closes to the explosion and 1 being furthest away.
    public int attemptExplosionProtection(float distance, float radius) {
        if (!isMachineEnabled()) {
            return -1;
        }
        return energyHandler.map(h -> {
            long rf = h.getEnergyStored();
            int rfneeded = (int) (BlockProtectorConfiguration.rfForExplosionProtection.get() * (1.0 - distance) * radius / 8.0f) + 1;
            float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(0.0f);
            rfneeded = (int) (rfneeded * (2.0f - factor) / 2.0f);

            if (rfneeded > rf) {
                return -1;
            }
            if (rfneeded <= 0) {
                rfneeded = 1;
            }
            consumeEnergy(rfneeded);
            return rfneeded;
        }).orElse(0);
    }

    public Set<BlockPos> getProtectedBlocks() {
        return protectedBlocks;
    }

    public BlockPos absoluteToRelative(BlockPos c) {
        return absoluteToRelative(c.getX(), c.getY(), c.getZ());
    }

    public BlockPos absoluteToRelative(int x, int y, int z) {
        return new BlockPos(x - getPos().getX(), y - getPos().getY(), z - getPos().getZ());
    }

    // Test if this relative coordinate is protected.
    public boolean isProtected(BlockPos c) {
        return protectedBlocks.contains(c);
    }

    // Used by the explosion event handler.
    public void removeProtection(BlockPos relative) {
        protectedBlocks.remove(relative);
        markDirtyClient();
    }

    // Toggle a coordinate to be protected or not. The coordinate given here is absolute.
    public void toggleCoordinate(GlobalCoordinate c) {
        if (c.getDimension() != getWorld().getDimension().getType().getId()) {
            // Wrong dimension. Don't do anything.
            return;
        }
        BlockPos relative = absoluteToRelative(c.getCoordinate());
        if (protectedBlocks.contains(relative)) {
            protectedBlocks.remove(relative);
        } else {
            protectedBlocks.add(relative);
        }
        markDirtyClient();
    }

    @Override
    public void selectBlock(PlayerEntity player, BlockPos pos) {
        // This is always called server side.
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (Math.abs(pos.getX()-xCoord) > BlockProtectorConfiguration.maxProtectDistance.get()
                || Math.abs(pos.getY()-yCoord) > BlockProtectorConfiguration.maxProtectDistance.get()
                || Math.abs(pos.getZ()-zCoord) > BlockProtectorConfiguration.maxProtectDistance.get()) {
            Logging.message(player, TextFormatting.RED + "Block out of range of the block protector!");
            return;
        }
        GlobalCoordinate gc = new GlobalCoordinate(pos, getWorld().getDimension().getType().getId());
        toggleCoordinate(gc);
    }

    public int getOrCalculateID() {
        if (id == -1) {
            BlockProtectors protectors = BlockProtectors.get();
            GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().getDimension().getType().getId());
            id = protectors.getNewId(gc);
            protectors.save();
            setId(id);
        }
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        markDirtyClient();
    }

    /**
     * This method is called after putting down a protector that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        BlockProtectors protectors = BlockProtectors.get();

        GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().getDimension().getType().getId());

        if (id == -1) {
            id = protectors.getNewId(gc);
            markDirty();
        } else {
            protectors.assignId(gc, id);
        }

        protectors.save();
        markDirtyClient();
    }


    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

        ListNBT tagList = tagCompound.getList("coordinates", Constants.NBT.TAG_COMPOUND);
        protectedBlocks.clear();
        for (INBT inbt : tagList) {
            CompoundNBT tag = (CompoundNBT) inbt;
            protectedBlocks.add(BlockPosTools.read(tag, "c"));
        }
        active = tagCompound.getBoolean("active");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        if (tagCompound.contains("protectorId")) {
            id = tagCompound.getInt("protectorId");
        } else {
            id = -1;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        ListNBT list = new ListNBT();
        for (BlockPos block : protectedBlocks) {
            list.add(BlockPosTools.write(block));
        }
        tagCompound.put("coordinates", list);
        tagCompound.putBoolean("active", active);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }


    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("protectorId", id);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        if (cap == CapabilityInfusable.INFUSABLE_CAPABILITY) {
            return infusableHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
