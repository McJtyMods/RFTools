package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.shapes.BeaconType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static mcjty.rftools.blocks.shaper.LocatorTileEntity.*;

public class GuiLocator extends GenericGuiContainer<LocatorTileEntity, GenericContainer> {

    private static final int LOCATOR_WIDTH = 256;
    private static final int LOCATOR_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/locator.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;

    private ColorChoiceLabel hostile;
    private ColorChoiceLabel passive;
    private ColorChoiceLabel player;
    private ToggleButton hostileBeacon;
    private ToggleButton passiveBeacon;
    private ToggleButton playerBeacon;

    private TextField filter;

    private ColorChoiceLabel energy;
    private ToggleButton energyBeacon;
    private TextField minEnergy;
    private TextField maxEnergy;

    private Label energyLabel;

    public static int energyConsumption = 0;

    public GuiLocator(LocatorTileEntity tileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, tileEntity, container, inventory, GuiProxy.GUI_MANUAL_SHAPE, "locator");

        xSize = LOCATOR_WIDTH;
        ySize = LOCATOR_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        energyBar = new EnergyBar(minecraft, this).setHorizontal().setLayoutHint(28, 10, 70, 10).setShowText(false);
        toplevel.addChild(energyBar);

        ImageChoiceLabel redstoneMode = initRedstoneMode();
        toplevel.addChild(redstoneMode);

        hostile = new ColorChoiceLabel(minecraft, this);
        hostileBeacon = new ToggleButton(minecraft, this);
        addBeaconSetting(toplevel, hostile, hostileBeacon, 30, "Hostile");
        hostile.setCurrentColor(tileEntity.getHostileType().getColor());
        hostileBeacon.setPressed(tileEntity.isHostileBeacon());

        passive = new ColorChoiceLabel(minecraft, this);
        passiveBeacon = new ToggleButton(minecraft, this);
        addBeaconSetting(toplevel, passive, passiveBeacon, 46, "Passive");
        passive.setCurrentColor(tileEntity.getPassiveType().getColor());
        passiveBeacon.setPressed(tileEntity.isPassiveBeacon());

        player = new ColorChoiceLabel(minecraft, this);
        playerBeacon = new ToggleButton(minecraft, this);
        addBeaconSetting(toplevel, player, playerBeacon, 62, "Player");
        player.setCurrentColor(tileEntity.getPlayerType().getColor());
        playerBeacon.setPressed(tileEntity.isPlayerBeacon());

        toplevel.addChild(new Label(minecraft, this)
                .setText("Filter")
                .setLayoutHint(8, 82, 40, 14));
        filter = new TextField(minecraft, this);
        filter.setLayoutHint(50, 82, 90, 14);
        filter.setText(tileEntity.getFilter());
        filter.addTextEvent((parent, newText) -> update());
        toplevel.addChild(filter);

        energy = new ColorChoiceLabel(minecraft, this);
        energyBeacon = new ToggleButton(minecraft, this);
        addBeaconSetting(toplevel, energy, energyBeacon, 98, "Energy");
        energy.setCurrentColor(tileEntity.getEnergyType().getColor());
        energyBeacon.setPressed(tileEntity.isEnergyBeacon());

        toplevel.addChild(new Label(minecraft, this).setText("<").setLayoutHint(153, 98, 10, 14));
        minEnergy = new TextField(minecraft, this).setLayoutHint(162, 98, 25, 14);
        minEnergy.setText(tileEntity.getMinEnergy() == null ? "" : Integer.toString(tileEntity.getMinEnergy()));
        minEnergy.addTextEvent((parent, newText) -> update());
        toplevel.addChild(new Label(minecraft, this).setText("%").setLayoutHint(187, 98, 10, 14));
        toplevel.addChild(minEnergy);
        toplevel.addChild(new Label(minecraft, this).setText(">").setLayoutHint(205, 98, 10, 14));
        maxEnergy = new TextField(minecraft, this).setLayoutHint(214, 98, 25, 14);
        maxEnergy.setText(tileEntity.getMaxEnergy() == null ? "" : Integer.toString(tileEntity.getMaxEnergy()));
        maxEnergy.addTextEvent((parent, newText) -> update());
        toplevel.addChild(maxEnergy);
        toplevel.addChild(new Label(minecraft, this).setText("%").setLayoutHint(238, 98, 10, 14));

