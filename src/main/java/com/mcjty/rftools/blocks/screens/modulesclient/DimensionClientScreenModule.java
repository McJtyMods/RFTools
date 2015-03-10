package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.events.ButtonEvent;
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

public class DimensionClientScreenModule implements ClientScreenModule {
    private String line = "";
    private int color = 0xffffff;
    private int rfcolor = 0xffffff;
    private int rfcolorNeg = 0xffffff;
    private boolean hidebar = false;
    private boolean hidetext = false;
    private boolean showdiff = false;
    private boolean showpct = false;
    private FormatStyle format = FormatStyle.MODE_FULL;

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

        ClientScreenModuleHelper.renderLevel(fontRenderer, xoffset, currenty, screenData, "RF", hidebar, hidetext, showpct, showdiff, rfcolor, rfcolorNeg, 0xffff0000, 0xff333300, format);
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
        addColorPanel(mc, gui, currentData, moduleGuiChanged, panel);

        addOptionPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addMonitorPanel(mc, gui, currentData, moduleGuiChanged, panel);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
        }

        return panel;
    }

    private void addColorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        ColorChoiceLabel labelColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "color").setTooltips("Color for the label");
        ColorChoiceLabel rfColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "rfcolor").setTooltips("Color for the RF text");
        ColorChoiceLabel rfNegColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "rfcolor_neg").setTooltips("Color for the negative", "RF/tick ratio");
        Panel colorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("L:")).addChild(labelColorSelector).
                addChild(new Label(mc, gui).setText("+:")).addChild(rfColorSelector).
                addChild(new Label(mc, gui).setText("-:")).addChild(rfNegColorSelector).
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


    private void addOptionPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel optionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);

        final ToggleButton barButton = new ToggleButton(mc, gui).setText("Bar").setTooltips("Toggle visibility of the", "energy bar").setDesiredHeight(13).setCheckMarker(true);
        barButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("hidebar", !barButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(barButton);

        ChoiceLabel modeButton = ClientScreenModuleHelper.setupModeCombo(mc, gui, "RF", currentData, moduleGuiChanged);
        optionPanel.addChild(modeButton);

        ChoiceLabel formatButton = ClientScreenModuleHelper.setupFormatCombo(mc, gui, currentData, moduleGuiChanged);
        optionPanel.addChild(formatButton);

        if (currentData != null) {
            barButton.setPressed(!currentData.getBoolean("hidebar"));
        } else {
            barButton.setPressed(true);
        }

        panel.addChild(optionPanel);
    }

    private void addMonitorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel monitorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                setDesiredHeight(16);
        monitorPanel.addChild(new Label(mc, gui).setText("Dimension:"));
        TextField dimensionTextField = new TextField(mc, gui).setTooltips("The id of the dimension", "to monitor");
        monitorPanel.addChild(dimensionTextField);
        dimensionTextField.addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                int dim;
                try {
                    dim = Integer.parseInt(newText);
                } catch (NumberFormatException e) {
                    dim = 0;
                }
                currentData.setInteger("dim", dim);
                moduleGuiChanged.updateData();
            }
        });
        if (currentData != null) {
            dimensionTextField.setText(Integer.toString(currentData.getInteger("dim")));
        }
        panel.addChild(monitorPanel);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("rfcolor")) {
                rfcolor = tagCompound.getInteger("rfcolor");
            } else {
                rfcolor = 0xffffff;
            }
            if (tagCompound.hasKey("rfcolor_neg")) {
                rfcolorNeg = tagCompound.getInteger("rfcolor_neg");
            } else {
                rfcolorNeg = 0xffffff;
            }

            hidebar = tagCompound.getBoolean("hidebar");
            hidetext = tagCompound.getBoolean("hidetext");
            showdiff = tagCompound.getBoolean("showdiff");
            showpct = tagCompound.getBoolean("showpct");

            format = FormatStyle.values()[tagCompound.getInteger("format")];
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
