package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.EffectType;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class EffectDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Effect";
    }

    @Override
    public String getOpcode() {
        return "e";
    }

    @Override
    public String getTextureName() {
        return "effectDimlet";
    }

    @Override
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return false;
    }

    @Override
    public boolean isInjectable() {
        return true;
    }

    @Override
    public void inject(DimletKey key, DimensionInformation dimensionInformation) {
        Set<EffectType> effectTypes = dimensionInformation.getEffectTypes();
        effectTypes.add(DimletObjectMapping.idToEffectType.get(key));
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        Set<EffectType> effectTypes = dimensionInformation.getEffectTypes();
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_EFFECT, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomEffectChance) {
                DimletKey key = DimletRandomizer.getRandomEffect(random, false);
                EffectType effectType = DimletObjectMapping.idToEffectType.get(key);
                if (!effectTypes.contains(effectType)) {
                    dimensionInformation.updateCostFactor(key);
                    effectTypes.add(effectType);
                }
            }
        } else {
            for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
                DimletKey key = dimletWithModifier.getLeft();
                EffectType effectType = DimletObjectMapping.idToEffectType.get(key);
                if (effectType != EffectType.EFFECT_NONE) {
                    effectTypes.add(effectType);
                }
            }
        }
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Control various environmental effects", "in the dimension." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
