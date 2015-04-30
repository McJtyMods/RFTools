package mcjty.rftools.items.smartwrench;

import net.minecraft.item.ItemStack;

public interface SmartWrench {
    SmartWrenchMode getMode(ItemStack itemStack);
}
