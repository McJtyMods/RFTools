package mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

public class ItemFilter extends AbstractShieldFilter {

    public static final String ITEM = "item";

    @Override
    public boolean match(Entity entity) {
        return entity instanceof EntityItem;
    }

    @Override
    public String getFilterName() {
        return ITEM;
    }
}
