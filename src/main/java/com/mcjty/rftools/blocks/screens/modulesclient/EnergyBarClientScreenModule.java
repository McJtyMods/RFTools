package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.events.ColorChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

public class EnergyBarClientScreenModule implements ClientScreenModule {
    private String line = "";
    private int color = 0xffffff;
    private int dim = 0;
    private Coordinate coordinate = Coordinate.INVALID;

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

        if (coordinate.isValid()) {
            int energy;
            int maxEnergy;
            if (screenData == null) {
                energy = 0;
                maxEnergy = 0;
            } else {
                int i = screenData.indexOf('/');
                energy = Integer.parseInt(screenData.substring(0, i));
                maxEnergy = Integer.parseInt(screenData.substring(i+1));
            }

            if (maxEnergy > 0) {
                int width = 80;
                int value = energy * width / maxEnergy;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(7 + 40, currenty, 7 + 40 + value, currenty + 8, 0xffff0000, 0xff333300);
                fontRenderer.drawString(energy + "RF", 7 + 40, currenty, 0xffffff);
            }
        } else {
            fontRenderer.drawString("<invalid>", 7 + 40, currenty, 0xff0000);
        }
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        TextField textField = new TextField(mc, gui).setDesiredHeight(16).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("text", newText);
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(textField);
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(50).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger("color", newColor);
                moduleGuiChanged.updateData();
            }
        });

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
            int currentColor = currentData.getInteger("color");
            if (currentColor != 0) {
                colorSelector.setCurrentColor(currentColor);
            }
        }

        panel.addChild(new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("Color:")).
                addChild(colorSelector).
                setDesiredHeight(18));
        return panel;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            color = tagCompound.getInteger("color");
            coordinate = Coordinate.INVALID;
            if (tagCompound.hasKey("monitorx")) {
                this.dim = tagCompound.getInteger("dim");
                if (dim == this.dim) {
                    Coordinate c = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                    int dx = Math.abs(c.getX() - x);
                    int dy = Math.abs(c.getY() - y);
                    int dz = Math.abs(c.getZ() - z);
                    if (dx <= 16 && dy <= 16 && dz <= 16) {
                        coordinate = c;
                    }
                }
            }
        }
    }
}
