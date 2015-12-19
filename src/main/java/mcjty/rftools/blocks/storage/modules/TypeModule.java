package mcjty.rftools.blocks.storage.modules;

import mcjty.rftools.blocks.storage.sorters.ItemSorter;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface TypeModule {
    List<ItemSorter> getSorters();

    String getLongLabel(ItemStack stack);

    String getShortLabel(ItemStack stack);
}
