package mcjty.rftools.blocks.blockprotector;

import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.entity.SyncedValueSet;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.items.smartwrench.SmartWrenchSelector;
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;
import java.util.Set;

public class BlockProtectorTileEntity extends GenericEnergyReceiverTileEntity implements SmartWrenchSelector {

    public static final String CMD_RSMODE = "rsMode";

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private int powered = 0;
    private int id = -1;

    // Relative coordinates (relative to this tile entity)
    private SyncedValueSet<Coordinate> protectedBlocks = new SyncedValueSet<Coordinate>() {
        @Override
        public Coordinate readElementFromNBT(NBTTagCompound tagCompound) {
            return Coordinate.readFromNBT(tagCompound, "c");
        }

        @Override
        public NBTTagCompound writeElementToNBT(Coordinate element) {
            return Coordinate.writeToNBT(element);
        }
    };

    public BlockProtectorTileEntity() {
        super(BlockProtectorConfiguration.MAXENERGY, BlockProtectorConfiguration.RECEIVEPERTICK);
        registerSyncedObject(protectedBlocks);
    }

    @Override
    protected void checkStateServer() {
        if (protectedBlocks.isEmpty()) {
            return;
        }

        if (isDisabled()) return;

        consumeEnergy(protectedBlocks.size() * BlockProtectorConfiguration.rfPerProtectedBlock);
    }

    private boolean isDisabled() {
        if (redstoneMode != RedstoneMode.REDSTONE_IGNORED) {
            boolean rs = powered > 0;
            if (redstoneMode == RedstoneMode.REDSTONE_OFFREQUIRED) {
                if (rs) {
                    return true;
                }
            } else if (redstoneMode == RedstoneMode.REDSTONE_ONREQUIRED) {
                if (!rs) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markDirty();
        }
    }


    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }


    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public boolean attemptHarvestProtection() {
        if (isDisabled()) return false;
        int rf = getEnergyStored(ForgeDirection.DOWN);
        if (BlockProtectorConfiguration.rfForHarvestAttempt > rf) {
            return false;
        }
        consumeEnergy(BlockProtectorConfiguration.rfForHarvestAttempt);
        return true;
    }

    // Distance is relative with 0 being closes to the explosion and 1 being furthest away.
    public int attemptExplosionProtection(float distance, float radius) {
        if (isDisabled()) return -1;
        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rfneeded = (int) (BlockProtectorConfiguration.rfForExplosionProtection * (1.0 - distance) * radius / 8.0f) + 1;
        rfneeded = (int) (rfneeded * (2.0f - getInfusedFactor()) / 2.0f);

        if (rfneeded > rf) {
            return -1;
        }
        if (rfneeded <= 0) {
            rfneeded = 1;
        }
        consumeEnergy(rfneeded);
        return rfneeded;
    }

    public Set<Coordinate> getProtectedBlocks() {
        return protectedBlocks;
    }

    public Coordinate absoluteToRelative(Coordinate c) {
        return absoluteToRelative(c.getX(), c.getY(), c.getZ());
    }

    public Coordinate absoluteToRelative(int x, int y, int z) {
        return new Coordinate(x - xCoord, y - yCoord, z - zCoord);
    }

    // Test if this relative coordinate is protected.
    public boolean isProtected(Coordinate c) {
        return protectedBlocks.contains(c);
    }

    // Used by the explosion event handler.
    public void removeProtection(Coordinate relative) {
        protectedBlocks.remove(relative);
        markDirty();
        notifyBlockUpdate();
    }

    // Toggle a coordinate to be protected or not. The coordinate given here is absolute.
    public void toggleCoordinate(GlobalCoordinate c) {
        if (c.getDimension() != worldObj.provider.dimensionId) {
            // Wrong dimension. Don't do anything.
            return;
        }
        Coordinate relative = absoluteToRelative(c.getCoordinate());
        if (protectedBlocks.contains(relative)) {
            protectedBlocks.remove(relative);
        } else {
            protectedBlocks.add(relative);
        }
        markDirty();
        notifyBlockUpdate();
    }

    @Override
    public void selectBlock(EntityPlayer player, int x, int y, int z) {
        // This is always called server side.
        if (Math.abs(x-xCoord) > BlockProtectorConfiguration.maxProtectDistance || Math.abs(y-yCoord) > BlockProtectorConfiguration.maxProtectDistance  || Math.abs(z-zCoord) > BlockProtectorConfiguration.maxProtectDistance) {
            RFTools.message(player, EnumChatFormatting.RED + "Block out of range of the block protector!");
            return;
        }
        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(x, y, z), worldObj.provider.dimensionId);
        toggleCoordinate(gc);
    }

    public int getOrCalculateID() {
        if (id == -1) {
            BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);
            GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);
            id = protectors.getNewId(gc);

            protectors.save(worldObj);
            setId(id);
        }
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    /**
     * This method is called after putting down a protector that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);

        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);

        if (id == -1) {
            id = protectors.getNewId(gc);
            markDirty();
        } else {
            protectors.assignId(gc, id);
        }

        protectors.save(worldObj);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        protectedBlocks.readFromNBT(tagCompound, "coordinates");
        powered = tagCompound.getByte("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("protectorId")) {
            id = tagCompound.getInteger("protectorId");
        } else {
            id = -1;
        }
        int m = tagCompound.getByte("rsMode");
        redstoneMode = RedstoneMode.values()[m];
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        protectedBlocks.writeToNBT(tagCompound, "coordinates");
        tagCompound.setByte("powered", (byte) powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("protectorId", id);
        tagCompound.setByte("rsMode", (byte) redstoneMode.ordinal());
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRedstoneMode(RedstoneMode.getMode(m));
            return true;
        }
        return false;
    }

}
