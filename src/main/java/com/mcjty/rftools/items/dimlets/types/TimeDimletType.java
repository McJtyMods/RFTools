package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimletconstruction.TimeAbsorberTileEntity;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TimeDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_terrain";

    private static int rarity = DimletRandomizer.RARITY_2;
    private static int baseCreationCost = 300;
    private static int baseMaintainCost = 20;
    private static int baseTickCost = 10;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public String getOpcode() {
        return "t";
    }

    @Override
    public String getTextureName() {
        return "timeDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the time dimlet type");
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
        dimensionInformation.setCelestialAngle(DimletObjectMapping.idToCelestialAngle.get(key));
        dimensionInformation.setTimeSpeed(DimletObjectMapping.idToSpeed.get(key));
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        Float celestialAngle = null;
        Float timeSpeed = null;
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_TIME, dimlets);
        if (dimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomSpecialTimeChance) {
                celestialAngle = null;      // Default
                timeSpeed = null;
            } else {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToCelestialAngle.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                celestialAngle = DimletObjectMapping.idToCelestialAngle.get(key);
                timeSpeed = DimletObjectMapping.idToSpeed.get(key);
            }
        } else {
            DimletKey key = dimlets.get(random.nextInt(dimlets.size())).getKey();
            celestialAngle = DimletObjectMapping.idToCelestialAngle.get(key);
            timeSpeed = DimletObjectMapping.idToSpeed.get(key);
        }
        dimensionInformation.setCelestialAngle(celestialAngle);
        dimensionInformation.setTimeSpeed(timeSpeed);

    }

    @Override
    public String[] getInformation() {
        return new String[] { "Control the flow of time." };
    }

    private static boolean isValidTimeEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = BlockTools.getBlock(stackEssence);

        if (essenceBlock != ModBlocks.timeAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        float angle = essenceCompound.getFloat("angle");
        if (absorbing > 0 || angle < -0.01f) {
            return false;
        }
        return true;
    }

    private static DimletKey findTimeDimlet(ItemStack stackEssence) {
        float angle = stackEssence.getTagCompound().getFloat("angle");
        return TimeAbsorberTileEntity.findBestTimeDimlet(angle);
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidTimeEssence(stackEssence, stackEssence.getTagCompound())) {
            return null;
        }
        DimletKey timeDimlet = findTimeDimlet(stackEssence);
        if (timeDimlet == null) {
            return null;
        }
        if (!DimletCraftingTools.matchDimletRecipe(timeDimlet, stackController, stackMemory, stackEnergy)) {
            return null;
        }
        return timeDimlet;
    }
}
