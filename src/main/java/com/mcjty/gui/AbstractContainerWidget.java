package com.mcjty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;

public class AbstractContainerWidget<P extends AbstractContainerWidget> extends AbstractWidget<P> {
    protected ArrayList<Widget> children = new ArrayList<Widget>();

    public AbstractContainerWidget(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        markDirty();
        super.setBounds(bounds);
    }


    public P addChild(Widget child) {
        children.add(child);
        markDirty();
        return (P) this;
    }

    public P removeChild(Widget child) {
        children.remove(child);
        markDirty();
        return (P) this;
    }

    public void removeChildren() {
        children.clear();
        markDirty();
    }

}
