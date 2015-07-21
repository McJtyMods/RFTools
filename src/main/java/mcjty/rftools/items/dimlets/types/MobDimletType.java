package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.description.MobDescriptor;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletObjectMapping;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class MobDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_mob";

    private static int rarity = DimletRandomizer.RARITY_2;
    private static int baseCreationCost = 300;
    private static int baseMaintainCost = 100;
    private static int baseTickCost = 200;

    @Override
    public String getName() {
        return "Mob";
    }

    @Override
    public String getOpcode() {
        return "M";
    }

    @Override
    public String getTextureName() {
        return "mobsDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the mob dimlet type");
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
        MobDescriptor mobDescriptor = DimletObjectMapping.idtoMob.get(key);
        if (mobDescriptor != null && mobDescriptor.getEntityClass() != null) {
            dimensionInformation.getExtraMobs().add(mobDescriptor);
        } else {
            dimensionInformation.getExtraMobs().clear();
        }
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        List<MobDescriptor> extraMobs = dimensionInformation.getExtraMobs();
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_MOBS, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomExtraMobsChance) {
                DimletKey key = DimletRandomizer.getRandomMob(random, false);
                dimensionInformation.updateCostFactor(key);
                extraMobs.add(DimletObjectMapping.idtoMob.get(key));
            }
        } else {
            DimletKey key = dimlets.get(0).getLeft();
            MobDescriptor mobDescriptor = DimletObjectMapping.idtoMob.get(key);
            if (dimlets.size() == 1 && (mobDescriptor == null || mobDescriptor.getEntityClass() == null)) {
                // Just default.
            } else {
                for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : dimlets) {
                    DimletKey modifierKey = dimletWithModifiers.getLeft();
                    MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(modifierKey);
                    if (descriptor != null && descriptor.getEntityClass() != null) {
                        extraMobs.add(descriptor);
                    }
                }
            }
        }
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Control what type of mobs can spawn", "in addition to normal mob spawning." };
    }

    private static boolean isValidMobEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        if (stackEssence.getItem() != DimletConstructionSetup.syringeItem) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int level = essenceCompound.getInteger("level");
        String mob = essenceCompound.getString("mobName");
        if (level < DimletConstructionConfiguration.maxMobInjections || mob == null) {
            return false;
        }
        return true;
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidMobEssence(stackEssence, stackEssence.getTagCompound())) {
            return null;
        }
        String mob = stackEssence.getTagCompound().getString("mobName");
        if (!DimletCraftingTools.matchDimletRecipe(new DimletKey(DimletType.DIMLET_MOBS, mob), stackController, stackMemory, stackEnergy)) {
            return null;
        }
        DimletKey mobDimlet = new DimletKey(DimletType.DIMLET_MOBS, mob);
        return mobDimlet;
    }
}
