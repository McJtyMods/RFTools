package com.mcjty.entity;

public interface SyncedObject {

    void setInvalid();

    boolean isClientValueUptodate();

    void updateClientValue();
}
