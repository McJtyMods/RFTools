package mcjty.container;

import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class SlotDefinition {
    private final SlotType type;
    private final ItemStack[] itemStacks;
    private final Class itemClass;

    public SlotDefinition(SlotType type, ItemStack... itemStacks) {
        this.type = type;
        this.itemStacks = itemStacks;
        this.itemClass = null;
    }

    public SlotDefinition(SlotType type, Class itemClass) {
        this.type = type;
        this.itemStacks = new ItemStack[0];
        this.itemClass = itemClass;
    }

    public SlotType getType() {
        return type;
    }

    public boolean itemStackMatches(ItemStack stack) {
        for (ItemStack itemStack : itemStacks) {
            if (itemStack.getItem() == stack.getItem()) {
                return true;
            }
        }
        if (itemClass != null && itemClass.isInstance(stack.getItem())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlotDefinition that = (SlotDefinition) o;

        if (!Arrays.equals(itemStacks, that.itemStacks)) {
            return false;
        }
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (itemStacks != null ? Arrays.hashCode(itemStacks) : 0);
        return result;
    }
}
