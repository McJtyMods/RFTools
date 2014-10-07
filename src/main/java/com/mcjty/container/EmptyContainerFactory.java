package com.mcjty.container;

/**
 * Use this in case you want a container with no slots (for example, for energy storage only).
 */
public class EmptyContainerFactory extends ContainerFactory {

    private static EmptyContainerFactory instance = null;

    public static synchronized EmptyContainerFactory getInstance() {
        if (instance == null) {
            instance = new EmptyContainerFactory();
        }
        return instance;
    }

}
