package com.mcjty.rftools.blocks.shield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

public class ItemFilter extends AbstractShieldFilter {
    @Override
    public boolean match(Entity entity) {
        return entity instanceof EntityItem;
    }

    @Override
    public String getFilterName() {
        return "item";
    }
}
