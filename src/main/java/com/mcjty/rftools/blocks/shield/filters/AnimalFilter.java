package com.mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;

public class AnimalFilter extends AbstractShieldFilter {
    @Override
    public boolean match(Entity entity) {
        return entity instanceof IAnimals && !(entity instanceof IMob);
    }

    @Override
    public String getFilterName() {
        return "animal";
    }
}
