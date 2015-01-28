package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.RenderHelper;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.widgets.ChoiceLabel;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;

public class ClientScreenModuleHelper {

    public static void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, String[] screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                                   int poscolor, int negcolor,
                                   int gradient1, int gradient2) {
        if (screenData == null) {
            return;
        }

        int maxContents = Integer.parseInt(screenData[1]);
        if (maxContents > 0) {
            if (!hidebar) {
                int contents = Integer.parseInt(screenData[0]);

                int width = 80 - xoffset + 7 + 40;
                long value = (long) contents * width / maxContents;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(xoffset, currenty, (int) (xoffset + value), currenty + 8, gradient1, gradient2);
            }
        }
        if (!hidetext) {
            if (showdiff) {
                if (screenData[2].startsWith("-")) {
                    fontRenderer.drawString(screenData[2] + " " + label + "/t", xoffset, currenty, negcolor);
                } else {
                    fontRenderer.drawString("+" + screenData[2] + " " + label + "/t", xoffset, currenty, poscolor);
                }

            } else if (maxContents > 0) {
                if (showpct) {
                    int contents = Integer.parseInt(screenData[0]);
                    long value = (long)contents * 100 / (long)maxContents;
                    if (value < 0) {
                        value = 0;
                    } else if (value > 100) {
                        value = 100;
                    }
                    fontRenderer.drawString(value + "%", xoffset, currenty, poscolor);
                } else {
                    fontRenderer.drawString(screenData[0] + label, xoffset, currenty, poscolor);
                }
            }
        }
    }

    public static ChoiceLabel setupModeCombo(Minecraft mc, Gui gui, final String componentName, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        String mode_none = "None";
        final String mode_pertick = componentName + "/t";
        final String mode_pct = componentName + "%";
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(60).setDesiredHeight(13).addChoices(mode_none, componentName, mode_pertick, mode_pct).
                setChoiceTooltip(mode_none, "No text is shown").
                setChoiceTooltip(componentName, "Show the amount of " + componentName).
                setChoiceTooltip(mode_pertick, "Show the average "+componentName+"/tick", "gain or loss").
                setChoiceTooltip(mode_pct, "Show the amount of "+componentName, "as a percentage").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        if (componentName.equals(newChoice)) {
                            currentData.setBoolean("showdiff", false);
                            currentData.setBoolean("showpct", false);
                            currentData.setBoolean("hidetext", false);
                        } else if (mode_pertick.equals(newChoice)) {
                            currentData.setBoolean("showdiff", true);
                            currentData.setBoolean("showpct", false);
                            currentData.setBoolean("hidetext", false);
                        } else if (mode_pct.equals(newChoice)) {
                            currentData.setBoolean("showdiff", false);
                            currentData.setBoolean("showpct", true);
                            currentData.setBoolean("hidetext", false);
                        } else {
                            currentData.setBoolean("showdiff", false);
                            currentData.setBoolean("showpct", false);
                            currentData.setBoolean("hidetext", true);
                        }
                        moduleGuiChanged.updateData();
                    }
                });


        if (currentData.getBoolean("hidetext")) {
            modeButton.setChoice(mode_none);
        } else if (currentData.getBoolean("showdiff")) {
            modeButton.setChoice(mode_pertick);
        } else if (currentData.getBoolean("showpct")) {
            modeButton.setChoice(mode_pct);
        } else {
            modeButton.setChoice(componentName);
        }

        return modeButton;
    }

}
