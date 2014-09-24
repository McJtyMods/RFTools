package com.mcjty.entity;

/**
 * This is used by GenericTileEntity to represent a value that needs to be synchronized between
 * the client and server version of the tile entity. Possibly sending block updates to the
 * world if needed.
 * @param <T>
 */
public class SyncedValue<T> implements SyncedObject {
    private T value = null;
    private T clientValue = null;

    public SyncedValue() {
    }

    public SyncedValue(T value) {
        this.value = value;
    }

    @Override
    public void setInvalid() {
        value = null;
        clientValue = null;
    }

    @Override
    public boolean isClientValueUptodate() {
        if (value == null && clientValue == null) {
            return false;
        }
        return value != null && value.equals(clientValue);
    }

    @Override
    public void updateClientValue() {
        clientValue = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
