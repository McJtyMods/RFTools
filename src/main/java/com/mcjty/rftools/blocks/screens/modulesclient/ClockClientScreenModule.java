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

public class ClockClientScreenModule implements ClientScreenModule {
    private int color = 0xffffff;
    private String line = "";
    private boolean large = false;

    @Override
    public TransformMode getTransformMode() {
        return large ? TransformMode.TEXTLARGE : TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return large ? 20 : 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft minecraft = Minecraft.getMinecraft();
        double time = 0.0D;

        if (minecraft.theWorld != null && minecraft.thePlayer != null) {
            if (minecraft.theWorld.provider.isSurfaceWorld()) {
                time = minecraft.theWorld.getCelestialAngle(1.0F);
            } else {
                time = Math.random();
            }
        }
        int minutes = (int) (time * ((24 * 60) - 0.1f));
        int hours = minutes / 60;
        hours = (hours + 12) % 24;
        minutes = minutes % 60;
        String timeString;
        if (hours < 10) {
            timeString = "0" + hours;
        } else {
            timeString = Integer.toString(hours);
        }
        timeString += ':';
        if (minutes < 10) {
            timeString += "0" + minutes;
        } else {
            timeString += Integer.toString(minutes);
        }

        if (large) {
            fontRenderer.drawString(line + " " + timeString, 4, currenty / 2 + 1, color);
        } else {
            fontRenderer.drawString(line + " " + timeString, 7, currenty, color);
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
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(50).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger("color", newColor);
                moduleGuiChanged.updateData();
            }
        });

        final ToggleButton largeButton = new ToggleButton(mc, gui).setText("Large").setTooltips("Large or small font").setDesiredHeight(13).setCheckMarker(true);
        largeButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean("large", largeButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });
        panel.addChild(largeButton);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
            int currentColor = currentData.getInteger("color");
            if (currentColor != 0) {
                colorSelector.setCurrentColor(currentColor);
            }
            largeButton.setPressed(currentData.getBoolean("large"));
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
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            large = tagCompound.getBoolean("large");
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
