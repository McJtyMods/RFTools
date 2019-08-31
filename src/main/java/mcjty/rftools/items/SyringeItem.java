package mcjty.rftools.items;

import mcjty.lib.McJtyLib;
import mcjty.lib.varia.EntityTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

//import net.minecraft.entity.monster.SkeletonType;

public class SyringeItem extends Item {

    public SyringeItem() {
        super(new Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        // .containerItem(this)??? @todo 1.14 what to do about this?
        setRegistryName("syringe");
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        return tagCompound != null && tagCompound.getInt("level") > 0;
    }

    // @todo 1.14
//    @Override
//    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
//        if(isInCreativeTab(tab)) {
//            items.add(new ItemStack(this));
//            for(EntityEntry entry : ForgeRegistries.ENTITIES) {
//                Class<? extends Entity> clazz = entry.getEntityClass();
//                if(MobEntity.class.isAssignableFrom(clazz)) {
//                    items.add(createMobSyringe(clazz));
//                }
//            }
//        }
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        for (int i = 0 ; i <= 5 ; i++) {
//            String domain = getRegistryName().getResourceDomain();
//            String path = getRegistryName().getResourcePath();
//            ModelBakery.registerItemVariants(this, new ModelResourceLocation(new ResourceLocation(domain, path + i), "inventory"));
//        }
//
//        ModelLoader.setCustomMeshDefinition(this, stack -> {
//            CompoundNBT tagCompound = stack.getTag();
//            int level = 0;
//            if (tagCompound != null) {
//                level = tagCompound.getInt("level");
//            }
//            if (level <= 0) {
//                level = 0;
//            } else if (level >= GeneralConfiguration.maxMobInjections.get()) {
//                level = 5;
//            } else {
//                level = ((level-1) * 4 / (GeneralConfiguration.maxMobInjections.get()-1)) + 1;
//            }
//            String domain = getRegistryName().getResourceDomain();
//            String path = getRegistryName().getResourcePath();
//            return new ModelResourceLocation(new ResourceLocation(domain, path + level), "inventory");
//        });
//    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            CompoundNBT tagCompound = stack.getTag();
            if (tagCompound != null) {
                String mobName = getMobName(stack);
                if (mobName != null) {
                    Logging.message(player, TextFormatting.BLUE + "Mob: " + mobName);
                }
                int level = tagCompound.getInt("level");
                level = level * 100 / GeneralConfiguration.maxMobInjections.get();
                Logging.message(player, TextFormatting.BLUE + "Essence level: " + level + "%");
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        MobEntity entityLiving = getEntityLivingFromClickedEntity(entity);
        if(entityLiving != null) {
            String prevMobId = null;
            CompoundNBT tagCompound = stack.getTag();
            if (tagCompound != null) {
                prevMobId = EntityTools.fixEntityId(tagCompound.getString("mobId"));
            } else {
                tagCompound = new CompoundNBT();
                stack.setTag(tagCompound);
            }
            String id = findSelectedMobId(entityLiving);
            if (id != null && !id.isEmpty()) {
                if (!id.equals(prevMobId)) {
                    tagCompound.putString("mobId", id);
                    tagCompound.putInt("level", 1);
                } else {
                    tagCompound.putInt("level", Math.min(tagCompound.getInt("level") + 1, GeneralConfiguration.maxMobInjections.get()));
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Nullable
    private static MobEntity getEntityLivingFromClickedEntity(Entity entity) {
        if(entity instanceof MobEntity) {
            return (MobEntity)entity;
// @Todo 1.14
            //        } else if(entity instanceof MultiPartEntityPart) {
//            IEntityMultiPart parent = ((MultiPartEntityPart)entity).parent;
//            if(parent instanceof MobEntity) {
//                return (MobEntity)parent;
//            }
        }
        return null;
    }

    private String findSelectedMobId(Entity entity) {
        ResourceLocation key = entity.getType().getRegistryName();
        return key != null ? key.toString() : null;
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            String mobName = getMobName(itemStack);
            if (mobName != null) {
                list.add(new StringTextComponent(TextFormatting.BLUE + "Mob: " + mobName));
            }
            int level = tagCompound.getInt("level");
            level = level * 100 / GeneralConfiguration.maxMobInjections.get();
            list.add(new StringTextComponent(TextFormatting.BLUE + "Essence level: " + level + "%"));
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "Use this to extract essence from mobs"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

    private static ItemStack createMobSyringe(ResourceLocation id) {
        ItemStack syringe = new ItemStack(ModItems.syringeItem);
        CompoundNBT tagCompound = new CompoundNBT();
        tagCompound.putString("mobId", id.toString());
        tagCompound.putInt("level", GeneralConfiguration.maxMobInjections.get());
        syringe.setTag(tagCompound);
        return syringe;
    }

    public static String getMobId(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobId");
            return mob;
        }
        return null;
    }

    public static String getMobName(ItemStack stack) {
        String id = getMobId(stack);
        if (id == null) {
            return "?";
        }
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        if (type == null) {
            return "?";
        }
        return type.getName().getFormattedText();   // @todo 1.14 is this the good way?
    }

}
