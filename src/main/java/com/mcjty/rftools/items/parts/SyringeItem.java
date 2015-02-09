package com.mcjty.rftools.items.parts;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimletconstruction.DimletConstructionConfiguration;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletMapping;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

public class SyringeItem extends Item {

    private IIcon filledLevel[] = new IIcon[6];

    public SyringeItem() {
        setMaxStackSize(1);
        setTextureName(RFTools.MODID + ":parts/syringeItem0");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);
        for (int i = 0 ; i <= 5 ; i++) {
            filledLevel[i] = iconRegister.registerIcon(RFTools.MODID + ":parts/syringeItem" + i);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                int mobId = tagCompound.getInteger("mobId");
                if (mobId > 0) {
                    DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(mobId);
                    if (entry != null) {
                        RFTools.message(player, EnumChatFormatting.BLUE + "Mob: " + entry.getKey().getName());
                    }
                }
                int level = tagCompound.getInteger("level");
                level = level * 100 / DimletConstructionConfiguration.maxMobInjections;
                RFTools.message(player, EnumChatFormatting.BLUE + "Essence level: " + level + "%");
            }
            return stack;
        }
        return stack;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        int id = findSelectedMobId(entity);
        if (id != -1) {
            int prevId = -1;
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                prevId = tagCompound.getInteger("mobId");
            } else {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            if (prevId != id) {
                tagCompound.setInteger("mobId", id);
                tagCompound.setInteger("level", 1);
            } else {
                int level = tagCompound.getInteger("level");
                level++;
                if (level > DimletConstructionConfiguration.maxMobInjections) {
                    level = DimletConstructionConfiguration.maxMobInjections;
                }
                tagCompound.setInteger("level", level);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    private int findSelectedMobId(Entity entity) {
        for (Map.Entry<Integer, MobDescriptor> entry : DimletMapping.idtoMob.entrySet()) {
            if (entry.getValue().getEntityClass().isAssignableFrom(entity.getClass())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int mobId = tagCompound.getInteger("mobId");
            if (mobId > 0) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(mobId);
                if (entry != null) {
                    list.add(EnumChatFormatting.BLUE + "Mob: " + entry.getKey().getName());
                }
            }
            int level = tagCompound.getInteger("level");
            level = level * 100 / DimletConstructionConfiguration.maxMobInjections;
            list.add(EnumChatFormatting.BLUE + "Essence level: " + level + "%");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Use this to extract essence from mobs. This");
            list.add(EnumChatFormatting.WHITE + "essence can then be used in the Dimlet");
            list.add(EnumChatFormatting.WHITE + "Workbench. Be careful!");
        } else {
            list.add(EnumChatFormatting.WHITE + "Press Shift for more");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        int level = 0;
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            level = tagCompound.getInteger("level");
        }
        if (level == 0) {
            return filledLevel[0];
        } else if (level == DimletConstructionConfiguration.maxMobInjections) {
            return filledLevel[5];
        } else {
            level = ((level-1) * 4 / (DimletConstructionConfiguration.maxMobInjections-1)) + 1;
            return filledLevel[level];
        }
    }


}
