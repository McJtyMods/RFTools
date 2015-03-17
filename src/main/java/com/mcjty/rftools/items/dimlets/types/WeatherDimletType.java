package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.WeatherDescriptor;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Weather";
    }

    @Override
    public String getOpcode() {
        return "W";
    }

    @Override
    public String getTextureName() {
        return "weatherDimlet";
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
        WeatherDescriptor.Builder builder = new WeatherDescriptor.Builder();
        builder.combine(dimensionInformation.getWeatherDescriptor());
        WeatherDescriptor newDescriptor = DimletObjectMapping.idToWeatherDescriptor.get(key);
        builder.combine(newDescriptor);
        dimensionInformation.setWeatherDescriptor(builder.build());
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_WEATHER, dimlets);
        WeatherDescriptor.Builder builder = new WeatherDescriptor.Builder();
        if (dimlets.isEmpty()) {
            while (random.nextFloat() > DimletConfiguration.randomWeatherChance) {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToWeatherDescriptor.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                dimensionInformation.updateCostFactor(key);
                builder.combine(DimletObjectMapping.idToWeatherDescriptor.get(key));
            }
        } else {
            for (Pair<DimletKey, List<DimletKey>> dimlet : dimlets) {
                DimletKey key = dimlet.getKey();
                builder.combine(DimletObjectMapping.idToWeatherDescriptor.get(key));
            }
        }
        dimensionInformation.setWeatherDescriptor(builder.build());
    }

    @Override
    public String[] getInformation() {
        return new String[] { "A weather dimlet affects the weather", "in a dimension." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
