package com.mcjty.entity;

/**
 * This is used by GenericTileEntity to represent a value that needs to be synchronized between
 * the client and server version of the tile entity. Possibly sending block updates to the
 * world if needed.
 * @param <T>
 */
public class SyncedValue<T> {
    private T value = null;
    private T clientValue = null;

    public SyncedValue() {
    }

    public SyncedValue(T value) {
        this.value = value;
    }

    public void setInvalid() {
        value = null;
        clientValue = null;
    }

    public boolean isClientValueUptodate() {
        if (value == null && clientValue == null) {
            return false;
        }
        return value != null && value.equals(clientValue);
    }

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
