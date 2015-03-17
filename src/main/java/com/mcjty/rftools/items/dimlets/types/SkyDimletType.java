package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.SkyDescriptor;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkyDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Sky";
    }

    @Override
    public String getOpcode() {
        return "s";
    }

    @Override
    public String getTextureName() {
        return "skyDimlet";
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
        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        builder.combine(dimensionInformation.getSkyDescriptor());
        SkyDescriptor newDescriptor = DimletObjectMapping.idToSkyDescriptor.get(key);
        if (newDescriptor.specifiesFogColor()) {
            builder.resetFogColor();
        }
        if (newDescriptor.specifiesSkyColor()) {
            builder.resetSkyColor();
        }
        builder.combine(newDescriptor);
        dimensionInformation.setSkyDescriptor(builder.build());
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_SKY, dimlets);
        if (dimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomSpecialSkyChance) {
                // If nothing was specified then there is random chance we get random sky stuff.
                List<DimletKey> skyIds = new ArrayList<DimletKey>(DimletObjectMapping.idToSkyDescriptor.keySet());
                for (int i = 0 ; i < 1+random.nextInt(3) ; i++) {
                    DimletKey key = skyIds.get(random.nextInt(skyIds.size()));
                    List<DimletKey> modifiers = Collections.emptyList();
                    dimlets.add(Pair.of(key, modifiers));
                }
            }

            if (random.nextFloat() < DimletConfiguration.randomSpecialSkyChance) {
                List<DimletKey> bodyKeys = new ArrayList<DimletKey>();
                for (DimletKey key : DimletObjectMapping.idToSkyDescriptor.keySet()) {
                    if (DimletObjectMapping.celestialBodies.contains(key)) {
                        bodyKeys.add(key);
                    }
                }

                for (int i = 0 ; i < random.nextInt(3) ; i++) {
                    DimletKey key = bodyKeys.get(random.nextInt(bodyKeys.size()));
                    List<DimletKey> modifiers = Collections.emptyList();
                    dimlets.add(Pair.of(key, modifiers));
                }
            }
        }

        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getKey();
            builder.combine(DimletObjectMapping.idToSkyDescriptor.get(key));
        }
        dimensionInformation.setSkyDescriptor(builder.build());
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Control various features of the sky", "like sky color, fog color, celestial bodies, ..." };
    }

    @Override
    public ItemStack attemptDimletCrafting(World world, ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
