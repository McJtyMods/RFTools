package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.Random;

public class TimeAbsorberTileEntity extends GenericTileEntity {
    private int absorbing = 0;
    private float angle = -1.0f;
    // For pulse detection.
    private boolean prevIn = false;
    private int registerTimeout = 0;

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

    public int getAbsorbing() {
        return absorbing;
    }

    public float getAngle() {
        return angle;
    }

    public int getRegisterTimeout() {
        return registerTimeout;
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue = BlockTools.getRedstoneSignalIn(meta);
        boolean pulse = newvalue && !prevIn;
        prevIn = newvalue;
        markDirty();

        if (registerTimeout > 0) {
            registerTimeout--;
            return;
        }

        if (pulse) {
            registerTime();
        }
    }

    private void registerTime() {
        if (worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord)) {
            float a = worldObj.getCelestialAngle(1.0f);
            DimletKey bestDimlet = findBestTimeDimlet(a);
            float besta = DimletObjectMapping.idToCelestialAngle.get(bestDimlet);

            if (angle < -0.001f) {
                angle = besta;
                absorbing = DimletConstructionConfiguration.maxTimeAbsorbtion-1;
            } else if (Math.abs(besta-angle) < 0.1f) {
                absorbing--;
                if (absorbing < 0) {
                    absorbing = 0;
                }
                registerTimeout = 3000;
            }
        }
    }

    public static DimletKey findBestTimeDimlet(float a) {
        float bestDiff = 10000.0f;
        DimletKey bestDimlet = null;
        for (Map.Entry<DimletKey, Float> entry : DimletObjectMapping.idToCelestialAngle.entrySet()) {
            Float celangle = entry.getValue();
            if (celangle != null) {
                float diff = Math.abs(a - celangle);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestDimlet = entry.getKey();
                }
                diff = Math.abs((a-1.0f) - celangle);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestDimlet = entry.getKey();
                }
                diff = Math.abs((a+1.0f) - celangle);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestDimlet = entry.getKey();
                }
            }
        }
        return bestDimlet;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("absorbing", absorbing);
        tagCompound.setFloat("angle", angle);
        tagCompound.setInteger("registerTimeout", registerTimeout);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        absorbing = tagCompound.getInteger("absorbing");
        if (tagCompound.hasKey("angle")) {
            angle = tagCompound.getFloat("angle");
        } else {
            angle = -1.0f;
        }
        registerTimeout = tagCompound.getInteger("registerTimeout");
    }

}

