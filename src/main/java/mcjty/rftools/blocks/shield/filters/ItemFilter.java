package mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;

public class ItemFilter extends AbstractShieldFilter {

    public static final String ITEM = "item";

    @Override
    public boolean match(Entity entity) {
        return entity instanceof ItemEntity;
    }

    @Override
    public String getFilterName() {
        return ITEM;
    }
}
