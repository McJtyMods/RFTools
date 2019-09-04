package mcjty.rftools.blocks.screens.modulesclient.helper;

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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenModuleGuiBuilder implements IModuleGuiBuilder {
    private Minecraft mc;
    private Screen gui;
    private CompoundNBT currentData;
    private IModuleGuiChanged moduleGuiChanged;

    private Panel panel;
    private List<Widget<?>> row = new ArrayList<>();

    public ScreenModuleGuiBuilder(Minecraft mc, Screen gui, CompoundNBT currentData, IModuleGuiChanged moduleGuiChanged) {
        this.gui = gui;
        this.mc = mc;
        this.moduleGuiChanged = moduleGuiChanged;
        this.currentData = currentData;
        panel = new Panel(mc, gui).setLayout(new VerticalLayout().setVerticalMargin(3).setSpacing(1));
    }

    @Override
    public CompoundNBT getCurrentData() {
        return currentData;
    }

    @Override
    public World getWorld() {
        return mc.player.getEntityWorld();
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
        Label label = new Label(mc, gui).setText(text).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        row.add(label);
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder text(final String tagname, String... tooltip) {
        TextField textField = new TextField(mc, gui).setDesiredHeight(15).setTooltips(tooltip).addTextEvent((parent, newText) -> {
            currentData.putString(tagname, newText);
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
            currentData.putInt(tagname, value);
            moduleGuiChanged.updateData();
        });
        row.add(textField);
        if (currentData != null) {
            if (currentData.contains(tagname)) {
                int dd = currentData.getInt(tagname);
                textField.setText(Integer.toString(dd));
            }
        }
        return this;
    }

    @Override
    public ScreenModuleGuiBuilder toggle(final String tagname, String label, String... tooltip) {
        final ToggleButton toggleButton = new ToggleButton(mc, gui).setText(label).setTooltips(tooltip).setDesiredHeight(14).setCheckMarker(true);
        toggleButton.addButtonEvent(parent -> {
            currentData.putBoolean(tagname, toggleButton.isPressed());
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
            currentData.putBoolean(tagname, !toggleButton.isPressed());
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
        ColorSelector colorSelector = new ColorSelector(mc, gui).setTooltips(tooltip)
                .setDesiredWidth(20).setDesiredHeight(14).addChoiceEvent((parent, newColor) -> {
                    currentData.putInt(tagname, newColor);
                    moduleGuiChanged.updateData();
                });
        row.add(colorSelector);
        if (currentData != null) {
            int currentColor = currentData.getInt(tagname);
            if (currentColor != 0) {
                colorSelector.setCurrentColor(currentColor);
            }
        }
        return this;
    }

    @Override
    public IModuleGuiBuilder choices(String tagname, String tooltip, String... choices) {
        ChoiceLabel choiceLabel = new ChoiceLabel(mc, gui).setTooltips(tooltip)
                .setDesiredWidth(50).setDesiredHeight(14);
        for (String s : choices) {
            choiceLabel.addChoices(s);
        }
        choiceLabel.addChoiceEvent((parent, newChoice) -> {
            currentData.putString(tagname, newChoice);
            moduleGuiChanged.updateData();
        });
        row.add(choiceLabel);
        if (currentData != null) {
            String currentChoice = currentData.getString(tagname);
            if (!currentChoice.isEmpty()) {
                choiceLabel.setChoice(currentChoice);
            }
        }
        return this;
    }

    @Override
    public IModuleGuiBuilder choices(String tagname, Choice... choices) {
        ChoiceLabel choiceLabel = new ChoiceLabel(mc, gui)
                .setDesiredWidth(50).setDesiredHeight(14);
        Map<String, Integer> choicesMap = new HashMap<>(choices.length);
        for (int i = 0; i < choices.length; ++i) {
            Choice c = choices[i];
            String name = c.getName();
            choicesMap.put(name, i);
            choiceLabel.addChoices(name);
            choiceLabel.setChoiceTooltip(name, c.getTooltips());
        }
        choiceLabel.addChoiceEvent((parent, newChoice) -> {
            currentData.putInt(tagname, choicesMap.get(newChoice));
            moduleGuiChanged.updateData();
        });
        row.add(choiceLabel);
        if (currentData != null) {
            int currentChoice = currentData.getInt(tagname);
            if (currentChoice < choices.length && currentChoice >= 0) {
                choiceLabel.setChoice(choices[currentChoice].getName());
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
        if (currentData.contains(tagnamePos + "x")) {
            int dim;
            if (currentData.contains(tagnamePos + "dim")) {
                dim = currentData.getInt(tagnamePos + "dim");
            } else {
                // For compatibility reasons.
                dim = currentData.getInt("dim");
            }
            World world = getWorld();
            if (dim == world.getDimension().getType().getId()) {
                int x = currentData.getInt(tagnamePos+"x");
                int y = currentData.getInt(tagnamePos+"y");
                int z = currentData.getInt(tagnamePos+"z");
                monitoring = currentData.getString(tagnamePos+"name");
                Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                row.add(new BlockRender(mc, gui).setRenderItem(block).setDesiredWidth(20));
                row.add(new Label(mc, gui).setText(x + "," + y + "," + z).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(150));
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
        ItemStack stack = ItemStack.EMPTY;
        if (currentData.contains(tagname)) {
            stack = ItemStack.read(currentData.getCompound(tagname));
        }

        BlockRender blockRender = new BlockRender(mc, gui).setRenderItem(stack).setDesiredWidth(18).setDesiredHeight(18).setFilledRectThickness(1).setFilledBackground(0xff555555);
        row.add(blockRender);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget<?> widget) {
                ItemStack holding = Minecraft.getInstance().player.inventory.getItemStack();
                if (holding.isEmpty()) {
                    currentData.remove(tagname);
                    blockRender.setRenderItem(null);
                } else {
                    ItemStack copy = holding.copy();
                    copy.setCount(1);
                    blockRender.setRenderItem(copy);
                    CompoundNBT tc = new CompoundNBT();
                    copy.write(tc);
                    currentData.put(tagname, tc);
                }
                moduleGuiChanged.updateData();
            }

            @Override
            public void doubleClick(Widget<?> widget) {

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
            for (Widget<?> widget : row) {
                rowPanel.addChild(widget);
            }
            panel.addChild(rowPanel);
            row.clear();
        }

        return this;
    }

    private static ChoiceLabel setupFormatCombo(Minecraft mc, Screen gui, String tagname, final CompoundNBT currentData, final IModuleGuiChanged moduleGuiChanged) {
        final String modeFull = FormatStyle.MODE_FULL.getName();
        final String modeCompact = FormatStyle.MODE_COMPACT.getName();
        final String modeCommas = FormatStyle.MODE_COMMAS.getName();
        final ChoiceLabel modeButton = new ChoiceLabel(mc, gui).setDesiredWidth(58).setDesiredHeight(14).addChoices(modeFull, modeCompact, modeCommas).
                setChoiceTooltip(modeFull, "Full format: 3123555").
                setChoiceTooltip(modeCompact, "Compact format: 3.1M").
                setChoiceTooltip(modeCommas, "Comma format: 3,123,555").
                addChoiceEvent((parent, newChoice) -> {
//                    currentData.putInt(tagname, FormatStyle.getStyle(newChoice).ordinal());
                    currentData.putString(tagname, FormatStyle.getStyle(newChoice).getName());
                    moduleGuiChanged.updateData();
                });

        //FormatStyle currentFormat = FormatStyle.values()[currentData.getInt(tagname)];
        FormatStyle currentFormat = FormatStyle.getStyle(currentData.getString(tagname));
        modeButton.setChoice(currentFormat.getName());

        return modeButton;
    }

    private static ChoiceLabel setupModeCombo(Minecraft mc, Screen gui, final String componentName, final CompoundNBT currentData, final IModuleGuiChanged moduleGuiChanged) {
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
                        currentData.putBoolean("showdiff", false);
                        currentData.putBoolean("showpct", false);
                        currentData.putBoolean("hidetext", false);
                    } else if (modePertick.equals(newChoice)) {
                        currentData.putBoolean("showdiff", true);
                        currentData.putBoolean("showpct", false);
                        currentData.putBoolean("hidetext", false);
                    } else if (modePct.equals(newChoice)) {
                        currentData.putBoolean("showdiff", false);
                        currentData.putBoolean("showpct", true);
                        currentData.putBoolean("hidetext", false);
                    } else {
                        currentData.putBoolean("showdiff", false);
                        currentData.putBoolean("showpct", false);
                        currentData.putBoolean("hidetext", true);
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