        toplevel.addChild(new Label(minecraft, this)
                .setColor(0x993300)
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                .setText("RF per scan (every " + ScannerConfiguration.ticksPerLocatorScan + " ticks):")
                .setLayoutHint(8, 186, 156, 14));
        energyLabel = new Label(minecraft, this).setText("");
        energyLabel.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        energyLabel.setLayoutHint(8, 200, 156, 14);
        toplevel.addChild(energyLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        energyConsumption = 0;

        window.bind(RFToolsMessages.INSTANCE, "redstone", tileEntity, GenericTileEntity.VALUE_RSMODE.getName());
    }

    private static final Map<Integer, BeaconType> COLOR_TO_TYPE = new HashMap<>();

    private void addBeaconSetting(Panel toplevel, ColorChoiceLabel choice, ToggleButton toggle, int y, String label) {
        toplevel.addChild(new Label(minecraft, this).setText(label).setLayoutHint(new PositionalLayout.PositionalHint(8, y, 40, 14)));
        choice.setLayoutHint(new PositionalLayout.PositionalHint(50, y, 30, 14));
        for (BeaconType type : BeaconType.VALUES) {
            choice.addColors(type.getColor());
            COLOR_TO_TYPE.put(type.getColor(), type);
        }
        choice.addChoiceEvent((parent, newColor) -> {
            update();
        });
        toplevel.addChild(choice);
        toggle.setCheckMarker(true).setText("Beacon");
        toggle.setLayoutHint(new PositionalLayout.PositionalHint(90, y, 60, 14));
        toggle.addButtonEvent(parent -> update());
        toplevel.addChild(toggle);
    }

    private void update() {
        TypedMap.Builder builder = TypedMap.builder();
        builder.put(PARAM_HOSTILE_TYPE, COLOR_TO_TYPE.get(hostile.getCurrentColor()).getCode());
        builder.put(PARAM_PASSIVE_TYPE, COLOR_TO_TYPE.get(passive.getCurrentColor()).getCode());
        builder.put(PARAM_PLAYER_TYPE, COLOR_TO_TYPE.get(player.getCurrentColor()).getCode());
        builder.put(PARAM_ENERGY_TYPE, COLOR_TO_TYPE.get(energy.getCurrentColor()).getCode());
        builder.put(PARAM_HOSTILE_BEACON, hostileBeacon.isPressed());
        builder.put(PARAM_PASSIVE_BEACON, passiveBeacon.isPressed());
        builder.put(PARAM_PLAYER_BEACON, playerBeacon.isPressed());
        builder.put(PARAM_ENERGY_BEACON, energyBeacon.isPressed());
        builder.put(PARAM_FILTER, filter.getText());
        if (!minEnergy.getText().trim().isEmpty()) {
            try {
                builder.put(PARAM_MIN_ENERGY, Integer.parseInt(minEnergy.getText()));
            } catch (NumberFormatException e) {
            }
        }
        if (!maxEnergy.getText().trim().isEmpty()) {
            try {
                builder.put(PARAM_MAX_ENERGY, Integer.parseInt(maxEnergy.getText()));
            } catch (NumberFormatException e) {
            }
        }

        sendServerCommand(RFToolsMessages.INSTANCE, LocatorTileEntity.CMD_SETTINGS, builder.build());
    }

    private ImageChoiceLabel initRedstoneMode() {
        ImageChoiceLabel redstoneMode = new ImageChoiceLabel(minecraft, this).
                setName("redstone").
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(8, 10, 16, 16);
        return redstoneMode;
    }

    static int cnt = 10;

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {

        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });
        cnt--;
        if (cnt < 0) {
            cnt = 10;
            sendServerCommand(RFTools.MODID, CommandHandler.CMD_REQUEST_LOCATOR_ENERGY,
                    TypedMap.builder().put(CommandHandler.PARAM_POS, tileEntity.getPos()).build());
        }
        energyLabel.setText(energyConsumption + " RF");
    }

}
