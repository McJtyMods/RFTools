package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.dimension.description.SkyDescriptor;
import com.mcjty.rftools.dimension.description.WeatherDescriptor;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DimletObjectMapping {
    public static final Map<DimletKey,TerrainType> idToTerrainType = new HashMap<DimletKey, TerrainType>();
    public static final Map<DimletKey,SpecialType> idToSpecialType = new HashMap<DimletKey, SpecialType>();
    public static final Map<DimletKey,FeatureType> idToFeatureType = new HashMap<DimletKey, FeatureType>();
    public static final Map<DimletKey,ControllerType> idToControllerType = new HashMap<DimletKey, ControllerType>();
    public static final Map<DimletKey,EffectType> idToEffectType = new HashMap<DimletKey, EffectType>();
    public static final Map<DimletKey,StructureType> idToStructureType = new HashMap<DimletKey, StructureType>();
    public static final Map<DimletKey,BiomeGenBase> idToBiome = new HashMap<DimletKey, BiomeGenBase>();
    public static final Map<DimletKey,String> idToDigit = new HashMap<DimletKey, String>();
    public static final Map<DimletKey,BlockMeta> idToBlock = new HashMap<DimletKey, BlockMeta>();
    public static final Map<DimletKey,Block> idToFluid = new HashMap<DimletKey, Block>();
    public static final Map<DimletKey,SkyDescriptor> idToSkyDescriptor = new HashMap<DimletKey, SkyDescriptor>();
    public static final Map<DimletKey,WeatherDescriptor> idToWeatherDescriptor = new HashMap<DimletKey, WeatherDescriptor>();
    public static final Map<DimletKey,MobDescriptor> idtoMob = new HashMap<DimletKey, MobDescriptor>();
    public static final Map<DimletKey,Float> idToCelestialAngle = new HashMap<DimletKey, Float>();
    public static final Map<DimletKey,Float> idToSpeed = new HashMap<DimletKey, Float>();

    public static final Set<DimletKey> celestialBodies = new HashSet<DimletKey>();

    public static void clean() {
        idToTerrainType.clear();
        idToSpecialType.clear();
        idToFeatureType.clear();
        idToControllerType.clear();
        idToEffectType.clear();
        idToStructureType.clear();
        idToBiome.clear();
        idToDigit.clear();
        idToBlock.clear();
        idToFluid.clear();
        idToSkyDescriptor.clear();
        idToWeatherDescriptor.clear();
        idtoMob.clear();
        idToCelestialAngle.clear();
        idToSpeed.clear();
        celestialBodies.clear();
    }
}
