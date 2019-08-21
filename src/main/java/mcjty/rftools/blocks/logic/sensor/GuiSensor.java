package mcjty.rftools.blocks.logic.sensor;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.varia.NamedEnum;
import net.minecraft.util.ResourceLocation;

public class GuiSensor extends GenericGuiContainer<SensorTileEntity> {

    private ChoiceLabel typeLabel;

    public GuiSensor(SensorTileEntity sensorTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, sensorTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "sensor");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/sensor.gui"));
        super.initGui();

        initializeFields();
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


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        SensorType sensorType = NamedEnum.getEnumByName(typeLabel.getCurrentChoice(), SensorType.values());
        window.setFlag("number", sensorType.isSupportsNumber());
        window.setFlag("group", sensorType.isSupportsGroup());
        drawWindow();
    }
}
