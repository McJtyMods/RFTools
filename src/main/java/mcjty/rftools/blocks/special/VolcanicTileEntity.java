package mcjty.rftools.blocks.special;

import mcjty.entity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class VolcanicTileEntity extends GenericTileEntity {
    private int age = 0;        // Event counter

    private AxisAlignedBB beamBox = null;
    private int ticker = 20;

    private void igniteNearEntities() {
        if (beamBox == null) {
            beamBox = AxisAlignedBB.getBoundingBox(xCoord-.5, yCoord - .5, zCoord - .5, xCoord + 1.5, yCoord + 2.5, zCoord + 1.5);
        }

        List<Entity> l = worldObj.getEntitiesWithinAABB(Entity.class, beamBox);
        for (Entity entity : l) {
            if (!entity.isImmuneToFire()) {
                entity.attackEntityFrom(DamageSource.inFire, 6.0f);
            }

            boolean wet = entity.isWet();
            if (!wet) {
                entity.setFire(6);
            }
        }
    }

    private int checkSurroundings() {
        int cntHot = 0;
        for (ForgeDirection direction : ForgeDirection.values()) {
            if (direction != ForgeDirection.UNKNOWN) {
                int ox = xCoord + direction.offsetX;
                int oy = yCoord + direction.offsetY;
                int oz = zCoord + direction.offsetZ;
                Block block = worldObj.getBlock(ox, oy, oz);
                if (block == Blocks.lava || block == SpecialSetup.volcanicBlock || block == SpecialSetup.volcanicCoreBlock || block == Blocks.flowing_lava || block == Blocks.fire) {
                    cntHot++;
                } else if (block == Blocks.water) {
                    worldObj.setBlock(ox, oy, oz, Blocks.cobblestone, 0, 2);
                }
            }
        }
        return cntHot;
    }

    @Override
    protected void checkStateServer() {
        ticker--;
        if (ticker < 0) {
            ticker = 20;
            igniteNearEntities();
            int cntHot = checkSurroundings();
            if (cntHot <= 3) {
                if (VolcanicEvents.random.nextInt(120) < 2) {
                    coolDown();
                    return;
                }
            }
        }

        if (VolcanicEvents.random.nextFloat() < 0.01f) {
            age++;
            markDirty();

            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            if (VolcanicEvents.random.nextInt(150) < (age - 26)) {
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
                    case 24:
                    case 25:
                        VolcanicEvents.randomFire(worldObj, xCoord, yCoord, zCoord, 3 + (meta * 8) / 15);
                        break;
                    case 100:
                    case 101:
                    case 102:
                    case 103:
                    case 104:
                    case 105:
                    case 106:
                    case 107:
                    case 108:
                    case 109:
                    case 110:
                        VolcanicEvents.randomLava(worldObj, xCoord, yCoord, zCoord, 1);
                        break;
                    case 111:
                    case 112:
                    case 113:
                    case 114:
                    case 115:
                    case 116:
                    case 117:
                    case 118:
                    case 119:
                    case 120:
                    case 121:
                    case 122:
                    case 123:
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
            int dir = VolcanicEvents.random.nextInt(6);
            ForgeDirection direction = ForgeDirection.values()[dir];

            int x = xCoord + direction.offsetX;
            int y = yCoord + direction.offsetY;
            int z = zCoord + direction.offsetZ;
            if (y < 1 || y >= worldObj.getHeight()) {
                return;
            }
            Block block = worldObj.getBlock(x, y, z);

            if (block == null || block.getMaterial() == Material.air) {
                float chanceToSpawn = (direction == ForgeDirection.DOWN) ? .5f : .1f;
                Block blockBelow = worldObj.getBlock(x, y-1, z);
                if ((blockBelow == null || blockBelow.getMaterial() == Material.air) && VolcanicEvents.random.nextFloat() > chanceToSpawn) {
                    // If the block below us is empty there is a high chance we don't spawn a volcanic block.
                    return;
                }
                switch (direction) {
                    case DOWN:
                        break;
                    case UP:
                        meta -= 2;
                        break;
                    case NORTH:
                    case SOUTH:
                    case EAST:
                    case WEST:
                        // If we go horizontal we have a small chance of not decreasing meta.
                        if (VolcanicEvents.random.nextFloat() > .2f) {
                            meta--;
                        }
                        break;
                    case UNKNOWN:
                        break;
                }

                if (meta >= 0) {
                    worldObj.setBlock(x, y, z, SpecialSetup.volcanicBlock, meta, 2);
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
