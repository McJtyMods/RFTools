package mcjty.rftools.blocks.special;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class VolcanicEvents {
    public static final Random random = new Random();

    public static void randomFire(World worldObj, int xCoord, int yCoord, int zCoord, int originradius) {
        int rx = random.nextInt(originradius+originradius+1)-originradius;
        int ry = random.nextInt(originradius+originradius+1)-originradius;
        int rz = random.nextInt(originradius+originradius+1)-originradius;
        if (rx != 0 || ry != 0 || rz != 0) {
            int x = xCoord + rx;
            int y = yCoord + ry;
            int z = zCoord + rz;
            if (y < 1 || y >= worldObj.getHeight()-1) {
                return;
            }
            Block block = worldObj.getBlock(x, y, z);
            Block blockBelow = worldObj.getBlock(x, y - 1, z);
            if ((block == null || block.getMaterial() == Material.air) && blockBelow.func_149730_j()) {
                worldObj.setBlock(x, y, z, Blocks.fire);
            }
        }
    }

    public static void randomLava(World worldObj, int xCoord, int yCoord, int zCoord, int originradius) {
        int rx = random.nextInt(originradius+originradius+1)-originradius;
        int ry = random.nextInt(originradius+originradius+1)-originradius;
        int rz = random.nextInt(originradius+originradius+1)-originradius;
        if (rx != 0 || ry != 0 || rz != 0) {
            int x = xCoord + rx;
            int y = yCoord + ry;
            int z = zCoord + rz;
            if (y < 1 || y >= worldObj.getHeight()-1) {
                return;
            }
            Block block = worldObj.getBlock(x, y, z);
            if (block == null || block.getMaterial() == Material.air) {
                worldObj.setBlock(x, y, z, Blocks.flowing_lava, 0, 3);
                worldObj.markBlockForUpdate(x, y, z);
            }
        }
    }

    public static void spawnVolcanicBlocks(World worldObj, int xCoord, int yCoord, int zCoord, int originradius) {
        int rx = random.nextInt(originradius+originradius+1)-originradius;
        int rz = random.nextInt(originradius+originradius+1)-originradius;

        int x = rx + xCoord;
        int z = rz + zCoord;
        int y = worldObj.getTopSolidOrLiquidBlock(x, z);
        if (y > yCoord - 5 && y < yCoord + 9) {
            if (y <= 0 || y >= worldObj.getHeight()-1) {
                return;
            }
            worldObj.setBlock(x, y, z, ModBlocks.volcanicBlock, 8, 3);
            if (random.nextFloat() < .7) {
                worldObj.setBlock(x-1, y, z, ModBlocks.volcanicBlock, 8, 3);
            }
            if (random.nextFloat() < .7) {
                worldObj.setBlock(x+1, y, z, ModBlocks.volcanicBlock, 8, 3);
            }
            if (random.nextFloat() < .7) {
                worldObj.setBlock(x, y, z-1, ModBlocks.volcanicBlock, 8, 3);
            }
            if (random.nextFloat() < .7) {
                worldObj.setBlock(x, y, z+1, ModBlocks.volcanicBlock, 8, 3);
            }
            if (random.nextFloat() < .7) {
                worldObj.setBlock(x, y+1, z, ModBlocks.volcanicBlock, 8, 3);
            }
        }

    }

    public static void explosion(World worldObj, int xCoord, int yCoord, int zCoord, int originradius, float radius) {
        int rx = random.nextInt(originradius+originradius+1)-originradius;
        int ry = random.nextInt(originradius+originradius+1)-originradius;
        int rz = random.nextInt(originradius+originradius+1)-originradius;
        if (rx != 0 || ry != 0 || rz != 0) {
            int x = xCoord + rx;
            int y = yCoord + ry;
            int z = zCoord + rz;
            if (y < 0 || y >= worldObj.getHeight()) {
                return;
            }
            worldObj.setBlockToAir(x, y, z);
            worldObj.newExplosion(null, (x + 0.5F), (y + 0.5F), (z + 0.5F), radius, true, true);
        }
    }


}
