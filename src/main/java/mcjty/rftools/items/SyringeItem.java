package mcjty.rftools.items;

import mcjty.lib.varia.EntityTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

import javax.annotation.Nullable;

//import net.minecraft.entity.monster.SkeletonType;

public class SyringeItem extends GenericRFToolsItem {

    public SyringeItem() {
        super("syringe");
        setMaxStackSize(1);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            for(EntityEntry entry : ForgeRegistries.ENTITIES) {
                Class<? extends Entity> clazz = entry.getEntityClass();
                if(EntityLiving.class.isAssignableFrom(clazz)) {
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
            NBTTagCompound tagCompound = stack.getTagCompound();
            int level = 0;
            if (tagCompound != null) {
                level = tagCompound.getInteger("level");
            }
            if (level <= 0) {
                level = 0;
            } else if (level >= GeneralConfiguration.maxMobInjections) {
                level = 5;
            } else {
                level = ((level-1) * 4 / (GeneralConfiguration.maxMobInjections-1)) + 1;
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                String mobName = getMobName(stack);
                if (mobName != null) {
                    Logging.message(player, TextFormatting.BLUE + "Mob: " + mobName);
                }
                int level = tagCompound.getInteger("level");
                level = level * 100 / GeneralConfiguration.maxMobInjections;
                Logging.message(player, TextFormatting.BLUE + "Essence level: " + level + "%");
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        EntityLiving entityLiving = getEntityLivingFromClickedEntity(entity);
        if(entityLiving != null) {
            String prevMobId = null;
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                prevMobId = EntityTools.fixEntityId(tagCompound.getString("mobId"));
            } else {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            String id = findSelectedMobId(entityLiving);
            if (id != null && !id.isEmpty()) {
                if (!id.equals(prevMobId)) {
                    tagCompound.setString("mobName", entityLiving.getName());
                    tagCompound.setString("mobId", id);
                    tagCompound.setInteger("level", 1);
                } else {
                    tagCompound.setInteger("level", Math.min(tagCompound.getInteger("level") + 1, GeneralConfiguration.maxMobInjections));
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    private static @Nullable EntityLiving getEntityLivingFromClickedEntity(Entity entity) {
        if(entity instanceof EntityLiving) {
            return (EntityLiving)entity;
        } else if(entity instanceof MultiPartEntityPart) {
            IEntityMultiPart parent = ((MultiPartEntityPart)entity).parent;
            if(parent instanceof EntityLiving) {
                return (EntityLiving)parent;
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
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String mobName = getMobName(itemStack);
            if (mobName != null) {
                list.add(TextFormatting.BLUE + "Mob: " + mobName);
            }
            int level = tagCompound.getInteger("level");
            level = level * 100 / GeneralConfiguration.maxMobInjections;
            list.add(TextFormatting.BLUE + "Essence level: " + level + "%");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Use this to extract essence from mobs");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    public static ItemStack createMobSyringe(Class<? extends Entity> mobClass) {
        String id = EntityTools.findEntityIdByClass(mobClass);
//        String name = EntityTools.findEntityLocNameByClass(mobClass);
        String name = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id)).getName();
        return createMobSyringe(id, name);
    }

    private static ItemStack createMobSyringe(String id, String name) {
        ItemStack syringe = new ItemStack(ModItems.syringeItem);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("mobId", id);
        if (name == null || name.isEmpty()) {
            name = id;
        }
        tagCompound.setString("mobName", name);
        tagCompound.setInteger("level", GeneralConfiguration.maxMobInjections);
        syringe.setTagCompound(tagCompound);
        return syringe;
    }

    public static String getMobId(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
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
        NBTTagCompound tagCompound = stack.getTagCompound();
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
