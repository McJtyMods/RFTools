package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.ColorChoiceEvent;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.RedstoneMode;
import com.mcjty.rftools.blocks.shield.filters.*;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiShield extends GenericGuiContainer<ShieldTEBase> {
    public static final int SHIELD_WIDTH = 256;
    public static final int SHIELD_HEIGHT = 224;

    public static final String ACTION_PASS = "Pass";
    public static final String ACTION_SOLID = "Solid";
    public static final String ACTION_DAMAGE = "Damage";
    public static final String ACTION_SOLIDDAMAGE = "SolDmg";

    public static final String DAMAGETYPE_GENERIC = DamageTypeMode.DAMAGETYPE_GENERIC.getDescription();
    public static final String DAMAGETYPE_PLAYER = DamageTypeMode.DAMAGETYPE_PLAYER.getDescription();

    private EnergyBar energyBar;
    private ChoiceLabel visibilityOptions;
    private ChoiceLabel actionOptions;
    private ChoiceLabel typeOptions;
    private ChoiceLabel damageType;
    private ImageChoiceLabel redstoneMode;
    private WidgetList filterList;
    private TextField player;
    private Button addFilter;
    private Button delFilter;
    private Button upFilter;
    private Button downFilter;

    // A copy of the filterList we're currently showing.
    private List<ShieldFilter> filters = null;
    private int listDirty = 0;

    private static List<ShieldFilter> fromServer_filters = new ArrayList<ShieldFilter>();
    public static void storeFiltersForClient(List<ShieldFilter> filters) {
        fromServer_filters = new ArrayList<ShieldFilter>(filters);
    }

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shieldprojector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiShield(ShieldTEBase shieldTileEntity, ShieldContainer container) {
        super(shieldTileEntity, container);
        shieldTileEntity.setCurrentRF(shieldTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SHIELD_WIDTH;
        ySize = SHIELD_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        initVisibilityMode();
        initActionOptions();
        initTypeOptions();
        initRedstoneMode();
        initDamageType();

        filterList = new WidgetList(mc, this).
                setFilledRectThickness(1).setLayoutHint(new PositionalLayout.PositionalHint(12, 12, 140, 115)).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                selectFilter();
            }
        });
        Slider filterSlider = new Slider(mc, this).setVertical().setScrollable(filterList).setLayoutHint(new PositionalLayout.PositionalHint(154, 12, 12, 115));

        Button applyCamo = new Button(mc, this).setText("Set").setTooltips("Set the camouflage block").
                setLayoutHint(new PositionalLayout.PositionalHint(51, 142, 28, 16)).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                applyCamoToShield();
            }
        });
        ColorChoiceLabel colorSelector = new ColorChoiceLabel(mc, this).addColors(0x96ffc8, 0x4698ff, 0xff6030, 0x55a0a0, 0xa055a0, 0xffffff).
                setLayoutHint(new PositionalLayout.PositionalHint(51, 186, 28, 16)).
                addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                sendServerCommand(ShieldTEBase.CMD_SETCOLOR, new Argument("color", newColor));
            }
        });
        colorSelector.setCurrentColor(tileEntity.getShieldColor());

        player = new TextField(mc, this).setTooltips("Optional player name").setLayoutHint(new PositionalLayout.PositionalHint(170, 44, 80, 14));

        addFilter = new Button(mc, this).setText("Add").setTooltips("Delete selected filter").setLayoutHint(new PositionalLayout.PositionalHint(170, 64, 36, 12)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        addNewFilter();
                    }
                });
        delFilter = new Button(mc, this).setText("Del").setTooltips("Delete selected filter").setLayoutHint(new PositionalLayout.PositionalHint(214, 64, 36, 12)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        removeSelectedFilter();
                    }
                });
        upFilter = new Button(mc, this).setText("Up").setTooltips("Move filter up").setLayoutHint(new PositionalLayout.PositionalHint(170, 78, 36, 12)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        moveFilterUp();
                    }
                });
        downFilter = new Button(mc, this).setText("Down").setTooltips("Move filter down").setLayoutHint(new PositionalLayout.PositionalHint(214, 78, 36, 12)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        moveFilterDown();
                    }
                });

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(visibilityOptions).addChild(applyCamo).addChild(redstoneMode).addChild(filterList).addChild(filterSlider).addChild(actionOptions).
                addChild(typeOptions).addChild(player).addChild(addFilter).addChild(delFilter).addChild(upFilter).addChild(downFilter).addChild(damageType).
                addChild(colorSelector);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        listDirty = 0;
        requestFilters();
        tileEntity.requestRfFromServer();
    }

    private void selectFilter() {
        int selected = filterList.getSelected();
        if (selected != -1) {
            ShieldFilter shieldFilter = filters.get(selected);
            boolean solid = (shieldFilter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
            boolean damage = (shieldFilter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0;
            if (solid && damage) {
                actionOptions.setChoice(ACTION_SOLIDDAMAGE);
            } else if (solid) {
                actionOptions.setChoice(ACTION_SOLID);
            } else if (damage) {
                actionOptions.setChoice(ACTION_DAMAGE);
            } else {
                actionOptions.setChoice(ACTION_PASS);
            }
            String type = shieldFilter.getFilterName();
            if (DefaultFilter.DEFAULT.equals(type)) {
                typeOptions.setChoice("All");
            } else if (AnimalFilter.ANIMAL.equals(type)) {
                typeOptions.setChoice("Passive");
            } else if (HostileFilter.HOSTILE.equals(type)) {
                typeOptions.setChoice("Hostile");
            } else if (PlayerFilter.PLAYER.equals(type)) {
                typeOptions.setChoice("Player");
            } else if (ItemFilter.ITEM.equals(type)) {
                typeOptions.setChoice("Item");
            }
            if (shieldFilter instanceof PlayerFilter) {
                player.setText(((PlayerFilter)shieldFilter).getName());
            } else {
                player.setText("");
            }
        }
    }

    private void requestFilters() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetFilters(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestFilters();
            listDirty = 20;
        }
    }

    private void populateFilters() {
        List<ShieldFilter> newFilters = new ArrayList<ShieldFilter>(fromServer_filters);
        if (newFilters.equals(filters)) {
            return;
        }

        filters = new ArrayList<ShieldFilter>(newFilters);
        filterList.removeChildren();
        for (ShieldFilter filter : filters) {
            String n;
            if ("player".equals(filter.getFilterName())) {
                PlayerFilter playerFilter = (PlayerFilter) filter;
                if (playerFilter.getName() == null || playerFilter.getName().isEmpty()) {
                    n = "players";
                } else {
                    n = "player " + playerFilter.getName();
                }
            } else {
                n = filter.getFilterName();
            }
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new Label(mc, this).setText(n).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(85));
            String actionName;
            boolean solid = (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
            boolean damage = (filter.getAction() & ShieldFilter.ACTION_DAMAGE) != 0;
            if (solid && damage) {
                actionName = ACTION_SOLIDDAMAGE;
            } else if (solid) {
                actionName = ACTION_SOLID;
            } else if (damage) {
                actionName = ACTION_DAMAGE;
            } else {
                actionName = ACTION_PASS;
            }
            panel.addChild(new Label(mc, this).setText(actionName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
            filterList.addChild(panel);
        }
    }

    private void moveFilterUp() {
        sendServerCommand(ShieldTEBase.CMD_UPFILTER, new Argument("selected", filterList.getSelected()));
        listDirty = 0;
    }

    private void moveFilterDown() {
        sendServerCommand(ShieldTEBase.CMD_DOWNFILTER, new Argument("selected", filterList.getSelected()));
        listDirty = 0;
    }

    private void addNewFilter() {
        String actionName = actionOptions.getCurrentChoice();
        int action;
        if (ACTION_PASS.equals(actionName)) {
            action = ShieldFilter.ACTION_PASS;
        } else if (ACTION_SOLID.equals(actionName)) {
            action = ShieldFilter.ACTION_SOLID;
        } else if (ACTION_SOLIDDAMAGE.equals(actionName)) {
            action = ShieldFilter.ACTION_DAMAGE + ShieldFilter.ACTION_SOLID;
        } else {
            action = ShieldFilter.ACTION_DAMAGE;
        }

        String filterName = typeOptions.getCurrentChoice();
        String type;
        if ("All".equals(filterName)) {
            type = DefaultFilter.DEFAULT;
        } else if ("Passive".equals(filterName)) {
            type = AnimalFilter.ANIMAL;
        } else if ("Hostile".equals(filterName)) {
            type = HostileFilter.HOSTILE;
        } else if ("Item".equals(filterName)) {
            type = ItemFilter.ITEM;
        } else {
            type = PlayerFilter.PLAYER;
        }

        String playerName = player.getText();
        int selected = filterList.getSelected();

        sendServerCommand(ShieldTEBase.CMD_ADDFILTER,
                new Argument("action", action), new Argument("type", type), new Argument("player", playerName),
                new Argument("selected", selected));
        listDirty = 0;
    }

    private void removeSelectedFilter() {
        sendServerCommand(ShieldTEBase.CMD_DELFILTER,
                new Argument("selected", filterList.getSelected()));
        listDirty = 0;
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(31, 186, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoice()]);
        sendServerCommand(ShieldTEBase.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoice()].getDescription()));
    }

    private void initVisibilityMode() {
        visibilityOptions = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(31, 161, 48, 14));
        for (ShieldRenderingMode m : ShieldRenderingMode.values()) {
            visibilityOptions.addChoices(m.getDescription());
        }
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_INVISIBLE.getDescription(), "Shield is completely invisible");
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_SHIELD.getDescription(), "Default shield texture");
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_SOLID.getDescription(), "Use the texture from the supplied block");
        visibilityOptions.setChoice(tileEntity.getShieldRenderingMode().getDescription());
        visibilityOptions.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeVisibilityMode();
            }
        });
    }

    private void initActionOptions() {
        actionOptions = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(170, 12, 80, 14));
        actionOptions.addChoices(ACTION_PASS, ACTION_SOLID, ACTION_DAMAGE, ACTION_SOLIDDAMAGE);
        actionOptions.setChoiceTooltip(ACTION_PASS, "Entity that matches this filter", "can pass through");
        actionOptions.setChoiceTooltip(ACTION_SOLID, "Entity that matches this filter", "cannot pass");
        actionOptions.setChoiceTooltip(ACTION_DAMAGE, "Entity that matches this filter", "can pass but gets damage");
        actionOptions.setChoiceTooltip(ACTION_SOLIDDAMAGE, "Entity that matches this filter", "cannot pass and gets damage");
    }

    private void initTypeOptions() {
        typeOptions = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(170, 28, 80, 14));
        typeOptions.addChoices("All", "Passive", "Hostile", "Item", "Player");
        typeOptions.setChoiceTooltip("All", "Matches everything");
        typeOptions.setChoiceTooltip("Passive", "Matches passive mobs");
        typeOptions.setChoiceTooltip("Hostile", "Matches hostile mobs");
        typeOptions.setChoiceTooltip("Item", "Matches items");
        typeOptions.setChoiceTooltip("Player", "Matches players", "(optionaly named)");
    }

    private void initDamageType() {
        damageType = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(170, 100, 80, 14));
        damageType.addChoices(DAMAGETYPE_GENERIC, DAMAGETYPE_PLAYER);
        damageType.setChoiceTooltip(DAMAGETYPE_GENERIC, "Generic damage type");
        damageType.setChoiceTooltip(DAMAGETYPE_PLAYER, "Damage as done by a player");
        damageType.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeDamageType();
            }
        });
        damageType.setChoice(tileEntity.getDamageMode().getDescription());
    }

    private void changeDamageType() {
        tileEntity.setDamageMode(DamageTypeMode.getMode(damageType.getCurrentChoice()));
        sendServerCommand(ShieldTEBase.CMD_DAMAGEMODE, new Argument("mode", DamageTypeMode.getMode(damageType.getCurrentChoice()).getDescription()));

    }

    private void changeVisibilityMode() {
        ShieldRenderingMode newMode = ShieldRenderingMode.getMode(visibilityOptions.getCurrentChoice());
        tileEntity.setShieldRenderingMode(newMode);
        sendServerCommand(ShieldTEBase.CMD_SHIELDVISMODE,
                new Argument("mode", newMode.getDescription()));
    }

    private void applyCamoToShield() {
        ItemStack stack = tileEntity.getStackInSlot(0);

        int pass = 0;
        if (stack != null) {
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block != null) {
                pass = block.getRenderBlockPass();
            }
        }
        sendServerCommand(ShieldTEBase.CMD_APPLYCAMO, new Argument("pass", pass));
    }

    private void enableButtons() {
        int sel = filterList.getSelected();
        int cnt = filterList.getMaximum();
        delFilter.setEnabled(sel != -1 && cnt > 0);
        upFilter.setEnabled(sel > 0 && cnt > 0);
        downFilter.setEnabled(sel < cnt-1 && sel != -1 && cnt > 0);
        if (sel == -1) {
            addFilter.setText("Add");
        } else {
            addFilter.setText("Insert");
        }
        player.setEnabled("Player".equals(typeOptions.getCurrentChoice()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        requestListsIfNeeded();
        populateFilters();
        enableButtons();
        window.draw();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer();
    }
}