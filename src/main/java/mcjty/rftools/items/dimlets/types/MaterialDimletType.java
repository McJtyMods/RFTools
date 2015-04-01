package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletObjectMapping;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MaterialDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_material";

    private static int rarity = DimletRandomizer.RARITY_1;
    private static int baseCreationCost = 300;
    private static int baseMaintainCost = 10;
    private static int baseTickCost = 100;

    @Override
    public String getName() {
        return "Material";
    }

    @Override
    public String getOpcode() {
        return "m";
    }

    @Override
    public String getTextureName() {
        return "materialDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the material dimlet type. Note that this is a modifier so actual cost depends on terrain/feature you use this with");
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
        return true;
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
        return false;
    }

    @Override
    public void inject(DimletKey key, DimensionInformation dimensionInformation) {

    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        // As a modifier this is handled in the dimlet that is being modified.
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This is a modifier for terrain, tendrils, canyons, orbs,", "liquid orbs, or oregen.", "Put these dimlets BEFORE the thing you want", "to change." };
    }

    private static boolean isValidMaterialEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = BlockTools.getBlock(stackEssence);

        if (essenceBlock != ModBlocks.materialAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        int blockID = essenceCompound.getInteger("block");
        if (absorbing > 0 || blockID == -1) {
            return false;
        }
        return true;
    }

    private static DimletKey findMaterialDimlet(NBTTagCompound essenceCompound) {
        int blockID = essenceCompound.getInteger("block");
        int meta = essenceCompound.getInteger("meta");
        for (Map.Entry<DimletKey, BlockMeta> entry : DimletObjectMapping.idToBlock.entrySet()) {
            if (entry.getValue() != null) {
                int id = Block.blockRegistry.getIDForObject(entry.getValue().getBlock());
                if (blockID == id && meta == entry.getValue().getMeta()) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidMaterialEssence(stackEssence, stackEssence.getTagCompound())) {
            return null;
        }
        DimletKey materialDimlet = findMaterialDimlet(stackEssence.getTagCompound());
        if (materialDimlet == null) {
            return null;
        }
        if (!DimletCraftingTools.matchDimletRecipe(materialDimlet, stackController, stackMemory, stackEnergy)) {
            return null;
        }
        return materialDimlet;
    }
}
