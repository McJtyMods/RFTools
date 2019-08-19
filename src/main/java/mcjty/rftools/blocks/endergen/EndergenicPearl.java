package mcjty.rftools.blocks.endergen;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EndergenicPearl {
    private int ticksLeft;
    private final BlockPos destination;
    private final int age;

    public EndergenicPearl(int ticksLeft, BlockPos destination, int age) {
        this.ticksLeft = ticksLeft;
        this.destination = destination;
        this.age = age;
    }

    public EndergenicPearl(CompoundNBT tagCompound) {
        ticksLeft = tagCompound.getInteger("t");
        destination = BlockPosTools.readFromNBT(tagCompound, "dest");
        age = tagCompound.getInteger("age");
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public int getAge() {
        return age;
    }

    public BlockPos getDestination() {
        return destination;
    }

    // Return true if the pearl has to be removed (it arrived).
    public boolean handleTick(World world) {
        ticksLeft--;
        if (ticksLeft <= 0) {
            // We arrived. Check that the destination is still there.
            TileEntity te = world.getTileEntity(destination);
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
                endergenicTileEntity.receivePearl(age);
            } else {
                Logging.log("Pearl: where did the destination go?");
            }
            return true;
        }
        return false;
    }

    public CompoundNBT getTagCompound() {
        CompoundNBT tagCompound = new CompoundNBT();
        tagCompound.setInteger("t", ticksLeft);
        BlockPosTools.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setInteger("age", age);
        return tagCompound;
    }
}
