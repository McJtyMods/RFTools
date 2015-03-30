package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.WeatherDescriptor;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_weather";

    private static int rarity = DimletRandomizer.RARITY_1;
    private static int baseCreationCost = 100;
    private static int baseMaintainCost = 50;
    private static int baseTickCost = 10;

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
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the weather dimlet type");
        rarity = cfg.get(CATEGORY_TYPE, "rarity", rarity, "Default rarity for this dimlet type").getInt();
        baseCreationCost = cfg.get(CATEGORY_TYPE, "creation.cost", baseCreationCost, "Dimlet creation cost (how much power this dimlets adds during creation time of a dimension)").getInt();
        baseMaintainCost = cfg.get(CATEGORY_TYPE, "maintenance.cost", baseMaintainCost, "Dimlet maintenance cost (how much power this dimlet will use up to keep the dimension running)").getInt();
        baseTickCost = cfg.get(CATEGORY_TYPE, "tick.cost", baseTickCost, "Dimlet tick cost (how long it takes to make a dimension with this dimlet in it)").getInt();
    }

    @Override
    public int getRarity() {
        return rarity;
    }

    @Override
    public int getCreationCost() {
        return baseCreationCost;
    }

    @Override
    public int getMaintenanceCost() {
        return baseMaintainCost;
    }

    @Override
    public int getTickCost() {
        return baseTickCost;
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
    public float getModifierCreateCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
    }

    @Override
    public float getModifierMaintainCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
    }

    @Override
    public float getModifierTickCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
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
