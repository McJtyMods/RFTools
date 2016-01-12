package mcjty.rftools.blocks.storage;

import net.minecraft.util.IStringSerializable;

public enum ModularAmountOverlay implements IStringSerializable {
    AMOUNT_NONE("none"),
    AMOUNT_EMPTY("empty"),
    AMOUNT_G0("g0"),
    AMOUNT_G1("g1"),
    AMOUNT_G2("g2"),
    AMOUNT_G3("g3"),
    AMOUNT_G4("g4"),
    AMOUNT_G5("g5"),
    AMOUNT_G6("g6"),
    AMOUNT_G7("g7"),
    AMOUNT_R0("r0"),
    AMOUNT_R1("r1"),
    AMOUNT_R2("r2"),
    AMOUNT_R3("r3"),
    AMOUNT_R4("r4"),
    AMOUNT_R5("r5"),
    AMOUNT_R6("r6"),
    AMOUNT_R7("r7");

    private final String name;

    ModularAmountOverlay(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
