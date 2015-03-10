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

import java.text.DecimalFormat;

public class ClientScreenModuleHelper {

    public static void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, Object[] screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                                   int poscolor, int negcolor,
                                   int gradient1, int gradient2, FormatStyle formatStyle) {
        if (screenData == null) {
            return;
        }

        long maxContents = 0;
        try {
            maxContents = (Long) screenData[1];
        } catch (Exception e) {
            return;
        }
        if (maxContents > 0) {
            if (!hidebar) {
                long contents = (Long) screenData[0];

                int width = 80 - xoffset + 7 + 40;
                long value = contents * width / maxContents;
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
                long diff = (Long) screenData[2];
                if (diff < 0) {
                    fontRenderer.drawString(diff + " " + label + "/t", xoffset, currenty, negcolor);
                } else {
                    fontRenderer.drawString("+" + diff + " " + label + "/t", xoffset, currenty, poscolor);
                }

            } else if (maxContents > 0) {
                long contents = (Long) screenData[0];
                if (showpct) {
                    long value = contents * 100 / maxContents;
                    if (value < 0) {
                        value = 0;
                    } else if (value > 100) {
                        value = 100;
                    }
                    fontRenderer.drawString(value + "%", xoffset, currenty, poscolor);
                } else {
                    fontRenderer.drawString(format(String.valueOf(contents), formatStyle) + label, xoffset, currenty, poscolor);
                }
            }
        }
    }

    private static DecimalFormat dfCommas = new DecimalFormat("###,###");

    public static String format(String in, FormatStyle style) {
        switch (style) {
            case MODE_FULL:
                return in;
            case MODE_COMPACT: {
                long contents = Long.parseLong(in);
                int unit = 1000;
                if (contents < unit) {
                    return in;
                }
                int exp = (int) (Math.log(contents) / Math.log(unit));
                char pre = "KMGTP".charAt(exp-1);
                return String.format("%.1f %s", contents / Math.pow(unit, exp), pre);
            }
            case MODE_COMMAS:
                return dfCommas.format(Long.parseLong(in));
        }
        return in;
    }

    public static ChoiceLabel setupFormatCombo(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        final String modeFull = FormatStyle.MODE_FULL.getName();
        final String modeCompact = FormatStyle.MODE_COMPACT.getName();
        final String modeCommas = FormatStyle.MODE_COMMAS.getName();
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(60).setDesiredHeight(13).addChoices(modeFull, modeCompact, modeCommas).
                setChoiceTooltip(modeFull, "Full format: 3123555").
                setChoiceTooltip(modeCompact, "Compact format: 3.1M").
                setChoiceTooltip(modeCommas, "Comma format: 3,123,555").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        currentData.setInteger("format", FormatStyle.getStyle(newChoice).ordinal());
                        moduleGuiChanged.updateData();
                    }
                });

        FormatStyle currentFormat = FormatStyle.values()[currentData.getInteger("format")];
        modeButton.setChoice(currentFormat.getName());

        return modeButton;
    }

    public static ChoiceLabel setupModeCombo(Minecraft mc, Gui gui, final String componentName, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        String modeNone = "None";
        final String modePertick = componentName + "/t";
        final String modePct = componentName + "%";
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(60).setDesiredHeight(13).addChoices(modeNone, componentName, modePertick, modePct).
                setChoiceTooltip(modeNone, "No text is shown").
                setChoiceTooltip(componentName, "Show the amount of " + componentName).
                setChoiceTooltip(modePertick, "Show the average "+componentName+"/tick", "gain or loss").
                setChoiceTooltip(modePct, "Show the amount of "+componentName, "as a percentage").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        if (componentName.equals(newChoice)) {
                            currentData.setBoolean("showdiff", false);
                            currentData.setBoolean("showpct", false);
                            currentData.setBoolean("hidetext", false);
                        } else if (modePertick.equals(newChoice)) {
                            currentData.setBoolean("showdiff", true);
                            currentData.setBoolean("showpct", false);
                            currentData.setBoolean("hidetext", false);
                        } else if (modePct.equals(newChoice)) {
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
            modeButton.setChoice(modeNone);
        } else if (currentData.getBoolean("showdiff")) {
            modeButton.setChoice(modePertick);
        } else if (currentData.getBoolean("showpct")) {
            modeButton.setChoice(modePct);
        } else {
            modeButton.setChoice(componentName);
        }

        return modeButton;
    }

}
