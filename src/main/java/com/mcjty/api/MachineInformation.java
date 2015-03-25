package com.mcjty.api;

public interface MachineInformation {

    /// Get the amount of tags that this machine supports.
    int getTagCount();

    /// Get the name of a specific tag.
    String getTagName(int index);

    /// Get the description for a specific tag.
    String getTagDescription(int index);

    /// Get specific information for the given tag.
    String getData(int index, long millis);
}
