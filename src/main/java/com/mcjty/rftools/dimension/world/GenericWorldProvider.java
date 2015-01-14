package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.*;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;

public class GenericWorldProvider extends WorldProvider {
    private DimensionInformation dimensionInformation;
    private DimensionStorage storage;

    private long calculateSeed(long seed, int dim) {
        return dim * 13L + seed;
    }

    private DimensionInformation getDimensionInformation() {
        if (dimensionInformation == null) {
            int dim = worldObj.provider.dimensionId;
            dimensionInformation = RfToolsDimensionManager.getDimensionManager(worldObj).getDimensionInformation(dim);
            if (dimensionInformation == null) {
                RFTools.log("Dimension information for dimension " + dim + " is missing!");
            }
        }
        return dimensionInformation;
    }

    @Override
    public void registerWorldChunkManager() {
        int dim = worldObj.provider.dimensionId;
        long seed = calculateSeed(worldObj.getSeed(), dim);

        getDimensionInformation();
        storage = DimensionStorage.getDimensionStorage(worldObj);

        if (dimensionInformation != null && !dimensionInformation.getBiomes().isEmpty()) {
            worldChunkMgr = new SingleBiomeWorldChunkManager(worldObj, seed, terrainType);
        } else {
            worldChunkMgr = new WorldChunkManager(seed, worldObj.getWorldInfo().getTerrainType());
        }
        hasNoSky = false;
    }

    public static WorldProvider getProviderForDimension(int id) {
        return DimensionManager.createProviderFor(id);
    }

    @Override
    public double getHorizon() {
        getDimensionInformation();
        if (dimensionInformation.getTerrainType().hasNoHorizon()) {
            return 0;
        } else {
            return super.getHorizon();
        }
    }

    @Override
    public String getDimensionName() {
        return "rftools dimension";
    }

    @Override
    public String getWelcomeMessage() {
        return "Entering the rftools dimension!";
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public int getRespawnDimension(EntityPlayerMP player) {
        return DimletConfiguration.spawnDimension;
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        int dim = worldObj.provider.dimensionId;
        long seed = calculateSeed(worldObj.getSeed(), dim);
        return new GenericChunkProvider(worldObj, seed);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return super.getBiomeGenForCoords(x, z);
    }

    private static long lastFogTime = 0;

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getFogColor(float angle, float p_76562_2_) {
        int dim = worldObj.provider.dimensionId;
        if (System.currentTimeMillis() - lastTime > 1000) {
            lastTime = System.currentTimeMillis();
            PacketHandler.INSTANCE.sendToServer(new PacketGetDimensionEnergy(dim));
        }

        float factor = calculatePowerBlackout(dim);
        getDimensionInformation();

        float r;
        float g;
        float b;
        if (dimensionInformation == null) {
            r = g = b = 1.0f;
        } else {
            r = dimensionInformation.getSkyDescriptor().getFogColorFactorR() * factor;
            g = dimensionInformation.getSkyDescriptor().getFogColorFactorG() * factor;
            b = dimensionInformation.getSkyDescriptor().getFogColorFactorB() * factor;
        }

        Vec3 color = super.getFogColor(angle, p_76562_2_);
        return Vec3.createVectorHelper(color.xCoord * r, color.yCoord * g, color.zCoord * b);
    }

    private static long lastTime = 0;

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
        int dim = worldObj.provider.dimensionId;
        if (System.currentTimeMillis() - lastTime > 1000) {
            lastTime = System.currentTimeMillis();
            PacketHandler.INSTANCE.sendToServer(new PacketGetDimensionEnergy(dim));
        }

        float factor = calculatePowerBlackout(dim);
        getDimensionInformation();

        float r;
        float g;
        float b;
        if (dimensionInformation == null) {
            r = g = b = 1.0f;
        } else {
            r = dimensionInformation.getSkyDescriptor().getSkyColorFactorR() * factor;
            g = dimensionInformation.getSkyDescriptor().getSkyColorFactorG() * factor;
            b = dimensionInformation.getSkyDescriptor().getSkyColorFactorB() * factor;
        }

        Vec3 skyColor = super.getSkyColor(cameraEntity, partialTicks);
        return Vec3.createVectorHelper(skyColor.xCoord * r, skyColor.yCoord * g, skyColor.zCoord * b);
    }

    private float calculatePowerBlackout(int dim) {
        float factor = 1.0f;
        int power = storage.getEnergyLevel(dim);
        if (power < DimletConfiguration.DIMPOWER_WARN3) {
            factor = ((float) power) / DimletConfiguration.DIMPOWER_WARN3 * 0.2f;
        } else  if (power < DimletConfiguration.DIMPOWER_WARN2) {
            factor = (float) (power - DimletConfiguration.DIMPOWER_WARN3) / (DimletConfiguration.DIMPOWER_WARN2 - DimletConfiguration.DIMPOWER_WARN3) * 0.3f + 0.2f;
        } else if (power < DimletConfiguration.DIMPOWER_WARN1) {
            factor = (float) (power - DimletConfiguration.DIMPOWER_WARN2) / (DimletConfiguration.DIMPOWER_WARN1 - DimletConfiguration.DIMPOWER_WARN2) * 0.3f + 0.5f;
        } else if (power < DimletConfiguration.DIMPOWER_WARN0) {
            factor = (float) (power - DimletConfiguration.DIMPOWER_WARN1) / (DimletConfiguration.DIMPOWER_WARN0 - DimletConfiguration.DIMPOWER_WARN1) * 0.2f + 0.8f;
        }
        return factor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1) {
        getDimensionInformation();
        if (dimensionInformation == null) {
            return super.getSunBrightness(par1);
        }
        int dim = worldObj.provider.dimensionId;
        float factor = calculatePowerBlackout(dim);
        return super.getSunBrightness(par1) * dimensionInformation.getSkyDescriptor().getSunBrightnessFactor() * factor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1) {
        getDimensionInformation();
        if (dimensionInformation == null) {
            return super.getStarBrightness(par1);
        }
        return super.getStarBrightness(par1) * dimensionInformation.getSkyDescriptor().getStarBrightnessFactor();
    }

    @Override
    public float calculateCelestialAngle(long time, float p_76563_3_) {
        getDimensionInformation();
        if (dimensionInformation == null) {
            return super.calculateCelestialAngle(time, p_76563_3_);
        }

        if (dimensionInformation.getCelestialAngle() == null) {
            if (dimensionInformation.getTimeSpeed() == null) {
                return super.calculateCelestialAngle(time, p_76563_3_);
            } else {
                return super.calculateCelestialAngle((long) (time * dimensionInformation.getTimeSpeed()), p_76563_3_);
            }
        } else {
            return dimensionInformation.getCelestialAngle();
        }
    }
}
