package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.api.screens.FormatStyle;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.blocks.screens.IModuleGuiChanged;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ScreenModuleGuiBuilder implements IModuleGuiBuilder {
    private Minecraft mc;
    private Gui gui;
    private NBTTagCompound currentData;
    private IModuleGuiChanged moduleGuiChanged;

    private Panel panel;
    private List<Widget> row = new ArrayList<>();

    public ScreenModuleGuiBuilder(Minecraft mc, Gui gui, NBTTagCompound currentData, IModuleGuiChanged moduleGuiChanged) {
        this.gui = gui;
        this.mc = mc;
        this.moduleGuiChanged = moduleGuiChanged;
        this.currentData = currentData;
        panel = new Panel(mc, gui).setLayout(new VerticalLayout().setVerticalMargin(5));
    }

    public NBTTagCompound getCurrentData() {
        return currentData;
    }

    public IModuleGuiChanged getModuleGuiChanged() {
        return moduleGuiChanged;
    }

    public Gui getGui() {
        return gui;
    }

    public ScreenModuleGuiBuilder overridePanel(Panel p) {
        panel = p;
        return this;
    }

    public Panel build() {
        nl();
        return panel;
    }

    @Override
    public ScreenModuleGuiBuilder label(String text) {
        Label label = new Label(mc, gui).setText(text);
        row.add(label);
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder leftLabel(String text) {
        Label label = new Label(mc, gui).setText(text).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        row.add(label);
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder text(final String tagname, String... tooltip) {
        TextField textField = new TextField(mc, gui).setDesiredHeight(15).setTooltips(tooltip).addTextEvent((parent, newText) -> {
            currentData.setString(tagname, newText);
            moduleGuiChanged.updateData();
        });
        row.add(textField);
        if (currentData != null) {
            textField.setText(currentData.getString(tagname));
        }
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder integer(final String tagname, String... tooltip) {
        TextField textField = new TextField(mc, gui).setDesiredHeight(15).setTooltips(tooltip).addTextEvent((parent, newText) -> {
            int value;
            try {
                value = Integer.parseInt(newText);
            } catch (NumberFormatException e) {
                value = 0;
            }
            currentData.setInteger(tagname, value);
            moduleGuiChanged.updateData();
        });
        row.add(textField);
        if (currentData != null) {
            textField.setText(Integer.toString(currentData.getInteger(tagname)));
        }
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder toggle(final String tagname, String label, String... tooltip) {
        final ToggleButton toggleButton = new ToggleButton(mc, gui).setText(label).setTooltips(tooltip).setDesiredHeight(14).setCheckMarker(true);
        toggleButton.addButtonEvent(parent -> {
            currentData.setBoolean(tagname, toggleButton.isPressed());
            moduleGuiChanged.updateData();
        });

        row.add(toggleButton);
        if (currentData != null) {
            toggleButton.setPressed(currentData.getBoolean(tagname));
        }
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder toggleNegative(final String tagname, String label, String... tooltip) {
        final ToggleButton toggleButton = new ToggleButton(mc, gui).setText(label).setTooltips(tooltip).setDesiredHeight(14).setDesiredWidth(36).setCheckMarker(true);
        toggleButton.addButtonEvent(parent -> {
            currentData.setBoolean(tagname, !toggleButton.isPressed());
            moduleGuiChanged.updateData();
        });

        row.add(toggleButton);
        if (currentData != null) {
            toggleButton.setPressed(!currentData.getBoolean(tagname));
        } else {
            toggleButton.setPressed(true);
        }
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder color(final String tagname, String... tooltip) {
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, gui).setTooltips(tooltip)
                .addColors(0xffffff, 0x888888, 0x010101, 0xff0000, 0x880000, 0x00ff00, 0x008800, 0x0000ff, 0x000088, 0xffff00, 0x888800, 0xff00ff, 0x880088, 0x00ffff, 0x008888)
                .setDesiredWidth(20).setDesiredHeight(14).addChoiceEvent((parent, newColor) -> {
                    currentData.setInteger(tagname, newColor);
                    moduleGuiChanged.updateData();
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

    @Override
    public ScreenModuleGuiBuilder format(String tagname) {
        ChoiceLabel label = setupFormatCombo(mc, gui, tagname, currentData, moduleGuiChanged);
        row.add(label);
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder mode(String componentName) {
        ChoiceLabel label = setupModeCombo(mc, gui, componentName, currentData, moduleGuiChanged);
        row.add(label);
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder block(String tagnamePos) {
        String monitoring;
        if (currentData.hasKey(tagnamePos + "x")) {
            int dim;
            if (currentData.hasKey(tagnamePos + "dim")) {
                dim = currentData.getInteger(tagnamePos + "dim");
            } else {
                // For compatibility reasons.
                dim = currentData.getInteger("dim");
            }
            World world = mc.thePlayer.worldObj;
            if (dim == world.provider.getDimension()) {
                int x = currentData.getInteger(tagnamePos+"x");
                int y = currentData.getInteger(tagnamePos+"y");
                int z = currentData.getInteger(tagnamePos+"z");
                monitoring = currentData.getString(tagnamePos+"name");
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

    @Override
    public IModuleGuiBuilder ghostStack(String tagname) {
        ItemStack stack = null;
        if (currentData.hasKey(tagname)) {
            stack = ItemStack.loadItemStackFromNBT(currentData.getCompoundTag(tagname));
        }

        BlockRender blockRender = new BlockRender(mc, gui).setRenderItem(stack).setDesiredWidth(20).setFilledBackground(0xff555555);
        row.add(blockRender);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget widget) {
                ItemStack holding = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
                blockRender.setRenderItem(holding);
                if (holding == null) {
                    currentData.removeTag(tagname);
                } else {
                    NBTTagCompound tc = new NBTTagCompound();
                    holding.writeToNBT(tc);
                    currentData.setTag(tagname, tc);
                }
                moduleGuiChanged.updateData();
            }

            @Override
            public void doubleClick(Widget widget) {

            }
        });

        return this;
    }

    @Override
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

    private static ChoiceLabel setupFormatCombo(Minecraft mc, Gui gui, String tagname, final NBTTagCompound currentData, final IModuleGuiChanged moduleGuiChanged) {
        final String modeFull = FormatStyle.MODE_FULL.getName();
        final String modeCompact = FormatStyle.MODE_COMPACT.getName();
        final String modeCommas = FormatStyle.MODE_COMMAS.getName();
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(58).setDesiredHeight(14).addChoices(modeFull, modeCompact, modeCommas).
                setChoiceTooltip(modeFull, "Full format: 3123555").
                setChoiceTooltip(modeCompact, "Compact format: 3.1M").
                setChoiceTooltip(modeCommas, "Comma format: 3,123,555").
                addChoiceEvent((parent, newChoice) -> {
                    currentData.setInteger(tagname, FormatStyle.getStyle(newChoice).ordinal());
                    moduleGuiChanged.updateData();
                });

        FormatStyle currentFormat = FormatStyle.values()[currentData.getInteger(tagname)];
        modeButton.setChoice(currentFormat.getName());

        return modeButton;
    }

    private static ChoiceLabel setupModeCombo(Minecraft mc, Gui gui, final String componentName, final NBTTagCompound currentData, final IModuleGuiChanged moduleGuiChanged) {
        String modeNone = "None";
        final String modePertick = componentName + "/t";
        final String modePct = componentName + "%";
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(50).setDesiredHeight(14).addChoices(modeNone, componentName, modePertick, modePct).
                setChoiceTooltip(modeNone, "No text is shown").
                setChoiceTooltip(componentName, "Show the amount of " + componentName).
                setChoiceTooltip(modePertick, "Show the average "+componentName+"/tick", "gain or loss").
                setChoiceTooltip(modePct, "Show the amount of "+componentName, "as a percentage").
                addChoiceEvent((parent, newChoice) -> {
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
