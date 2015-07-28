package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class PatreonDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_patreon";

    private static int rarity = DimletRandomizer.RARITY_2;
    private static int baseCreationCost = 10;
    private static int baseMaintainCost = 0;
    private static int baseTickCost = 1;

    @Override
    public String getName() {
        return "Patreon";
    }

    @Override
    public String getOpcode() {
        return "P";
    }

    @Override
    public String getTextureName() {
        return "patreonDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the patreon dimlet type");
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
        if ("McJty".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_FIREWORKS);
        } else if ("SickHippie".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_SICKMOON);
            dimensionInformation.setPatreonBit(Patreons.PATREON_SICKSUN);
        } else if ("Nissenfeld".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_RABBITMOON);
            dimensionInformation.setPatreonBit(Patreons.PATREON_RABBITSUN);
        } else if ("Lockesly".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_PINKPILLARS);
        } else if ("Puppeteer".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_PUPPETEER);
        } else if ("Rouven".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_LAYEREDMETA);
        } else if ("FireBall".equals(key.getName())) {
            dimensionInformation.setPatreonBit(Patreons.PATREON_COLOREDPRISMS);
        }
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_PATREON, dimlets);
        if (dimlets.isEmpty()) {
            return;
        }

        for (Pair<DimletKey, List<DimletKey>> dimlet : dimlets) {
            inject(dimlet.getKey(), dimensionInformation);
        }
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Patreon dimlets are in honor of a player and add purely cosmetic features to dimensions" };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
