package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.events.ColorChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

public class RedstoneClientScreenModule implements ClientScreenModule {

    private String line = "";
    private String yestext = "on";
    private String notext = "off";
    private int color = 0xffffff;
    private int yescolor = 0xffffff;
    private int nocolor = 0xffffff;
    private int channel = -1;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if (channel != -1) {
            boolean rs;
            if (screenData != null && screenData.length > 0) {
                rs = (Boolean) screenData[0];
            } else {
                rs = false;
            }
            fontRenderer.drawString(rs ? yestext : notext, xoffset, currenty, rs ? yescolor : nocolor);
        } else {
            fontRenderer.drawString("<invalid>", xoffset, currenty, 0xff0000);
        }
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        TextField textField = new TextField(mc, gui).setDesiredHeight(16).setTooltips("Text to use as label").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("text", newText);
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(textField);
        TextField yesTextField = new TextField(mc, gui).setDesiredHeight(16).setTooltips("Positive text").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("yestext", newText);
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(yesTextField);
        TextField noTextField = new TextField(mc, gui).setDesiredHeight(16).setTooltips("Negative text").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("notext", newText);
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(noTextField);
        addColorPanel(mc, gui, currentData, moduleGuiChanged, panel);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
            yesTextField.setText(currentData.getString("yestext"));
            noTextField.setText(currentData.getString("notext"));
        }

        return panel;
    }

    private void addColorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        ColorChoiceLabel labelColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "color").setTooltips("Color for the label");
        ColorChoiceLabel yesColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "yescolor").setTooltips("Positive color");
        ColorChoiceLabel noColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "nocolor").setTooltips("Negative color");
        Panel colorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("L:")).addChild(labelColorSelector).
                addChild(new Label(mc, gui).setText("+:")).addChild(yesColorSelector).
                addChild(new Label(mc, gui).setText("-:")).addChild(noColorSelector).
                setDesiredHeight(12);
        panel.addChild(colorPanel);
    }

    private ColorChoiceLabel addColorSelector(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, final String tagName) {
        ColorChoiceLabel colorChoiceLabel = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(26).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger(tagName, newColor);
                moduleGuiChanged.updateData();
            }
        });
        if (currentData != null) {
            int currentColor = currentData.getInteger(tagName);
            if (currentColor != 0) {
                colorChoiceLabel.setCurrentColor(currentColor);
            }
        }
        return colorChoiceLabel;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("yestext")) {
                yestext = tagCompound.getString("yestext");
            }
            if (tagCompound.hasKey("notext")) {
                notext = tagCompound.getString("notext");
            }
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("yescolor")) {
                yescolor = tagCompound.getInteger("yescolor");
            } else {
                yescolor = 0xffffff;
            }
            if (tagCompound.hasKey("nocolor")) {
                nocolor = tagCompound.getInteger("nocolor");
            } else {
                nocolor = 0xffffff;
            }
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInteger("channel");
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
