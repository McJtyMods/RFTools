package mcjty.rftools.blocks.spawner;

import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.rftools.blocks.spawner.SpawnerSetup.TYPE_MATTER_BEAMER;


public class MatterBeamerTileEntity extends GenericTileEntity implements ITickableTileEntity {

    public static final int TICKTIME = 20;
    public static final int SLOT_MATERIAL = 0;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), ContainerFactory.CONTAINER_CONTAINER, SLOT_MATERIAL, 28, 8, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, SpawnerConfiguration.BEAMER_MAXENERGY, SpawnerConfiguration.BEAMER_RECEIVEPERTICK));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(MatterBeamerTileEntity.this));

    public static final Key<BlockPos> VALUE_DESTINATION = new Key<>("destination", Type.BLOCKPOS);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_DESTINATION, this::getDestination, this::setDestination)
        };
    }


    // The location of the destination spawner..
    private BlockPos destination = null;
    private boolean glowing = false;

    private int ticker = TICKTIME;

    public MatterBeamerTileEntity() {
        super(TYPE_MATTER_BEAMER);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    public boolean isPowered() {
        return powerLevel != 0;
    }

    public boolean isGlowing() {
        return glowing;
    }

    private void checkStateServer() {
        if (powerLevel == 0) {
            disableBlockGlow();
            return;
        }

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = TICKTIME;

        itemHandler.ifPresent(h -> {
            energyHandler.ifPresent(e -> {
                TileEntity te = null;
                if (destination != null) {
                    te = world.getTileEntity(destination);
                    if (!(te instanceof SpawnerTileEntity)) {
                        setDestination(null);
                        return;
                    }
                } else {
                    return;
                }

                ItemStack itemStack = h.getStackInSlot(0);
                if (itemStack.isEmpty()) {
                    disableBlockGlow();
                    return;
                }

                SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) te;

                float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(0.0f);
                int maxblocks = (int) (SpawnerConfiguration.beamBlocksPerSend * (1.01 + factor * 2.0));
                int numblocks = Math.min(maxblocks, itemStack.getCount());

                int rf = (int) (SpawnerConfiguration.beamRfPerObject * numblocks * (4.0f - factor) / 4.0f);
                if (e.getEnergy() < rf) {
                    return;
                }
                e.consumeEnergy(rf);

                if (spawnerTileEntity.addMatter(itemStack, numblocks, factor)) {
                    h.extractItem(0, numblocks, false);
                    enableBlockGlow();
                }
            });
        });
    }

    private void disableBlockGlow() {
        if (glowing) {
            glowing = false;
            markDirtyClient();
        }
    }

    private void enableBlockGlow() {
        if (!glowing) {
            glowing = true;
            markDirtyClient();
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean oldglowing = glowing;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (oldglowing != glowing) {
                world.func_225319_b(getPos(), null, null);
//                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }


    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }
//
//    @Override
//    public AxisAlignedBB getRenderBoundingBox() {
//        int xCoord = getPos().getX();
//        int yCoord = getPos().getY();
//        int zCoord = getPos().getZ();
//        return new AxisAlignedBB(xCoord - 4, yCoord - 4, zCoord - 4, xCoord + 5, yCoord + 5, zCoord + 5);
//    }


    // Called from client side when a wrench is used.
    public void useWrench(PlayerEntity player) {
        BlockPos coord = RFTools.instance.clientInfo.getSelectedTE();
        TileEntity tileEntity = null;
        if (coord != null) {
            tileEntity = world.getTileEntity(coord);
        }

        if (!(tileEntity instanceof MatterBeamerTileEntity)) {
            // None selected. Just select this one.
            RFTools.instance.clientInfo.setSelectedTE(getPos());
            SpawnerTileEntity destinationTE = getDestinationTE();
            if (destinationTE == null) {
                RFTools.instance.clientInfo.setDestinationTE(null);
            } else {
                RFTools.instance.clientInfo.setDestinationTE(destinationTE.getPos());
            }
            Logging.message(player, "Select a spawner as destination");
        } else if (coord.equals(getPos())) {
            // Unselect this one.
            RFTools.instance.clientInfo.setSelectedTE(null);
            RFTools.instance.clientInfo.setDestinationTE(null);
            setDestination(null);
            Logging.message(player, "Destination cleared!");
        }
    }

    public void setDestination(BlockPos destination) {
        this.destination = destination;
        disableBlockGlow();
        markDirty();

        if (world.isRemote) {
            // We're on the client. Send change to server.
            valueToServer(RFToolsMessages.INSTANCE, VALUE_DESTINATION, destination);
        } else {
            markDirtyClient();
        }
    }

    public BlockPos getDestination() {
        return destination;
    }

    /**
     * Get the current destination. This function checks first if that destination is
     * still valid and if not it is reset to null (i.e. the destination was removed).
     * @return the destination TE or null if there is no valid one
     */
    private SpawnerTileEntity getDestinationTE() {
        if (destination == null) {
            return null;
        }
        TileEntity te = world.getTileEntity(destination);
        if (te instanceof SpawnerTileEntity) {
            return (SpawnerTileEntity) te;
        } else {
            destination = null;
            markDirtyClient();
            return null;
        }
    }


    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        destination = BlockPosTools.read(tagCompound, "dest");
        glowing = tagCompound.getBoolean("glowing");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        itemHandler.ifPresent(h -> h.deserializeNBT(tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND)));
        energyHandler.ifPresent(h -> h.setEnergy(tagCompound.getLong("Energy")));
    }


    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        BlockPosTools.write(tagCompound, "dest", destination);
        tagCompound.putBoolean("glowing", glowing);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        itemHandler.ifPresent(h -> tagCompound.put("Items", h.serializeNBT()));
        energyHandler.ifPresent(h -> tagCompound.putLong("Energy", h.getEnergy()));
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(MatterBeamerTileEntity.this, CONTAINER_FACTORY, 1);
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
        return super.getCapability(cap, facing);
    }
}
