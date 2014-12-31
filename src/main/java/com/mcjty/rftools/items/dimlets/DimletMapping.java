package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.dimension.MobDescriptor;
import com.mcjty.rftools.dimension.SkyDescriptor;
import com.mcjty.rftools.dimension.world.types.*;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.Map;

public class DimletMapping {
    public static final Map<Integer,TerrainType> idToTerrainType = new HashMap<Integer, TerrainType>();
    public static final Map<Integer,SpecialType> idToSpecialType = new HashMap<Integer, SpecialType>();
    public static final Map<Integer,FeatureType> idToFeatureType = new HashMap<Integer, FeatureType>();
    public static final Map<Integer,EffectType> idToEffectType = new HashMap<Integer, EffectType>();
    public static final Map<Integer,StructureType> idToStructureType = new HashMap<Integer, StructureType>();
    public static final Map<Integer,BiomeGenBase> idToBiome = new HashMap<Integer, BiomeGenBase>();
    public static final Map<Integer,String> idToDigit = new HashMap<Integer, String>();
    public static final Map<Integer,Block> idToBlock = new HashMap<Integer, Block>();
    public static final Map<Integer,Block> idToFluid = new HashMap<Integer, Block>();
    public static final Map<Integer,SkyDescriptor> idToSkyDescriptor = new HashMap<Integer, SkyDescriptor>();
    public static final Map<Integer,MobDescriptor> idtoMob = new HashMap<Integer, MobDescriptor>();
    public static final Map<Integer,Float> idToCelestialAngle = new HashMap<Integer, Float>();
    public static final Map<Integer,Float> idToSpeed = new HashMap<Integer, Float>();
}
