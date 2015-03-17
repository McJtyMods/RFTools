package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.ControllerType;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BiomeDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Biome";
    }

    @Override
    public String getOpcode() {
        return "B";
    }

    @Override
    public String getTextureName() {
        return "biomeDimlet";
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
        return false;
    }

    @Override
    public void inject(DimletKey key, DimensionInformation dimensionInformation) {
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        Set<DimletKey> biomeKeys = new HashSet<DimletKey>();
        List<Pair<DimletKey, List<DimletKey>>> biomeDimlets = DimensionInformation.extractType(DimletType.DIMLET_BIOME, dimlets);
        List<Pair<DimletKey, List<DimletKey>>> controllerDimlets = DimensionInformation.extractType(DimletType.DIMLET_CONTROLLER, dimlets);

        ControllerType controllerType;

        // First determine the controller to use.
        if (controllerDimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomControllerChance) {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToControllerType.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                controllerType = DimletObjectMapping.idToControllerType.get(key);
            } else {
                if (biomeDimlets.isEmpty()) {
                    controllerType = ControllerType.CONTROLLER_DEFAULT;
                } else if (biomeDimlets.size() > 1) {
                    controllerType = ControllerType.CONTROLLER_FILTERED;
                } else {
                    controllerType = ControllerType.CONTROLLER_SINGLE;
                }
            }
        } else {
            DimletKey key = controllerDimlets.get(random.nextInt(controllerDimlets.size())).getLeft();
            controllerType = DimletObjectMapping.idToControllerType.get(key);
        }

        // Now see if we have to add or randomize biomes.
        for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : biomeDimlets) {
            DimletKey key = dimletWithModifiers.getKey();
            biomeKeys.add(key);
        }

        int neededBiomes = controllerType.getNeededBiomes();
        if (neededBiomes == -1) {
            // Can work with any number of biomes.
            if (biomeKeys.size() >= 2) {
                neededBiomes = biomeKeys.size();     // We already have enough biomes
            } else {
                neededBiomes = random.nextInt(10) + 3;
            }
        }

        while (biomeKeys.size() < neededBiomes) {
            DimletKey key;
            List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToBiome.keySet());
            key = keys.get(random.nextInt(keys.size()));
            while (biomeKeys.contains(key)) {
                key = keys.get(random.nextInt(keys.size()));
            }
            biomeKeys.add(key);
        }

        List<BiomeGenBase> biomes = dimensionInformation.getBiomes();
        biomes.clear();
        for (DimletKey key : biomeKeys) {
            biomes.add(DimletObjectMapping.idToBiome.get(key));
        }
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This dimlet controls the biomes that can generate in a dimension", "The controller specifies how they can be used." };
    }

    private static boolean isValidBiomeEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = DimletCraftingTools.getBlock(stackEssence);

        if (essenceBlock != ModBlocks.biomeAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        int biome = essenceCompound.getInteger("biome");
        if (absorbing > 0 || biome == -1) {
            return false;
        }
        return true;
    }

    private static DimletKey findBiomeDimlet(NBTTagCompound essenceCompound) {
        int biomeID = essenceCompound.getInteger("biome");
        for (Map.Entry<DimletKey, BiomeGenBase> entry : DimletObjectMapping.idToBiome.entrySet()) {
            if (entry.getValue().biomeID == biomeID) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidBiomeEssence(stackEssence, stackEssence.getTagCompound())) {
            return null;
        }
        DimletKey biomeDimlet = findBiomeDimlet(stackEssence.getTagCompound());
        if (biomeDimlet == null) {
            return null;
        }
        if (!DimletCraftingTools.matchDimletRecipe(biomeDimlet, stackController, stackMemory, stackEnergy)) {
            return null;
        }
        return biomeDimlet;
    }
}
