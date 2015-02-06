package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.entity.GenericTileEntity;
import net.minecraft.world.WorldServer;

import java.util.Random;

public class BiomeAbsorberTileEntity extends GenericTileEntity {

    @Override
    protected void checkStateServer() {
        Random rand = worldObj.rand;

        double u = rand.nextFloat() * 2.0f - 1.0f;
        double v = (float) (rand.nextFloat() * 2.0f * Math.PI);
        double x = Math.sqrt(1 - u * u) * Math.cos(v);
        double y = Math.sqrt(1 - u * u) * Math.sin(v);
        double z = u;

        ((WorldServer)worldObj).func_147487_a("portal", xCoord + 0.5f + x * 1.0f, yCoord + 0.5f + y * 1.0f, zCoord + 0.5f + z * 1.0f, 2, -x, -y, -z, 0.1f);
    }
}

