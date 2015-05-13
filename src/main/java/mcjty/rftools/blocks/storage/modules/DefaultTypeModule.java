package mcjty.rftools.blocks.storage.modules;

import mcjty.rftools.blocks.storage.sorters.CountItemSorter;
import mcjty.rftools.blocks.storage.sorters.ItemSorter;
import mcjty.rftools.blocks.storage.sorters.NameItemSorter;

import java.util.ArrayList;
import java.util.List;

public class DefaultTypeModule implements TypeModule {
    private List<ItemSorter> sorters = null;

    @Override
    public List<ItemSorter> getSorters() {
        if (sorters == null) {
            sorters = new ArrayList<ItemSorter>();
            sorters.add(new NameItemSorter());
            sorters.add(new CountItemSorter());
        }
        return sorters;
    }
}
