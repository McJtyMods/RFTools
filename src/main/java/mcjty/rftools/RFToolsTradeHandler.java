package mcjty.rftools;

import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.items.parts.StructureEssenceItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RFToolsTradeHandler implements VillagerRegistry.IVillageTradeHandler {

    public static RFToolsTradeHandler INSTANCE = new RFToolsTradeHandler();

    public void load() {
        VillagerRegistry.instance().registerVillageTradeHandler(0, this);
        VillagerRegistry.instance().registerVillageTradeHandler(1, this);
        VillagerRegistry.instance().registerVillageTradeHandler(2, this);
        VillagerRegistry.instance().registerVillageTradeHandler(3, this);
        VillagerRegistry.instance().registerVillageTradeHandler(4, this);
    }

    @Override
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
        if (random.nextFloat() < DimletConstructionConfiguration.essenceTradeChance) {
            List<Integer> keys = new ArrayList<Integer>(StructureEssenceItem.structures.keySet());
            int structureType = keys.get(random.nextInt(keys.size()));
            recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 3), new ItemStack(DimletConstructionSetup.structureEssenceItem, 1, structureType)));
        }
    }
}
