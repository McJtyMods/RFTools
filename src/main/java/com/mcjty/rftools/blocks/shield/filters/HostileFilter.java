package com.mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;

public class HostileFilter extends AbstractShieldFilter {

    public static final String HOSTILE = "hostile";

    @Override
    public boolean match(Entity entity) {
        return entity instanceof IMob;
    }

    @Override
    public String getFilterName() {
        return HOSTILE;
    }
}
