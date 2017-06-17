package mcjty.rftools.items;

import mcjty.lib.tools.EntityTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//import net.minecraft.entity.monster.SkeletonType;

public class SyringeItem extends GenericRFToolsItem {

    public SyringeItem() {
        super("syringe");
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0 ; i <= 5 ; i++) {
            String domain = getRegistryName().getResourceDomain();
            String path = getRegistryName().getResourcePath();
            ModelBakery.registerItemVariants(this, new ModelResourceLocation(new ResourceLocation(domain, path + i), "inventory"));
        }

        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
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
            }
        });
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
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
        Class<? extends Entity> clazz = findSelectedMobClass(entity);
        if (clazz != null) {
            String prevMobId = null;
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                prevMobId = EntityTools.fixEntityId(tagCompound.getString("mobId"));
            } else {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            String id = findSelectedMobId(clazz, entity);
            if (id != null && !id.isEmpty()) {
                if (prevMobId == null || !prevMobId.equals(id)) {
                    tagCompound.setString("mobName", findSelectedMobName(entity));
                    tagCompound.setString("mobId", id);
                    tagCompound.setInteger("level", 1);
                } else {
                    int level = tagCompound.getInteger("level");
                    level++;
                    if (level > GeneralConfiguration.maxMobInjections) {
                        level = GeneralConfiguration.maxMobInjections;
                    }
                    tagCompound.setInteger("level", level);
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    private String findSelectedMobId(Class<? extends Entity> clazz, Entity entity) {
        return EntityTools.findId(clazz, entity);
    }

    private Class<? extends Entity> findSelectedMobClass(Entity entity) {
        // First try to find an exact matching class.
        Class<? extends Entity> entityClass = entity.getClass();
        return entityClass;
    }

    private String findSelectedMobName(Entity entity) {
        return EntityTools.getEntityName(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
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
        String name = EntityTools.findEntityLocNameByClass(mobClass);
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
            if (mob == null) {
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
