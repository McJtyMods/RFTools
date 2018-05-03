package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.NamedEnum;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;

public class GuiSensor extends GenericGuiContainer<SensorTileEntity> {
    public static final int SENSOR_WIDTH = 180;
    public static final int SENSOR_HEIGHT = 152;

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String META_MATCH = "Match";
    public static final String META_IGNORE = "Ignore";

    private ChoiceLabel typeLabel;

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

        TextField numberField = new TextField(mc, this)
                .setName("number")
                .setChannel("number")
                .setEnabledFlags("number")
                .setTooltips("Set a number specific to the type of sensor")
                .setLayoutHint(60, 51, 80, 14);

        typeLabel = new ChoiceLabel(mc, this).setName("type").setChannel("type");
        for (SensorType sensorType : SensorType.values()) {
            typeLabel.addChoices(sensorType.getName());
            typeLabel.setChoiceTooltip(sensorType.getName(), sensorType.getDescription());
        }
        typeLabel.setLayoutHint(60, 3, 80, 14);

        ChoiceLabel areaLabel = new ChoiceLabel(mc, this).setName("area").setChannel("area");
        for (AreaType areaType : AreaType.values()) {
            areaLabel.addChoices(areaType.getName());
            areaLabel.setChoiceTooltip(areaType.getName(), areaType.getDescription());
        }
        areaLabel.setLayoutHint(60, 19, 80, 14);

        ChoiceLabel groupLabel = new ChoiceLabel(mc, this).setName("group").setChannel("group").setEnabledFlags("group");
        for (GroupType groupType : GroupType.values()) {
            groupLabel.addChoices(groupType.getName());
            groupLabel.setChoiceTooltip(groupType.getName(), groupType.getDescription());
        }
        groupLabel.setLayoutHint(60, 35, 80, 14);

        toplevel
                .addChild(new Label(mc, this).setText("Type:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 3, 50, 14))
                .addChild(areaLabel)
                .addChild(new Label(mc, this).setText("Area:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 19, 50, 14))
                .addChild(typeLabel)
                .addChild(new Label(mc, this).setText("Group:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 35, 50, 14))
                .addChild(groupLabel)
                .addChild(new Label(mc, this).setText("Number:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 51, 50, 14))
                .addChild(numberField);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void initializeFields() {
        TextField numberField = window.findChild("number");
        int number = tileEntity.getNumber();
        numberField.setText(String.valueOf(number));

        typeLabel = window.findChild("type");
        typeLabel.setChoice(tileEntity.getSensorType().getName());

        ChoiceLabel areaLabel = window.findChild("area");
        areaLabel.setChoice(tileEntity.getAreaType().getName());

        ChoiceLabel groupLabel = window.findChild("group");
        groupLabel.setChoice(tileEntity.getGroupType().getName());
    }

    private void setupEvents() {
        window.addChannelEvent("number", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETNUMBER, params));
        window.addChannelEvent("type", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETTYPE, params));
        window.addChannelEvent("area", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETAREA, params));
        window.addChannelEvent("group", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, SensorTileEntity.CMD_SETGROUP, params));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        SensorType sensorType = NamedEnum.getEnumByName(typeLabel.getCurrentChoice(), SensorType.values());
        window.setFlag("number", sensorType.isSupportsNumber());
        window.setFlag("group", sensorType.isSupportsGroup());
        drawWindow();
    }
}
