package mcjty.rftools.blocks.storage.modules;

import mcjty.rftools.blocks.storage.sorters.ItemSorter;

import java.util.List;

public interface TypeModule {
    List<ItemSorter> getSorters();
}
