package mcjty.rftools;

import cpw.mods.fml.common.registry.VillagerRegistry;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.DimletEntry;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import mcjty.rftools.items.parts.StructureEssenceItem;
import mcjty.varia.WeightedRandomSelector;
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
        VillagerRegistry.instance().registerVillageTradeHandler(GeneralConfiguration.realVillagerId, this);
    }

    @Override
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
        switch (random.nextInt(4)) {
            case 0:
                getRandomBuyEssence(recipeList, random);
                break;
            case 1:
                getRandomSellEssence(recipeList, random);
                break;
            case 2:
                recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(DimletSetup.unknownDimlet, 2 + random.nextInt(3))));
                break;
            case 3:
                getRandomBuyDimlet(villager, recipeList, random, 1);
                break;
        }

        switch (random.nextInt(5)) {
            case 0:
                getRandomBuyEssence(recipeList, random);
                break;
            case 1:
                getRandomSellEssence(recipeList, random);
                break;
            case 2:
                recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(DimletSetup.unknownDimlet, 3 + random.nextInt(6))));
                break;
            case 3:
                getRandomBuyDimlet(villager, recipeList, random, 2);
                break;
            case 4:
                getRandomSellDimlet(villager, recipeList, random, 0.15f);
                break;
        }

        getRandomSellDimlet(villager, recipeList, random, 1.0f);
    }

    private void getRandomBuyEssence(MerchantRecipeList recipeList, Random random) {
        List<Integer> keys = new ArrayList<Integer>(StructureEssenceItem.structures.keySet());
        int structureType = keys.get(random.nextInt(keys.size()));
        recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 2 + random.nextInt(3)), new ItemStack(DimletConstructionSetup.structureEssenceItem, 1, structureType)));
    }

    private void getRandomSellEssence(MerchantRecipeList recipeList, Random random) {
        List<Integer> keys = new ArrayList<Integer>(StructureEssenceItem.structures.keySet());
        int structureType = keys.get(random.nextInt(keys.size()));
        recipeList.add(new MerchantRecipe(new ItemStack(DimletConstructionSetup.structureEssenceItem, 1, structureType), new ItemStack(Items.emerald, 2 + random.nextInt(3))));
    }

    private void getRandomSellDimlet(EntityVillager villager, MerchantRecipeList recipeList, Random random, float dimletBonus) {
        WeightedRandomSelector.Distribution<Integer> distribution = DimletRandomizer.randomDimlets.createDistribution(dimletBonus);
        DimletKey dimlet = DimletRandomizer.getRandomDimlet(distribution, random);
        if (dimlet != null) {
            DimletEntry entry = KnownDimletConfiguration.idToDimletEntry.get(dimlet);
            if (entry != null) {
                int rarity = entry.getRarity();
                ItemStack dimletStack = KnownDimletConfiguration.makeKnownDimlet(dimlet, villager.worldObj);
                recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, (2 + random.nextInt(2)) * (rarity + 1)), dimletStack));
            }
        }
    }

    private void getRandomBuyDimlet(EntityVillager villager, MerchantRecipeList recipeList, Random random, int bonus) {
        DimletKey dimlet = DimletRandomizer.getRandomDimlet(random);
        if (dimlet != null) {
            DimletEntry entry = KnownDimletConfiguration.idToDimletEntry.get(dimlet);
            if (entry != null) {
                int rarity = entry.getRarity();
                ItemStack dimletStack = KnownDimletConfiguration.makeKnownDimlet(dimlet, villager.worldObj);
                recipeList.add(new MerchantRecipe(dimletStack, new ItemStack(Items.emerald, (bonus + random.nextInt(2)) * (rarity + 1))));
            }
        }
    }
}
