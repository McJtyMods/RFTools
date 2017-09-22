package mcjty.rftools.items.modifier;

import net.minecraft.item.ItemStack;

public class ModifierEntry {
    private final ItemStack in;
    private final ItemStack out;
    private final ModifierFilterType type;
    private final ModifierFilterOperation op;

    public ModifierEntry(ItemStack in, ItemStack out, ModifierFilterType type, ModifierFilterOperation op) {
        this.in = in;
        this.out = out;
        this.type = type;
        this.op = op;
    }

    public ItemStack getIn() {
        return in;
    }

    public ItemStack getOut() {
        return out;
    }

    public ModifierFilterType getType() {
        return type;
    }

    public ModifierFilterOperation getOp() {
        return op;
    }
}
