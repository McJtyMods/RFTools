package mcjty.rftools.playerprops;

import mcjty.rftools.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PreferencesProperties {

    private Entity entity = null;

    private int buffX = 2;
    private int buffY = 2;

    private boolean dirty = true;

    public PreferencesProperties() {
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void tick() {
        if (dirty) {
            syncToClient();
        }
    }

    private void syncToClient() {
        PacketHandler.INSTANCE.sendTo(new PacketSendPreferencesToClient(buffX, buffY), (EntityPlayerMP) entity);
        dirty = false;
    }

    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("buffX", buffX);
        compound.setInteger("buffY", buffY);
    }

    public void loadNBTData(NBTTagCompound compound) {
        buffX = compound.getInteger("buffX");
        buffY = compound.getInteger("buffY");
        dirty = true;
    }

    public void setBuffXY(int x, int y) {
        this.buffX = x;
        this.buffY = y;
        dirty = true;
    }

    public int getBuffX() {
        return buffX;
    }

    public int getBuffY() {
        return buffY;
    }
}
