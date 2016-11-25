package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.TextEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.NamedEnum;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiSensor extends GenericGuiContainer<SensorTileEntity> {
    public static final int SENSOR_WIDTH = 180;
    public static final int SENSOR_HEIGHT = 152;

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String META_MATCH = "Match";
    public static final String META_IGNORE = "Ignore";

    private TextField numberField;
    private ChoiceLabel typeLabel;
    private ChoiceLabel areaLabel;
    private ChoiceLabel groupLabel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/sensor.png");

    public GuiSensor(SensorTileEntity sensorTileEntity, SensorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, sensorTileEntity, container, RFTools.GUI_MANUAL_MAIN, "sensor");
        xSize = SENSOR_WIDTH;
        ySize = SENSOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        numberField = new TextField(mc, this).setTooltips("Set a number specific to the type of sensor")
                .setLayoutHint(new PositionalLayout.PositionalHint(60, 51, 80, 14))
                .addTextEvent((parent, newText) -> setNumber());
        int number = tileEntity.getNumber();
        numberField.setText(String.valueOf(number));

        typeLabel = new ChoiceLabel(mc, this);
        for (SensorType sensorType : SensorType.values()) {
            typeLabel.addChoices(sensorType.getName());
            typeLabel.setChoiceTooltip(sensorType.getName(), sensorType.getDescription());
        }
        typeLabel.setLayoutHint(new PositionalLayout.PositionalHint(60, 3, 80, 14));
        typeLabel.setChoice(tileEntity.getSensorType().getName());
        typeLabel.addChoiceEvent((parent, newChoice) -> setType());

        areaLabel = new ChoiceLabel(mc, this);
        for (AreaType areaType : AreaType.values()) {
            areaLabel.addChoices(areaType.getName());
            areaLabel.setChoiceTooltip(areaType.getName(), areaType.getDescription());
        }
        areaLabel.setLayoutHint(new PositionalLayout.PositionalHint(60, 19, 80, 14));
        areaLabel.setChoice(tileEntity.getAreaType().getName());
        areaLabel.addChoiceEvent((parent, newChoice) -> setArea());

        groupLabel = new ChoiceLabel(mc, this);
        for (GroupType groupType : GroupType.values()) {
            groupLabel.addChoices(groupType.getName());
            groupLabel.setChoiceTooltip(groupType.getName(), groupType.getDescription());
        }
        groupLabel.setLayoutHint(new PositionalLayout.PositionalHint(60, 35, 80, 14));
        groupLabel.setChoice(tileEntity.getGroupType().getName());
        groupLabel.addChoiceEvent((parent, newChoice) -> setGroup());

        toplevel
                .addChild(new Label(mc, this).setText("Type:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 3, 50, 14)))
                .addChild(areaLabel)
                .addChild(new Label(mc, this).setText("Area:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 19, 50, 14)))
                .addChild(typeLabel)
                .addChild(new Label(mc, this).setText("Group:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 35, 50, 14)))
                .addChild(groupLabel)
                .addChild(new Label(mc, this).setText("Number:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 51, 50, 14)))
                .addChild(numberField);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void setArea() {
        AreaType areaType = NamedEnum.getEnumByName(areaLabel.getCurrentChoice(), AreaType.values());
        tileEntity.setAreaType(areaType);
        sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETAREA, new Argument("type", areaType.ordinal()));
    }

    private void setType() {
        SensorType sensorType = getSensorType();
        tileEntity.setSensorType(sensorType);
        sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETTYPE, new Argument("type", sensorType.ordinal()));
    }

    private SensorType getSensorType() {
        return NamedEnum.getEnumByName(typeLabel.getCurrentChoice(), SensorType.values());
    }

    private void setGroup() {
        GroupType groupType = NamedEnum.getEnumByName(groupLabel.getCurrentChoice(), GroupType.values());
        tileEntity.setGroupType(groupType);
        sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETGROUP, new Argument("type", groupType.ordinal()));
    }

    private void setNumber() {
        String d = numberField.getText();
        int number;
        try {
            number = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            number = 1;
        }
        tileEntity.setNumber(number);
        sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETNUMBER, new Argument("number", number));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        SensorType sensorType = getSensorType();
        numberField.setEnabled(sensorType.isSupportsNumber());
        groupLabel.setEnabled(sensorType.isSupportsGroup());
        drawWindow();
    }
}
