package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class MobDimletType implements IDimletType {
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
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return false;
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
            if (dimlets.size() == 1 && DimletObjectMapping.idtoMob.get(key).getEntityClass() == null) {
                // Just default.
            } else {
                for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : dimlets) {
                    DimletKey modifierKey = dimletWithModifiers.getLeft();
                    MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(modifierKey);
                    if (descriptor.getEntityClass() != null) {
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
        if (stackEssence.getItem() != ModItems.syringeItem) {
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
    public ItemStack attemptDimletCrafting(World world, ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidMobEssence(stackEssence, stackEssence.getTagCompound())) {
            return null;
        }
        String mob = stackEssence.getTagCompound().getString("mobName");
        if (!DimletCraftingTools.matchDimletRecipe(new DimletKey(DimletType.DIMLET_MOBS, mob), stackController, stackMemory, stackEnergy)) {
            return null;
        }
        DimletKey mobDimlet = new DimletKey(DimletType.DIMLET_MOBS, mob);
        return KnownDimletConfiguration.makeKnownDimlet(mobDimlet, world);
    }
}
