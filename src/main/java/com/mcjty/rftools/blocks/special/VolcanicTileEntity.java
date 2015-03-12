package com.mcjty.rftools.blocks.special;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

public class VolcanicTileEntity extends GenericTileEntity {
    private int age = 0;        // Event counter

    @Override
    protected void checkStateServer() {
        if (VolcanicEvents.random.nextFloat() < 0.01f) {
            age++;
            markDirty();
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            if (VolcanicEvents.random.nextInt(400) < (age - 100)) {
                coolDown();
            } else {
                int event = VolcanicEvents.random.nextInt(2048 + age * 100);
                switch (event) {
                    case 0:
                        VolcanicEvents.explosion(worldObj, xCoord, yCoord, zCoord, 2, 1.0f + (meta * 4.0f) / 15.0f);
                        break;
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        VolcanicEvents.randomFire(worldObj, xCoord, yCoord, zCoord, 3 + (meta * 8) / 15);
                        break;
                    case 100:
                        if (meta > 3) {
                            VolcanicEvents.randomLava(worldObj, xCoord, yCoord, zCoord, 1);
                        }
                        break;
                    default:
                        spawnVolcanicBlock();
                        break;
                }
            }
        }
    }

    private void coolDown() {
        worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.cobblestone, 0, 2);
    }

    private void spawnVolcanicBlock() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (meta > 0) {
            int rx = VolcanicEvents.random.nextInt(3)-1;
            int ry = VolcanicEvents.random.nextInt(3)-1;
            int rz = VolcanicEvents.random.nextInt(3)-1;
            if (rx != 0 || ry != 0 || rz != 0) {
                int x = xCoord + rx;
                int y = yCoord + ry;
                int z = zCoord + rz;
                if (y < 1 || y >= worldObj.getHeight()) {
                    return;
                }
                Block block = worldObj.getBlock(x, y, z);

                if (block == null || block.getMaterial() == Material.air) {
                    Block blockBelow = worldObj.getBlock(x, y-1, z);
                    if ((blockBelow == null || blockBelow.getMaterial() == Material.air) && VolcanicEvents.random.nextFloat() > .1f) {
                        // If the block below us is empty there is a high chance we don't spawn a volcanic block.
                        return;
                    }
                    if (ry == -1) {
                        // Down
                        if (rx == 0 && rz == 0) {
                            // meta unchanged. We can go down unhindered.
                        } else {
                            // We go down but not straight. Meta-1
                            meta--;
                        }
                    } else if (ry == 1) {
                        if (rx == 0 && rz == 0) {
                            // Straight up.
                            meta-=2;
                        } else {
                            meta-=3;
                        }
                    } else {
                        if (rx == 0 || rz == 0) {
                            // If we go horizontal we have a small chance of not decreasing meta.
                            if (VolcanicEvents.random.nextFloat() < .2f) {
                                meta++;     // Increase so it gets decreased below again.
                            }
                        }
                        meta--;
                    }
                    if (meta >= 0) {
                        worldObj.setBlock(x, y, z, ModBlocks.volcanicBlock, meta, 2);
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        age = tagCompound.getInteger("age");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("age", age);
    }
}
