package mcjty.rftools.items;

import mcjty.lib.varia.Logging;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                String mob = tagCompound.getString("mobName");
                if (mob != null) {
                    Logging.message(player, TextFormatting.BLUE + "Mob: " + mob);
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
            String prevMob = null;
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                prevMob = tagCompound.getString("mobClass");
            } else {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            if (prevMob == null || !prevMob.equals(clazz.getCanonicalName())) {
                tagCompound.setString("mobClass", clazz.getCanonicalName());
                tagCompound.setString("mobName", findSelectedMobName(entity));
                tagCompound.setString("mobId", EntityList.classToStringMapping.get(clazz));
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
        return super.onLeftClickEntity(stack, player, entity);
    }

    private String findSelectedMobClassName(Entity entity) {
        Class<? extends Entity> entityClass = findSelectedMobClass(entity);
        return entityClass.getCanonicalName();
    }

    private Class<? extends Entity> findSelectedMobClass(Entity entity) {
        // First try to find an exact matching class.
        Class<? extends Entity> entityClass = entity.getClass();

        // Special case for the ender dragon
        if (entity instanceof EntityDragonPart) {
            entityClass = EntityDragon.class;
        }
        return entityClass;
    }

    private String findSelectedMobName(Entity entity) {
        return entity.getName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobName");
            if (mob != null) {
                list.add(TextFormatting.BLUE + "Mob: " + mob);
            }
            int level = tagCompound.getInteger("level");
            level = level * 100 / GeneralConfiguration.maxMobInjections;
            list.add(TextFormatting.BLUE + "Essence level: " + level + "%");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Use this to extract essence from mobs");
            list.add(TextFormatting.WHITE + "Workbench. Be careful!");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
