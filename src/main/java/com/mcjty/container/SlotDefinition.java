package com.mcjty.container;

import net.minecraft.item.ItemStack;

public class SlotDefinition {
    private final SlotType type;
    private final ItemStack itemStack;

    public SlotDefinition(SlotType type) {
        this(type, null);
    }

    public SlotDefinition(SlotType type, ItemStack itemStack) {
        this.type = type;
        this.itemStack = itemStack;
    }

    public SlotType getType() {
        return type;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlotDefinition that = (SlotDefinition) o;

        if (itemStack != null ? !itemStack.equals(that.itemStack) : that.itemStack != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (itemStack != null ? itemStack.hashCode() : 0);
        return result;
    }
}
