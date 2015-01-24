package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public interface ClientScreenModule {
    public enum TransformMode {
        NONE,
        TEXT,
        ITEM
    }

    TransformMode getTransformMode();

    int getHeight();

    void render(FontRenderer fontRenderer, int currenty);

    Panel createGui(Minecraft mc, Gui gui, NBTTagCompound currentData, ModuleGuiChanged moduleGuiChanged);

    void setupFromNBT(NBTTagCompound tagCompound);
}
