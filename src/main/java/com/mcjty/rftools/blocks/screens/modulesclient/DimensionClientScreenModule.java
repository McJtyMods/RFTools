package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ColorChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
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
    private boolean hidebar = false;
    private boolean hidetext = false;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, String screenData) {
        GL11.glDisable(GL11.GL_LIGHTING);
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
        }

        int energy;
        if (screenData == null) {
            energy = 0;
        } else {
            energy = Integer.parseInt(screenData);
        }

        if (!hidebar) {
            long maxEnergy = DimletConfiguration.MAX_DIMENSION_POWER;
            int width = 80;
            long value = (long) energy * width / maxEnergy;
            if (value < 0) {
                value = 0;
            } else if (value > width) {
                value = width;
            }
            RenderHelper.drawHorizontalGradientRect(7 + 40, currenty, (int) (7 + 40 + value), currenty + 8, 0xffff0000, 0xff333300);
        }
        if (!hidetext) {
            fontRenderer.drawString(energy + "RF", 7 + 40, currenty, rfcolor);
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
        ColorChoiceLabel labelColorSelector = addColorPanel(mc, gui, currentData, moduleGuiChanged, panel, "color", "Label Color:");
        ColorChoiceLabel rfColorSelector = addColorPanel(mc, gui, currentData, moduleGuiChanged, panel, "rfColor", "RF Color:");
        addOptionPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addMonitorPanel(mc, gui, currentData, moduleGuiChanged, panel);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
            int currentColor = currentData.getInteger("color");
            if (currentColor != 0) {
                labelColorSelector.setCurrentColor(currentColor);
            }
            int currentRfColor = currentData.getInteger("rfcolor");
            if (currentRfColor != 0) {
                rfColorSelector.setCurrentColor(currentRfColor);
            }
        }

        return panel;
    }

    private void addOptionPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel optionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);

        final ToggleButton barButton = new ToggleButton(mc, gui).setText("Bar").setTooltips("Toggle visibility of the", "energy bar");
        barButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("hidebar", !barButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(barButton);

        final ToggleButton textButton = new ToggleButton(mc, gui).setText("Text").setTooltips("Toggle visibility of the", "energy text");
        textButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("hidetext", !textButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(textButton);

        if (currentData != null) {
            barButton.setPressed(!currentData.getBoolean("hidebar"));
            textButton.setPressed(!currentData.getBoolean("hidetext"));
        } else {
            barButton.setPressed(true);
            textButton.setPressed(true);
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

    private ColorChoiceLabel addColorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel, final String tagName, String labelName) {
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(50).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger(tagName, newColor);
                moduleGuiChanged.updateData();
            }
        });
        Panel colorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText(labelName)).
                addChild(colorSelector).
                setDesiredHeight(12);
        panel.addChild(colorPanel);
        return colorSelector;
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

            hidebar = tagCompound.getBoolean("hidebar");
            hidetext = tagCompound.getBoolean("hidetext");
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
