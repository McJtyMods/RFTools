package mcjty.rftools.blocks.shield.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;

public class AnimalFilter extends AbstractShieldFilter {

    public static final String ANIMAL = "animal";

    @Override
    public boolean match(Entity entity) {
        return entity instanceof AnimalEntity && !(entity instanceof IMob);
    }

    @Override
    public String getFilterName() {
        return ANIMAL;
    }
}
