package com.mcjty.rftools.blocks.special;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class VolcanicCoreTileEntity extends GenericTileEntity {

    @Override
    protected void checkStateServer() {
        if (VolcanicEvents.random.nextFloat() < 0.01f) {
            switch (VolcanicEvents.random.nextInt(16)) {
                case 0:
                case 1:
                case 2:
                    VolcanicEvents.explosion(worldObj, xCoord, yCoord, zCoord, 5, 5.0f);
                    break;
                case 10:
                case 11:
                case 12:
                    VolcanicEvents.randomFire(worldObj, xCoord, yCoord, zCoord, 12);
                    break;
                case 14:
                case 15:
                    VolcanicEvents.randomLava(worldObj, xCoord, yCoord, zCoord, 1);
                    break;
                default:
                    spawnVolcanicBlock();
                    break;
            }
        }
    }

    private void spawnVolcanicBlock() {
        int rx = VolcanicEvents.random.nextInt(3)-1;
        int ry = VolcanicEvents.random.nextInt(3)-1;
        int rz = VolcanicEvents.random.nextInt(3)-1;
        if (rx != 0 || ry != 0 || rz != 0) {
            int x = xCoord + rx;
            int y = yCoord + ry;
            int z = zCoord + rz;
            if (y < 0 || y >= worldObj.getHeight()) {
                return;
            }
            Block block = worldObj.getBlock(x, y, z);
            if (block == null || block.getMaterial() == Material.air) {
                worldObj.setBlock(x, y, z, ModBlocks.volcanicBlock, 15, 2);
            }
        }
    }
}
