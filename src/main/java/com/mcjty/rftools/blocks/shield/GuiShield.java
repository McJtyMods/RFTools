package com.mcjty.rftools.blocks.shield;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
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
import com.mcjty.rftools.network.PacketServerCommand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiShield extends GuiContainer {
    public static final int SHIELD_WIDTH = 256;
    public static final int SHIELD_HEIGHT = 224;

    public static final String ACTION_PASS = "Pass";
    public static final String ACTION_SOLID = "Solid";
    public static final String ACTION_DAMAGE = "Damage";

    private Window window;
    private EnergyBar energyBar;
    private ChoiceLabel visibilityOptions;
    private ChoiceLabel actionOptions;
    private ChoiceLabel typeOptions;
    private ImageChoiceLabel redstoneMode;
    private WidgetList filterList;
    private TextField player;
    private Button addFilter;
    private Button delFilter;
    private Button upFilter;
    private Button downFilter;

    private final ShieldTileEntity shieldTileEntity;

    // A copy of the filterList we're currently showing.
    private List<ShieldFilter> filters = null;
    private int listDirty = 0;

    private static List<ShieldFilter> fromServer_filters = new ArrayList<ShieldFilter>();
    public static void storeFiltersForClient(List<ShieldFilter> filters) {
        fromServer_filters = new ArrayList<ShieldFilter>(filters);
    }

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shieldprojector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiShield(ShieldTileEntity shieldTileEntity, ShieldContainer container) {
        super(container);
        this.shieldTileEntity = shieldTileEntity;
        shieldTileEntity.setOldRF(-1);
        shieldTileEntity.setCurrentRF(shieldTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SHIELD_WIDTH;
        ySize = SHIELD_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = shieldTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(shieldTileEntity.getCurrentRF());

        initVisibilityMode();
        initActionOptions();
        initTypeOptions();
        initRedstoneMode();

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
                addChild(typeOptions).addChild(player).addChild(addFilter).addChild(delFilter).addChild(upFilter).addChild(downFilter);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        listDirty = 0;
        requestFilters();
    }

    private void selectFilter() {
        int selected = filterList.getSelected();
        if (selected != -1) {
            ShieldFilter shieldFilter = filters.get(selected);
            switch (shieldFilter.getAction()) {
                case ShieldFilter.ACTION_SOLID:
                    actionOptions.setChoice(ACTION_SOLID);
                    break;
                case ShieldFilter.ACTION_PASS:
                    actionOptions.setChoice(ACTION_PASS);
                    break;
                case ShieldFilter.ACTION_DAMAGE:
                    actionOptions.setChoice(ACTION_DAMAGE);
                    break;
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
        PacketHandler.INSTANCE.sendToServer(new PacketGetFilters(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord));
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
            panel.addChild(new Label(mc, this).setText(n).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            String actionName;
            if (filter.getAction() == ShieldFilter.ACTION_PASS) {
                actionName = ACTION_PASS;
            } else if (filter.getAction() == ShieldFilter.ACTION_SOLID) {
                actionName = ACTION_SOLID;
            } else {
                actionName = ACTION_DAMAGE;
            }
            panel.addChild(new Label(mc, this).setText(actionName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
            filterList.addChild(panel);
        }
    }

    private void moveFilterUp() {
        int selected = filterList.getSelected();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_UPFILTER,
                new Argument("selected", selected)));
        listDirty = 0;
    }

    private void moveFilterDown() {
        int selected = filterList.getSelected();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_DOWNFILTER,
                new Argument("selected", selected)));
        listDirty = 0;
    }

    private void addNewFilter() {
        String actionName = actionOptions.getCurrentChoice();
        int action;
        if (ACTION_PASS.equals(actionName)) {
            action = ShieldFilter.ACTION_PASS;
        } else if (ACTION_SOLID.equals(actionName)) {
            action = ShieldFilter.ACTION_SOLID;
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

        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_ADDFILTER,
                new Argument("action", action), new Argument("type", type), new Argument("player", playerName),
                new Argument("selected", selected)));
        listDirty = 0;
    }

    private void removeSelectedFilter() {
        int selected = filterList.getSelected();
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_DELFILTER,
                new Argument("selected", selected)));
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
        redstoneMode.setCurrentChoice(shieldTileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        shieldTileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoice()]);
        RedstoneMode rsMode = RedstoneMode.values()[redstoneMode.getCurrentChoice()];
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_RSMODE,
                new Argument("rs", rsMode.getDescription())));
    }

    private void initVisibilityMode() {
        visibilityOptions = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(31, 161, 48, 14));
        for (ShieldRenderingMode m : ShieldRenderingMode.values()) {
            visibilityOptions.addChoices(m.getDescription());
        }
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_INVISIBLE.getDescription(), "Shield is completely invisible");
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_SHIELD.getDescription(), "Default shield texture");
        visibilityOptions.setChoiceTooltip(ShieldRenderingMode.MODE_SOLID.getDescription(), "Use the texture from the supplied block");
        visibilityOptions.setChoice(shieldTileEntity.getShieldRenderingMode().getDescription());
        visibilityOptions.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                changeVisibilityMode();
            }
        });
    }

    private void initActionOptions() {
        actionOptions = new ChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(170, 12, 80, 14));
        actionOptions.addChoices(ACTION_PASS, ACTION_SOLID, ACTION_DAMAGE);
        actionOptions.setChoiceTooltip(ACTION_PASS, "Entity that matches this filter", "can pass through");
        actionOptions.setChoiceTooltip(ACTION_SOLID, "Entity that matches this filter", "cannot pass");
        actionOptions.setChoiceTooltip(ACTION_DAMAGE, "Entity that matches this filter", "gets damage");
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

    private void changeVisibilityMode() {
        ShieldRenderingMode newMode = ShieldRenderingMode.getMode(visibilityOptions.getCurrentChoice());
        shieldTileEntity.setShieldRenderingMode(newMode);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_SHIELDVISMODE,
                new Argument("mode", newMode.getDescription())));
    }

    private void applyCamoToShield() {
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(shieldTileEntity.xCoord, shieldTileEntity.yCoord, shieldTileEntity.zCoord,
                ShieldTileEntity.CMD_APPLYCAMO));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }

    private void enableButtons() {
        int sel = filterList.getSelected();
        int cnt = filterList.getMaximum();
        delFilter.setEnabled(sel != -1);
        upFilter.setEnabled(sel > 0);
        downFilter.setEnabled(sel < cnt-1 && sel != -1);
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
        int currentRF = shieldTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }
}