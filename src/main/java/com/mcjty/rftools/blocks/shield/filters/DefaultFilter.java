package com.mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;

public class DefaultFilter extends AbstractShieldFilter {
    @Override
    public boolean match(Entity entity) {
        return true;
    }

    @Override
    public String getFilterName() {
        return "default";
    }
}
