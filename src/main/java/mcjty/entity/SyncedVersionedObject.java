package mcjty.entity;

import net.minecraft.nbt.NBTTagCompound;

public class SyncedVersionedObject implements SyncedObject {

    protected int serverVersion = 0;
    private int clientVersion = -1;

    public SyncedVersionedObject() {
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        serverVersion = tagCompound.getInteger("version");
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("version", serverVersion);
    }

    @Override
    public void setInvalid() {
        serverVersion = 0;
        clientVersion = -1;
    }

    @Override
    public boolean isClientValueUptodate() {
        return serverVersion != clientVersion;
    }

    @Override
    public void updateClientValue() {
        clientVersion = serverVersion;
    }

}
