package mcjty.rftools.blocks.storage;

import net.minecraft.util.IStringSerializable;

public enum ModularAmountOverlay implements IStringSerializable {
    AMOUNT_NONE("none"),
    AMOUNT_G0("g0");

    private final String name;

    ModularAmountOverlay(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
