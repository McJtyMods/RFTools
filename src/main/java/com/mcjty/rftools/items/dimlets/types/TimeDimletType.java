package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimletconstruction.TimeAbsorberTileEntity;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TimeDimletType implements IDimletType {
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
        Block essenceBlock = DimletCraftingTools.getBlock(stackEssence);

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
