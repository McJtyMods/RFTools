package com.mcjty.api;

public interface MachineInformation {

    /// Return a list of all tags that this machine supports.
    String[] getSupportedTags();

    /// Get specific information for the given tag.
    String getData(String tag, long millis);
}
