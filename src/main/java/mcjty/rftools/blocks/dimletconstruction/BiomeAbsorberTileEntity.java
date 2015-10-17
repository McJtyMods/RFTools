package mcjty.rftools.blocks.dimletconstruction;

import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

public class BiomeAbsorberTileEntity extends GenericTileEntity {

    private int absorbing = 0;
    private int biomeID = -1;


    @Override
    protected void checkStateClient() {
        if (absorbing > 0) {
            Random rand = worldObj.rand;

            double u = rand.nextFloat() * 2.0f - 1.0f;
            double v = (float) (rand.nextFloat() * 2.0f * Math.PI);
            double x = Math.sqrt(1 - u * u) * Math.cos(v);
            double y = Math.sqrt(1 - u * u) * Math.sin(v);
            double z = u;
            double r = 1.0f;

            worldObj.spawnParticle("portal", xCoord + 0.5f + x * r, yCoord + 0.5f + y * r, zCoord + 0.5f + z * r, -x, -y, -z);
        }
    }

    @Override
    protected void checkStateServer() {
        if (absorbing > 0) {
            BiomeGenBase biomeGenBase = worldObj.getBiomeGenForCoords(xCoord, zCoord);
            if (biomeGenBase == null || biomeGenBase.biomeID != biomeID) {
                return;
            }

            absorbing--;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void placeDown() {
        if (biomeID == -1) {
            BiomeGenBase biomeGenBase = worldObj.getBiomeGenForCoords(xCoord, zCoord);
            if (biomeGenBase == null) {
                biomeID = -1;
                absorbing = 0;
            } else if (biomeGenBase.biomeID != biomeID) {
                biomeID = biomeGenBase.biomeID;
                absorbing = DimletConstructionConfiguration.maxBiomeAbsorbtion;
            }
            markDirty();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("absorbing", absorbing);
        tagCompound.setInteger("biome", biomeID);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        absorbing = tagCompound.getInteger("absorbing");
        biomeID = tagCompound.getInteger("biome");
    }


}

