package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.items.dimlets.DimletMapping;
import com.mcjty.varia.BlockMeta;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class LiquidAbsorberTileEntity extends GenericTileEntity {
    private static final int ABSORB_SPEED = 5;

    private int absorbing = 0;
    private int blockID = -1;
    private int timer = ABSORB_SPEED;
    private Set<Coordinate> toscan = new HashSet<Coordinate>();

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

    private void checkBlock(Coordinate c, ForgeDirection direction) {
        Coordinate c2 = c.addDirection(direction);
        Block block = blockMatches(c2);
        if (block != null) {
            toscan.add(c2);
        }
    }

    private Block blockMatches(Coordinate c2) {
        Block block = worldObj.getBlock(c2.getX(), c2.getY(), c2.getZ());
        if (block == null || block.getMaterial() == Material.air) {
            return null;
        }
        int id = Block.blockRegistry.getIDForObject(block);
        return id == blockID ? block : null;
    }

    // Server side: play a sound to all nearby players
    private void playSound(String soundName, double x, double y, double z, double volume, double pitch) {
        S29PacketSoundEffect soundEffect = new S29PacketSoundEffect(soundName, x, y, z, (float) volume, (float) pitch);

        for (int j = 0; j < worldObj.playerEntities.size(); ++j) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)worldObj.playerEntities.get(j);
            ChunkCoordinates chunkcoordinates = entityplayermp.getPlayerCoordinates();
            double d7 = x - chunkcoordinates.posX;
            double d8 = y - chunkcoordinates.posY;
            double d9 = z - chunkcoordinates.posZ;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;

            if (d10 <= 256.0D) {
                entityplayermp.playerNetServerHandler.sendPacket(soundEffect);
            }
        }
    }


    @Override
    protected void checkStateServer() {
        if (absorbing > 0) {
            timer--;
            if (timer <= 0) {
                timer = ABSORB_SPEED;
                if (!toscan.isEmpty()) {
                    int r = worldObj.rand.nextInt(toscan.size());
                    Iterator<Coordinate> iterator = toscan.iterator();
                    Coordinate c = null;
                    for (int i = 0 ; i <= r ; i++) {
                        c = iterator.next();
                    }
                    toscan.remove(c);
                    checkBlock(c, ForgeDirection.DOWN);
                    checkBlock(c, ForgeDirection.UP);
                    checkBlock(c, ForgeDirection.EAST);
                    checkBlock(c, ForgeDirection.WEST);
                    checkBlock(c, ForgeDirection.SOUTH);
                    checkBlock(c, ForgeDirection.NORTH);

                    Block block = blockMatches(c);
                    if (block != null) {
                        playSound(block.stepSound.getBreakSound(), xCoord, yCoord, zCoord, 1.0f, 1.0f);
                        worldObj.setBlockToAir(c.getX(), c.getY(), c.getZ());
                        absorbing--;
                        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                    }
                }
            }
            markDirty();
        }
    }

    public void placeDown() {
        Block block = worldObj.getBlock(xCoord, yCoord - 1, zCoord);
        if (block == null || block.isAir(worldObj, xCoord, yCoord-1, zCoord)) {
            blockID = -1;
            absorbing = 0;
            toscan.clear();
            markDirty();
            return;
        }

        int id = Block.blockRegistry.getIDForObject(block);
        if (id != blockID) {
            boolean ok = isValidDimletLiquid(block);

            if (ok) {
                blockID = id;
                absorbing = DimletConstructionConfiguration.maxLiquidAbsorbtion;
                timer = ABSORB_SPEED;
                toscan.clear();
                toscan.add(new Coordinate(xCoord, yCoord-1, zCoord));
                markDirty();
            }
        } else {
            toscan.add(new Coordinate(xCoord, yCoord-1, zCoord));
            markDirty();
        }
    }

    private boolean isValidDimletLiquid(Block block) {
        boolean ok = false;
        for (Map.Entry<Integer, Block> entry : DimletMapping.idToFluid.entrySet()) {
            if (entry.getValue() == block) {
                ok = true;
                break;
            }
        }
        return ok;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        int[] x = new int[toscan.size()];
        int[] y = new int[toscan.size()];
        int[] z = new int[toscan.size()];
        int i = 0;
        for (Coordinate c : toscan) {
            x[i] = c.getX();
            y[i] = c.getY();
            z[i] = c.getZ();
            i++;
        }
        tagCompound.setIntArray("toscanx", x);
        tagCompound.setIntArray("toscany", y);
        tagCompound.setIntArray("toscanz", z);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("absorbing", absorbing);
        tagCompound.setInteger("liquid", blockID);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        int[] x = tagCompound.getIntArray("toscanx");
        int[] y = tagCompound.getIntArray("toscany");
        int[] z = tagCompound.getIntArray("toscanz");
        toscan.clear();
        for (int i = 0 ; i < x.length ; i++) {
            toscan.add(new Coordinate(x[i], y[i], z[i]));
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        absorbing = tagCompound.getInteger("absorbing");
        blockID = tagCompound.getInteger("liquid");
    }


}

