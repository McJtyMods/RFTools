package mcjty.rftools.dimension.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;

public class RfToolsBiomeMutator extends BiomeGenBase {
    private BiomeGenBase baseBiome;

    public RfToolsBiomeMutator(int index, BiomeGenBase baseBiome) {
        super(index);
        this.baseBiome = baseBiome;
        this.func_150557_a(baseBiome.color, true);
        this.biomeName = baseBiome.biomeName;
        this.topBlock = baseBiome.topBlock;
        this.fillerBlock = baseBiome.fillerBlock;
        this.field_76754_C = baseBiome.field_76754_C;
        this.rootHeight = baseBiome.rootHeight;
        this.heightVariation = baseBiome.heightVariation;
        this.temperature = baseBiome.temperature;
        this.rainfall = baseBiome.rainfall;
        this.waterColorMultiplier = baseBiome.waterColorMultiplier;
        this.enableSnow = baseBiome.func_150559_j();
//        this.enableRain = p_i45381_2_.getIntRainfall();
//        this.spawnableCreatureList = new ArrayList(p_i45381_2_.spawnableCreatureList);
//        this.spawnableMonsterList = new ArrayList(p_i45381_2_.spawnableMonsterList);
//        this.spawnableCaveCreatureList = new ArrayList(p_i45381_2_.spawnableCaveCreatureList);
//        this.spawnableWaterCreatureList = new ArrayList(p_i45381_2_.spawnableWaterCreatureList);
        this.temperature = baseBiome.temperature;
        this.rainfall = baseBiome.rainfall;
        this.rootHeight = baseBiome.rootHeight;
        this.heightVariation = baseBiome.heightVariation;
    }

    @Override
    public BiomeGenBase setDisableRain() {
        return baseBiome.setDisableRain();
    }

    @Override
    public boolean canSpawnLightningBolt() {
        return baseBiome.canSpawnLightningBolt();
    }

    @Override
    public List getSpawnableList(EnumCreatureType creatureType) {
        return baseBiome.getSpawnableList(creatureType);
    }
}
