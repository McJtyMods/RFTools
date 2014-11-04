package com.mcjty.rftools.blocks.shield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerFilter extends AbstractShieldFilter {
    private String name = null;

    public PlayerFilter() {
    }

    public PlayerFilter(String name) {
        this.name = name;
    }

    @Override
    public String getFilterName() {
        return "player";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean match(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        if (name == null) {
            return true;
        }

        EntityPlayer entityPlayer = (EntityPlayer) entity;
        return name.equals(entityPlayer.getDisplayName());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        name = tagCompound.getString("name");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setString("name", name);
    }
}
