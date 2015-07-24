package mcjty.rftools.blocks.spaceprojector;

import mcjty.entity.GenericTileEntity;
import mcjty.varia.Coordinate;
import mcjty.varia.Logging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class SpaceChamberControllerTileEntity extends GenericTileEntity {
    private Coordinate minCorner;
    private Coordinate maxCorner;
    private int channel = -1;

    public Coordinate getMinCorner() {
        return minCorner;
    }

    public Coordinate getMaxCorner() {
        return maxCorner;
    }

    public void createChamber(EntityPlayer player) {
        int x1 = xCoord;
        int y1 = yCoord;
        int z1 = zCoord;
        int x2 = x1;
        int y2 = y1;
        int z2 = z1;
        for (int i = 1 ; i < SpaceProjectorConfiguration.maxSpaceChamberDimension; i++) {
            if (x2 == x1) {
                if (worldObj.getBlock(x1-i, y1, z1) == SpaceProjectorSetup.spaceChamberBlock) {
                    x2 = x1-i;
                } else if (worldObj.getBlock(x1+i, y1, z1) == SpaceProjectorSetup.spaceChamberBlock) {
                    x2 = x1+i;
                }
            }
            if (z2 == z1) {
                if (worldObj.getBlock(x1, y1, z1-i) == SpaceProjectorSetup.spaceChamberBlock) {
                    z2 = z1-i;
                } else if (worldObj.getBlock(x1, y1, z1+i) == SpaceProjectorSetup.spaceChamberBlock) {
                    z2 = z1+i;
                }
            }
        }

        if (x1 == x2 || z2 == z1) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (worldObj.getBlock(x2, y1, z2) != SpaceProjectorSetup.spaceChamberBlock) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        for (int i = 1 ; i < SpaceProjectorConfiguration.maxSpaceChamberDimension; i++) {
            if (worldObj.getBlock(x1, y1-i, z1) == SpaceProjectorSetup.spaceChamberBlock) {
                y2 = y1-i;
                break;
            }
            if (worldObj.getBlock(x1, y1+i, z1) == SpaceProjectorSetup.spaceChamberBlock) {
                y2 = y1+i;
                break;
            }
        }

        if (y1 == y2) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (worldObj.getBlock(x2, y2, z2) != SpaceProjectorSetup.spaceChamberBlock) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (worldObj.getBlock(x1, y2, z2) != SpaceProjectorSetup.spaceChamberBlock) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (worldObj.getBlock(x2, y2, z1) != SpaceProjectorSetup.spaceChamberBlock) {
            Logging.message(player, EnumChatFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        // We have a valid shape.
        minCorner = new Coordinate(Math.min(x1, x2)+1, Math.min(y1, y2)+1, Math.min(z1, z2)+1);
        maxCorner = new Coordinate(Math.max(x1, x2)-1, Math.max(y1, y2)-1, Math.max(z1, z2)-1);
        if (minCorner.getX() > maxCorner.getX() || minCorner.getY() > maxCorner.getY() || minCorner.getZ() > maxCorner.getZ()) {
            Logging.message(player, EnumChatFormatting.RED + "Chamber is too small!");
            minCorner = null;
            maxCorner = null;
            return;
        }

        Logging.message(player, EnumChatFormatting.WHITE + "Chamber succesfully created!");

        SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(worldObj);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = chamberRepository.getOrCreateChannel(channel);
        chamberChannel.setDimension(worldObj.provider.dimensionId);
        chamberChannel.setMinCorner(minCorner);
        chamberChannel.setMaxCorner(maxCorner);
        chamberRepository.save(worldObj);

        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    public int getChannel() {
        return channel;
    }

    public int getChamberSize() {
        if (channel == -1) {
            return -1;
        }
        if (minCorner == null) {
            return -1;
        }
        return Coordinate.area(minCorner, maxCorner);
    }

    public void setChannel(int channel) {
        this.channel = channel;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        minCorner = Coordinate.readFromNBT(tagCompound, "minCorner");
        maxCorner = Coordinate.readFromNBT(tagCompound, "maxCorner");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Coordinate.writeToNBT(tagCompound, "minCorner", minCorner);
        Coordinate.writeToNBT(tagCompound, "maxCorner", maxCorner);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
