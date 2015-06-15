package mcjty.rftools.dimension.world;

import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.world.types.ControllerType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

public class GenericWorldChunkManager extends WorldChunkManager {
    private DimensionInformation dimensionInformation = null;

    public static DimensionInformation hackyDimensionInformation;       // Hack to get the dimension information here before 'super'.

    public DimensionInformation getDimensionInformation() {
        return dimensionInformation;
    }

    public GenericWorldChunkManager(long seed, WorldType worldType, DimensionInformation dimensionInformation) {
        super(seed, worldType);
        this.dimensionInformation = dimensionInformation;
    }

    @Override
    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        if (dimensionInformation == null) {
            dimensionInformation = hackyDimensionInformation;
        }
        GenLayer[] layer = super.getModdedBiomeGenerators(worldType, seed, original);
        GenLayer rflayer = null;
        ControllerType type;
        DimensionInformation di = dimensionInformation;
        if (di == null) {
            di = hackyDimensionInformation;
        }

        type = di.getControllerType();

        switch (type) {
            case CONTROLLER_DEFAULT:
            case CONTROLLER_SINGLE:
                // Cannot happen
                break;
            case CONTROLLER_CHECKERBOARD:
                rflayer = new GenLayerCheckerboard(this, seed, layer[0]);
                break;
            case CONTROLLER_COLD:
            case CONTROLLER_WARM:
            case CONTROLLER_MEDIUM:
            case CONTROLLER_DRY:
            case CONTROLLER_WET:
            case CONTROLLER_FIELDS:
            case CONTROLLER_MOUNTAINS:
            case CONTROLLER_MAGICAL:
            case CONTROLLER_FOREST:
            case CONTROLLER_FILTERED:
                rflayer = new GenLayerFiltered(this, seed, layer[0], type);
                break;
        }
        GenLayerVoronoiZoom zoomLayer = new GenLayerVoronoiZoom(10L, rflayer);
        zoomLayer.initWorldGenSeed(seed);
        return new GenLayer[] {rflayer, zoomLayer, rflayer};
    }
}
