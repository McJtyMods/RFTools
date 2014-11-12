package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.blocks.shield.filters.PlayerFilter;
import com.mcjty.rftools.blocks.shield.filters.ShieldFilter;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class ShieldBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;
    private int meta = 0;

    private int damageBits = 0;     // A 4-bit value indicating if a specific type of entity should get damage.

    // Damage timer is not saved with the TE as it is not needed.
    private int damageTimer = 10;

    private AxisAlignedBB beamBox = null;

    public int getDamageBits() {
        return damageBits;
    }

    public void setDamageBits(int damageBits) {
        this.damageBits = damageBits;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    // Coordinate of the shield block.
    private Coordinate shieldBlock;

    public int getCamoId() {
        return camoId;
    }

    public int getMeta() {
        return meta;
    }

    public void setCamoBlock(int camoId, int meta) {
        this.camoId = camoId;
        this.meta = meta;
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote) {
            if (damageBits != 0) {
                handleDamage();
            }
        }
    }

    private void handleDamage() {
        damageTimer--;
        if (damageTimer > 0) {
            return;
        }
        damageTimer = 10;
        if (beamBox == null) {
            beamBox = AxisAlignedBB.getBoundingBox(xCoord - .2, yCoord - .2, zCoord - .2, xCoord + 1.2, yCoord + 1.2, zCoord + 1.2);
        }

        if (shieldBlock != null) {
            ShieldTileEntity shieldTileEntity = (ShieldTileEntity) worldObj.getTileEntity(shieldBlock.getX(), shieldBlock.getY(), shieldBlock.getZ());
            if (shieldTileEntity != null) {
                List<Entity> l = worldObj.getEntitiesWithinAABB(Entity.class, beamBox);
                for (Entity entity : l) {
                    if ((damageBits & AbstractShieldBlock.META_HOSTILE) != 0 && entity instanceof IMob) {
                        shieldTileEntity.applyDamageToEntity(entity);
                    } else if ((damageBits & AbstractShieldBlock.META_PASSIVE) != 0 && entity instanceof IAnimals) {
                        shieldTileEntity.applyDamageToEntity(entity);
                    } else if ((damageBits & AbstractShieldBlock.META_PLAYERS) != 0 && entity instanceof EntityPlayer) {
                        if (checkPlayerDamage(shieldTileEntity, (EntityPlayer) entity)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private boolean checkPlayerDamage(ShieldTileEntity shieldTileEntity, EntityPlayer entity) {
        boolean damage = false;
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0) {
                if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                    PlayerFilter playerFilter = (PlayerFilter) filter;
                    String name = playerFilter.getName();
                    if ((name == null || name.isEmpty())) {
                        damage = true;
                        break;
                    } else if (name.equals(entity.getDisplayName())) {
                        damage = true;
                        break;
                    }
                }
            }
        }
        return damage;
    }



    public void setShieldBlock(Coordinate c) {
        shieldBlock = c;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public Coordinate getShieldBlock() {
        return shieldBlock;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        tagCompound.setInteger("camoMeta", meta);
        tagCompound.setInteger("damageBits", damageBits);
        if (shieldBlock != null) {
            tagCompound.setInteger("shieldX", shieldBlock.getX());
            tagCompound.setInteger("shieldY", shieldBlock.getY());
            tagCompound.setInteger("shieldZ", shieldBlock.getZ());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        camoId = tagCompound.getInteger("camoId");
        meta = tagCompound.getInteger("camoMeta");
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        damageBits = tagCompound.getInteger("damageBits");

        int sx = tagCompound.getInteger("shieldX");
        int sy = tagCompound.getInteger("shieldY");
        int sz = tagCompound.getInteger("shieldZ");
        shieldBlock = new Coordinate(sx, sy, sz);

        if (worldObj != null && worldObj.isRemote) {
            // For some reason this is needed to force rendering on the client when apply is pressed.
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
