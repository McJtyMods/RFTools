package com.mcjty.rftools.dimension;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    public DimensionInformation(String name, DimensionDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public DimensionDescriptor getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }
}
