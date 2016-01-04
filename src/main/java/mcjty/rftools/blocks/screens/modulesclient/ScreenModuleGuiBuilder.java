package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.ColorChoiceEvent;
import mcjty.lib.gui.events.TextEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ScreenModuleGuiBuilder {
    private Minecraft mc;
    private Gui gui;
    private NBTTagCompound currentData;
    private ModuleGuiChanged moduleGuiChanged;

    private Panel panel;
    private List<Widget> row = new ArrayList<Widget>();

    public ScreenModuleGuiBuilder(Minecraft mc, Gui gui, NBTTagCompound currentData, ModuleGuiChanged moduleGuiChanged) {
        this.gui = gui;
        this.mc = mc;
        this.moduleGuiChanged = moduleGuiChanged;
        this.currentData = currentData;
        panel = new Panel(mc, gui).setLayout(new VerticalLayout().setVerticalMargin(5));
    }

    public Panel build() {
        nl();
        return panel;
    }

    public ScreenModuleGuiBuilder label(String text) {
        Label label = new Label(mc, gui).setText(text);
        row.add(label);
        return this;
    }

    public ScreenModuleGuiBuilder leftLabel(String text) {
        Label label = new Label(mc, gui).setText(text).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        row.add(label);
        return this;
    }

    public ScreenModuleGuiBuilder text(final String tagname, String... tooltip) {
        TextField textField = new TextField(mc, gui).setDesiredHeight(15).setTooltips(tooltip).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString(tagname, newText);
                moduleGuiChanged.updateData();
            }
        });
        row.add(textField);
        if (currentData != null) {
            textField.setText(currentData.getString(tagname));
        }
        return this;
    }

    public ScreenModuleGuiBuilder integer(final String tagname, String... tooltip) {
        TextField textField = new TextField(mc, gui).setDesiredHeight(15).setTooltips(tooltip).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                int value;
                try {
                    value = Integer.parseInt(newText);
                } catch (NumberFormatException e) {
                    value = 0;
                }
                currentData.setInteger(tagname, value);
                moduleGuiChanged.updateData();
            }
        });
        row.add(textField);
        if (currentData != null) {
            textField.setText(Integer.toString(currentData.getInteger(tagname)));
        }
        return this;
    }

    public ScreenModuleGuiBuilder toggle(final String tagname, String label, String... tooltip) {
        final ToggleButton toggleButton = new ToggleButton(mc, gui).setText(label).setTooltips(tooltip).setDesiredHeight(14).setCheckMarker(true);
        toggleButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean(tagname, toggleButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });

        row.add(toggleButton);
        if (currentData != null) {
            toggleButton.setPressed(currentData.getBoolean(tagname));
        }
        return this;
    }

    public ScreenModuleGuiBuilder toggleNegative(final String tagname, String label, String... tooltip) {
        final ToggleButton toggleButton = new ToggleButton(mc, gui).setText(label).setTooltips(tooltip).setDesiredHeight(14).setDesiredWidth(36).setCheckMarker(true);
        toggleButton.addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                currentData.setBoolean(tagname, !toggleButton.isPressed());
                moduleGuiChanged.updateData();
            }
        });

        row.add(toggleButton);
        if (currentData != null) {
            toggleButton.setPressed(!currentData.getBoolean(tagname));
        } else {
            toggleButton.setPressed(true);
        }
        return this;
    }

    public ScreenModuleGuiBuilder color(final String tagname, String... tooltip) {
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, gui).setTooltips(tooltip)
                .addColors(0xffffff, 0x888888, 0x010101, 0xff0000, 0x880000, 0x00ff00, 0x008800, 0x0000ff, 0x000088, 0xffff00, 0x888800, 0xff00ff, 0x880088, 0x00ffff, 0x008888)
                .setDesiredWidth(20).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, Integer newColor) {
                        currentData.setInteger(tagname, newColor);
                        moduleGuiChanged.updateData();
                    }
                });
        row.add(colorSelector);
        if (currentData != null) {
            int currentColor = currentData.getInteger(tagname);
            if (currentColor != 0) {
                colorSelector.setCurrentColor(currentColor);
            }
        }
        return this;
    }

    public ScreenModuleGuiBuilder format() {
        ChoiceLabel label = setupFormatCombo(mc, gui, currentData, moduleGuiChanged);
        row.add(label);
        return this;
    }

    public ScreenModuleGuiBuilder mode(String componentName) {
        ChoiceLabel label = setupModeCombo(mc, gui, componentName, currentData, moduleGuiChanged);
        row.add(label);
        return this;
    }

    public ScreenModuleGuiBuilder monitor() {
        String monitoring;
        if (currentData.hasKey("monitorx")) {
            int dim = currentData.getInteger("dim");
            World world = mc.thePlayer.worldObj;
            if (dim == world.provider.getDimensionId()) {
                int x = currentData.getInteger("monitorx");
                int y = currentData.getInteger("monitory");
                int z = currentData.getInteger("monitorz");
                monitoring = currentData.getString("monitorname");
                Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                row.add(new BlockRender(mc, gui).setRenderItem(block).setDesiredWidth(20));
                row.add(new Label(mc, gui).setText(x + "," + y + "," + z).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(150));
            } else {
                monitoring = "<unreachable>";
            }
        } else {
            monitoring = "<not set>";
        }
        row.add(new Label(mc, gui).setText(monitoring));

        return this;
    }


    public ScreenModuleGuiBuilder nl() {
        if (row.size() == 1) {
            panel.addChild(row.get(0).setDesiredHeight(16));
            row.clear();
        } else if (!row.isEmpty()) {
            Panel rowPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);
            for (Widget widget : row) {
                rowPanel.addChild(widget);
            }
            panel.addChild(rowPanel);
            row.clear();
        }

        return this;
    }

    private static ChoiceLabel setupFormatCombo(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        final String modeFull = FormatStyle.MODE_FULL.getName();
        final String modeCompact = FormatStyle.MODE_COMPACT.getName();
        final String modeCommas = FormatStyle.MODE_COMMAS.getName();
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(58).setDesiredHeight(14).addChoices(modeFull, modeCompact, modeCommas).
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

    private static ChoiceLabel setupModeCombo(Minecraft mc, Gui gui, final String componentName, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        String modeNone = "None";
        final String modePertick = componentName + "/t";
        final String modePct = componentName + "%";
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(50).setDesiredHeight(14).addChoices(modeNone, componentName, modePertick, modePct).
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
