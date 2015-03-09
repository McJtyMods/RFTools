package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class KnownDimlet extends Item {
    private final Map<DimletType, IIcon> icons = new HashMap<DimletType, IIcon>();

    public KnownDimlet() {
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (DimletType type : DimletType.values()) {
            IIcon icon = iconRegister.registerIcon(RFTools.MODID + ":dimlets/" + type.getTextureName());
            icons.put(type, icon);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return stack;
        }

        DimletKey key = KnownDimletConfiguration.getDimletKey(stack, world);
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            if (isSeedDimlet(entry)) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                }

                boolean locked = tagCompound.getBoolean("locked");
                if (locked) {
                    RFTools.message(player, EnumChatFormatting.YELLOW + "This seed dimlet is locked. You cannot modify it!");
                    return stack;
                }

                long forcedSeed = tagCompound.getLong("forcedSeed");
                if (player.isSneaking()) {
                    if (forcedSeed == 0) {
                        RFTools.message(player, EnumChatFormatting.YELLOW + "This dimlet has no seed. You cannot lock it!");
                        return stack;
                    }
                    tagCompound.setBoolean("locked", true);
                    RFTools.message(player, "Dimlet locked!");
                } else {
                    long seed = world.getSeed();
                    tagCompound.setLong("forcedSeed", seed);
                    RFTools.message(player, "Seed set to: " + seed);
                }

                stack.setTagCompound(tagCompound);
            }
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        DimletKey key = KnownDimletConfiguration.getDimletKey(itemStack, player.getEntityWorld());
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry == null) {
            // Safety. Should not occur.
            list.add(EnumChatFormatting.RED + "Something is wrong!");
            list.add(EnumChatFormatting.RED + "Dimlet with key " + key + " (id " + itemStack.getItemDamage() + ") is missing!");
            return;
        }

        list.add(EnumChatFormatting.BLUE + "Rarity: " + entry.getRarity() + (KnownDimletConfiguration.craftableDimlets.contains(key) ? " (craftable)" : ""));
        list.add(EnumChatFormatting.YELLOW + "Create cost: " + entry.getRfCreateCost() + " RF/tick");
        int maintainCost = entry.getRfMaintainCost();
        if (maintainCost < 0) {
            list.add(EnumChatFormatting.YELLOW + "Maintain cost: " + maintainCost + "% RF/tick");
        } else {
            list.add(EnumChatFormatting.YELLOW + "Maintain cost: " + maintainCost + " RF/tick");
        }
        list.add(EnumChatFormatting.YELLOW + "Tick cost: " + entry.getTickCost() + " ticks");

        if (isSeedDimlet(entry)) {
            NBTTagCompound tagCompound = itemStack.getTagCompound();
            if (tagCompound != null && tagCompound.getLong("forcedSeed") != 0) {
                long forcedSeed = tagCompound.getLong("forcedSeed");
                boolean locked = tagCompound.getBoolean("locked");
                list.add(EnumChatFormatting.BLUE + "Forced seed: " + forcedSeed + (locked ? " [LOCKED]" : ""));
            } else {
                list.add(EnumChatFormatting.BLUE + "Right click to copy seed from dimension.");
                list.add(EnumChatFormatting.BLUE + "Shift-Right click to lock copied seed.");
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            list.add(EnumChatFormatting.AQUA + "Dimlet key: " + key);
            if (KnownDimletConfiguration.isNewKnownDimlet(itemStack)) {
                list.add(EnumChatFormatting.AQUA + "This dimlet uses a new key");
            } else {
                list.add(EnumChatFormatting.AQUA + "This dimlet uses an old key");
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            for (String info : entry.getKey().getType().getInformation()) {
                list.add(EnumChatFormatting.WHITE + info);
            }
            List<String> extra = KnownDimletConfiguration.idToExtraInformation.get(entry.getKey());
            if (extra != null) {
                for (String info : extra) {
                    list.add(EnumChatFormatting.YELLOW + info);
                }
            }
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private boolean isSeedDimlet(DimletEntry entry) {
        return entry.getKey().getType() == DimletType.DIMLET_SPECIAL && "Seed".equals(entry.getKey().getName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        DimletKey key = KnownDimletConfiguration.getDimletKey(stack, null);
        if (key == null) {
            // Safety. Should not occur.
            return icons.get(DimletType.DIMLET_SPECIAL);
        }
        DimletType type = key.getType();
        return icons.get(type);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        DimletKey key = KnownDimletConfiguration.getDimletKey(itemStack, null);
        String name = KnownDimletConfiguration.idToDisplayName.get(key);
        if (name == null) {
            return "<unknown dimlet>";
        }
        return name;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        return getUnlocalizedName(itemStack);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        DimletMapping mapping = DimletMapping.getInstance();
        if (mapping != null) {
            List<DimletKey> sortedKeys = new ArrayList<DimletKey>(mapping.getKeys());
            Collections.sort(sortedKeys,
                    new Comparator<DimletKey>() {
                        @Override
                        public int compare(DimletKey o1, DimletKey o2) {
                            if (o1.getType() == o2.getType()) {
                                return o1.getName().compareTo(o2.getName());
                            } else {
                                return o1.getType().compareTo(o2.getType());
                            }
                        }
                    });
            for (DimletKey key : sortedKeys) {
                list.add(KnownDimletConfiguration.makeKnownDimlet(key, null));
            }
        }
    }

}
