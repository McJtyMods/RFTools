package mcjty.rftools.blocks.builder;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import static mcjty.rftools.blocks.builder.BuilderSetup.TYPE_SPACE_CHAMBER;

public class SpaceChamberControllerTileEntity extends GenericTileEntity {
    private BlockPos minCorner;
    private BlockPos maxCorner;
    private int channel = -1;

    public SpaceChamberControllerTileEntity() {
        super(TYPE_SPACE_CHAMBER);
    }

    public BlockPos getMinCorner() {
        return minCorner;
    }

    public BlockPos getMaxCorner() {
        return maxCorner;
    }

    public void createChamber(PlayerEntity player) {
        int x1 = getPos().getX();
        int y1 = getPos().getY();
        int z1 = getPos().getZ();
        int x2 = x1;
        int y2 = y1;
        int z2 = z1;
        for (int i = 1 ; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
            if (x2 == x1) {
                if (getWorld().getBlockState(new BlockPos(x1 - i, y1, z1)).getBlock() == BuilderSetup.spaceChamberBlock) {
                    x2 = x1-i;
                } else if (getWorld().getBlockState(new BlockPos(x1 + i, y1, z1)).getBlock() == BuilderSetup.spaceChamberBlock) {
                    x2 = x1+i;
                }
            }
            if (z2 == z1) {
                if (getWorld().getBlockState(new BlockPos(x1, y1, z1 - i)).getBlock() == BuilderSetup.spaceChamberBlock) {
                    z2 = z1-i;
                } else if (getWorld().getBlockState(new BlockPos(x1, y1, z1 + i)).getBlock() == BuilderSetup.spaceChamberBlock) {
                    z2 = z1+i;
                }
            }
        }

        if (x1 == x2 || z2 == z1) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (getWorld().getBlockState(new BlockPos(x2, y1, z2)).getBlock() != BuilderSetup.spaceChamberBlock) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        for (int i = 1 ; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
            if (getWorld().getBlockState(new BlockPos(x1, y1 - i, z1)).getBlock() == BuilderSetup.spaceChamberBlock) {
                y2 = y1-i;
                break;
            }
            if (getWorld().getBlockState(new BlockPos(x1, y1 + i, z1)).getBlock() == BuilderSetup.spaceChamberBlock) {
                y2 = y1+i;
                break;
            }
        }

        if (y1 == y2) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (getWorld().getBlockState(new BlockPos(x2, y2, z2)).getBlock() != BuilderSetup.spaceChamberBlock) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (getWorld().getBlockState(new BlockPos(x1, y2, z2)).getBlock() != BuilderSetup.spaceChamberBlock) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        if (getWorld().getBlockState(new BlockPos(x2, y2, z1)).getBlock() != BuilderSetup.spaceChamberBlock) {
            Logging.message(player, TextFormatting.RED + "Not a valid chamber shape!");
            return;
        }

        // We have a valid shape.
        minCorner = new BlockPos(Math.min(x1, x2)+1, Math.min(y1, y2)+1, Math.min(z1, z2)+1);
        maxCorner = new BlockPos(Math.max(x1, x2)-1, Math.max(y1, y2)-1, Math.max(z1, z2)-1);
        if (minCorner.getX() > maxCorner.getX() || minCorner.getY() > maxCorner.getY() || minCorner.getZ() > maxCorner.getZ()) {
            Logging.message(player, TextFormatting.RED + "Chamber is too small!");
            minCorner = null;
            maxCorner = null;
            return;
        }

        Logging.message(player, TextFormatting.WHITE + "Chamber succesfully created!");

        SpaceChamberRepository chamberRepository = SpaceChamberRepository.get();
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = chamberRepository.getOrCreateChannel(channel);
        chamberChannel.setDimension(getWorld().getDimension().getType());
        chamberChannel.setMinCorner(minCorner);
        chamberChannel.setMaxCorner(maxCorner);
        chamberRepository.save();

        markDirtyClient();
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
        return BlockPosTools.area(minCorner, maxCorner);
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        minCorner = BlockPosTools.read(tagCompound, "minCorner");
        maxCorner = BlockPosTools.read(tagCompound, "maxCorner");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        channel = tagCompound.getInt("channel");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        BlockPosTools.write(tagCompound, "minCorner", minCorner);
        BlockPosTools.write(tagCompound, "maxCorner", maxCorner);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("channel", channel);
    }
}
