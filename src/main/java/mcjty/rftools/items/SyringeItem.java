package mcjty.rftools.items;

import mcjty.lib.varia.EntityTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

//import net.minecraft.entity.monster.SkeletonType;

public class SyringeItem extends GenericRFToolsItem {

    public SyringeItem() {
        super("syringe");
        setMaxStackSize(1);
        setContainerItem(this);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        return tagCompound != null && tagCompound.getInt("level") > 0;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            for(EntityEntry entry : ForgeRegistries.ENTITIES) {
                Class<? extends Entity> clazz = entry.getEntityClass();
                if(MobEntity.class.isAssignableFrom(clazz)) {
                    items.add(createMobSyringe(clazz));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0 ; i <= 5 ; i++) {
            String domain = getRegistryName().getResourceDomain();
            String path = getRegistryName().getResourcePath();
            ModelBakery.registerItemVariants(this, new ModelResourceLocation(new ResourceLocation(domain, path + i), "inventory"));
        }

        ModelLoader.setCustomMeshDefinition(this, stack -> {
            CompoundNBT tagCompound = stack.getTag();
            int level = 0;
            if (tagCompound != null) {
                level = tagCompound.getInt("level");
            }
            if (level <= 0) {
                level = 0;
            } else if (level >= GeneralConfiguration.maxMobInjections.get()) {
                level = 5;
            } else {
                level = ((level-1) * 4 / (GeneralConfiguration.maxMobInjections.get()-1)) + 1;
            }
            String domain = getRegistryName().getResourceDomain();
            String path = getRegistryName().getResourcePath();
            return new ModelResourceLocation(new ResourceLocation(domain, path + level), "inventory");
        });
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

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
                stack.setTagCompound(tagCompound);
            }
            String id = findSelectedMobId(entityLiving);
            if (id != null && !id.isEmpty()) {
                if (!id.equals(prevMobId)) {
                    tagCompound.setString("mobName", entityLiving.getName());
                    tagCompound.setString("mobId", id);
                    tagCompound.putInt("level", 1);
                } else {
                    tagCompound.putInt("level", Math.min(tagCompound.getInt("level") + 1, GeneralConfiguration.maxMobInjections.get()));
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    private static @Nullable
    MobEntity getEntityLivingFromClickedEntity(Entity entity) {
        if(entity instanceof MobEntity) {
            return (MobEntity)entity;
        } else if(entity instanceof MultiPartEntityPart) {
            IEntityMultiPart parent = ((MultiPartEntityPart)entity).parent;
            if(parent instanceof MobEntity) {
                return (MobEntity)parent;
            }
        }
        return null;
    }

    private String findSelectedMobId(Entity entity) {
        ResourceLocation key = EntityList.getKey(entity.getClass());
        return key != null ? key.toString() : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            String mobName = getMobName(itemStack);
            if (mobName != null) {
                list.add(TextFormatting.BLUE + "Mob: " + mobName);
            }
            int level = tagCompound.getInt("level");
            level = level * 100 / GeneralConfiguration.maxMobInjections.get();
            list.add(TextFormatting.BLUE + "Essence level: " + level + "%");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Use this to extract essence from mobs");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    public static ItemStack createMobSyringe(Class<? extends Entity> mobClass) {
        String id = EntityTools.findEntityIdByClass(mobClass);
        String name = EntityTools.findEntityLocNameByClass(mobClass);
        return createMobSyringe(id, name);
    }

    private static ItemStack createMobSyringe(String id, String name) {
        ItemStack syringe = new ItemStack(ModItems.syringeItem);
        CompoundNBT tagCompound = new CompoundNBT();
        tagCompound.setString("mobId", id);
        if (name == null || name.isEmpty()) {
            name = id;
        }
        tagCompound.setString("mobName", name);
        tagCompound.putInt("level", GeneralConfiguration.maxMobInjections.get());
        syringe.setTagCompound(tagCompound);
        return syringe;
    }

    public static String getMobId(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobId");
            if (mob == null) {
                // For compatibility only!
                return tagCompound.getString("mobName");
            } else {
                mob = EntityTools.fixEntityId(mob);
            }
            return mob;
        }
        return null;
    }

    public static String getMobName(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobName");
            if (mob == null || "unknown".equals(mob)) {
                if (tagCompound.hasKey("mobId")) {
                    String mobId = tagCompound.getString("mobId");
                    mobId = EntityTools.fixEntityId(mobId);
                    return mobId;
                } else {
                    return "?";
                }
            }
            return mob;
        }
        return null;
    }

}
