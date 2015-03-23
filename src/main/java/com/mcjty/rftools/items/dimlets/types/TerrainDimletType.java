package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TerrainDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Terrain";
    }

    @Override
    public String getOpcode() {
        return "T";
    }

    @Override
    public String getTextureName() {
        return "terrainDimlet";
    }

    @Override
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return type == DimletType.DIMLET_MATERIAL || type == DimletType.DIMLET_LIQUID;
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
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_TERRAIN, dimlets);
        List<DimletKey> modifiers;
        TerrainType terrainType = TerrainType.TERRAIN_VOID;
        if (dimlets.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            List<DimletKey> idList = new ArrayList<DimletKey>(DimletObjectMapping.idToTerrainType.keySet());
            DimletKey key = idList.get(random.nextInt(idList.size()));
            dimensionInformation.updateCostFactor(key);
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = Collections.emptyList();
        } else {
            int index = random.nextInt(dimlets.size());
            DimletKey key = dimlets.get(index).getLeft();
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = dimlets.get(index).getRight();
        }

        List<BlockMeta> blocks = new ArrayList<BlockMeta>();
        List<Block> fluids = new ArrayList<Block>();
        DimensionInformation.getMaterialAndFluidModifiers(modifiers, blocks, fluids);
        dimensionInformation.setTerrainType(terrainType);

        BlockMeta baseBlockForTerrain;
        if (!blocks.isEmpty()) {
            baseBlockForTerrain = blocks.get(random.nextInt(blocks.size()));
            if (baseBlockForTerrain == null) {
                baseBlockForTerrain = new BlockMeta(Blocks.stone, 0);     // This is the default in case None was specified.
            }
        } else {
            // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
            // Note that in this particular case we disallow randomly selecting 'expensive' blocks like glass.
            if (random.nextFloat() < DimletConfiguration.randomBaseBlockChance) {
                DimletKey key = DimletRandomizer.getRandomMaterialBlock(random, false);
                dimensionInformation.updateCostFactor(key);
                baseBlockForTerrain = DimletObjectMapping.idToBlock.get(key);
            } else {
                baseBlockForTerrain = new BlockMeta(Blocks.stone, 0);
            }
        }
        dimensionInformation.setBaseBlockForTerrain(baseBlockForTerrain);

        Block fluidForTerrain;
        if (!fluids.isEmpty()) {
            fluidForTerrain = fluids.get(random.nextInt(fluids.size()));
            if (fluidForTerrain == null) {
                fluidForTerrain = Blocks.water;         // This is the default.
            }
        } else {
            if (random.nextFloat() < DimletConfiguration.randomOceanLiquidChance) {
                DimletKey key = DimletRandomizer.getRandomFluidBlock(random, false);
                dimensionInformation.updateCostFactor(key);
                fluidForTerrain = DimletObjectMapping.idToFluid.get(key);
            } else {
                fluidForTerrain = Blocks.water;
            }
        }
        dimensionInformation.setFluidForTerrain(fluidForTerrain);
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This affects the type of terrain", "that you will get in a dimension", "This dimlet can receive liquid and material", "modifiers which have to come in front of the terrain." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
