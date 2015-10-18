package mcjty.rftools.blocks.shield;

import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.shield.filters.*;
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

import java.util.List;

public class ShieldBlockTileEntity extends TileEntity {

    private Block block;
    private int camoId = -1;
    private int hasTe = 0;
    private int shieldColor;

    private int damageBits = 0;     // A 4-bit value indicating if a specific type of entity should get damage.
    private int collisionData = 0;  // A 4-bit value indicating collision detection data.

    // Damage timer is not saved with the TE as it is not needed.
    private int damageTimer = 10;

    private AxisAlignedBB beamBox = null;

    public void setDamageBits(int damageBits) {
        this.damageBits = damageBits;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getCollisionData() {
        return collisionData;
    }

    public void setCollisionData(int collisionData) {
        this.collisionData = collisionData;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    // Coordinate of the shield block.
    private Coordinate shieldBlock;

    public int getShieldColor() {
        return shieldColor;
    }

    public void setShieldColor(int shieldColor) {
        this.shieldColor = shieldColor;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setCamoBlock(int camoId, int hasTe) {
        this.camoId = camoId;
        this.hasTe = hasTe;
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
            beamBox = AxisAlignedBB.getBoundingBox(xCoord - .4, yCoord - .4, zCoord - .4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);
        }

        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) worldObj.getTileEntity(shieldBlock.getX(), shieldBlock.getY(), shieldBlock.getZ());
            if (shieldTileEntity != null) {
                List<Entity> l = worldObj.getEntitiesWithinAABB(Entity.class, beamBox);
                for (Entity entity : l) {
                    if ((damageBits & AbstractShieldBlock.META_HOSTILE) != 0 && entity instanceof IMob) {
                        if (checkEntityDamage(shieldTileEntity, HostileFilter.HOSTILE)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((damageBits & AbstractShieldBlock.META_PASSIVE) != 0 && entity instanceof IAnimals) {
                        if (checkEntityDamage(shieldTileEntity, AnimalFilter.ANIMAL)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    } else if ((damageBits & AbstractShieldBlock.META_PLAYERS) != 0 && entity instanceof EntityPlayer) {
                        if (checkPlayerDamage(shieldTileEntity, (EntityPlayer) entity)) {
                            shieldTileEntity.applyDamageToEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private boolean checkEntityDamage(ShieldTEBase shieldTileEntity, String filterName) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (filterName.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            }
        }
        return false;
    }

    private boolean checkPlayerDamage(ShieldTEBase shieldTileEntity, EntityPlayer entity) {
        List<ShieldFilter> filters = shieldTileEntity.getFilters();
        for (ShieldFilter filter : filters) {
            if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
            } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                String name = playerFilter.getName();
                if ((name == null || name.isEmpty())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                } else if (name.equals(entity.getDisplayName())) {
                    return ((filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0);
                }
            }
        }
        return false;
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

    public boolean getHasTe() {
        return hasTe != 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("camoId", camoId);
        tagCompound.setInteger("hasTe", hasTe);
        tagCompound.setInteger("damageBits", damageBits);
        tagCompound.setInteger("collisionData", collisionData);
        tagCompound.setInteger("shieldColor", shieldColor);
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
        hasTe = tagCompound.getInteger("hasTe");
        if (camoId == -1) {
            block = null;
        } else {
            block = Block.getBlockById(camoId);
        }
        damageBits = tagCompound.getInteger("damageBits");
        collisionData = tagCompound.getInteger("collisionData");
        shieldColor = tagCompound.getInteger("shieldColor");
        if (shieldColor == 0) {
            shieldColor = 0x96ffc8;
        }

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
