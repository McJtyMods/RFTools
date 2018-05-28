package mcjty.rftools.blocks.logic.analog;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.text.DecimalFormat;

public class GuiAnalog extends GenericGuiContainer<AnalogTileEntity> {

    private TextField mulEqual;
    private TextField mulLess;
    private TextField mulGreater;
    private TextField addEqual;
    private TextField addLess;
    private TextField addGreater;

    public GuiAnalog(AnalogTileEntity te, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "analog");
    }

    private static final DecimalFormat fmt = new DecimalFormat("#.#");


    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/analog.gui"));
        super.initGui();

        initializeFields();
        setupEvents();
    }

    private void initializeFields() {
        mulEqual = window.findChild("mul_eq");
        mulLess = window.findChild("mul_less");
        mulGreater = window.findChild("mul_greater");
        addEqual = window.findChild("add_eq");
        addLess = window.findChild("add_less");
        addGreater = window.findChild("add_greater");

        mulEqual.setText(fmt.format(tileEntity.getMulEqual()));
        mulLess.setText(fmt.format(tileEntity.getMulLess()));
        mulGreater.setText(fmt.format(tileEntity.getMulGreater()));
        addEqual.setText(String.valueOf(tileEntity.getAddEqual()));
        addLess.setText(String.valueOf(tileEntity.getAddLess()));
        addGreater.setText(String.valueOf(tileEntity.getAddGreater()));
    }

    private void setupEvents() {
        window.event("update", (source, params) -> updateAnalog());
    }

    private static double safeDouble(String f) {
        try {
            return Double.parseDouble(f);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int safeInt(String f) {
        try {
            return Integer.parseInt(f);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateAnalog() {
        sendServerCommand(RFToolsMessages.INSTANCE, AnalogTileEntity.CMD_UPDATE,
                TypedMap.builder()
                        .put(AnalogTileEntity.PARAM_MUL_EQ, safeDouble(mulEqual.getText()))
                        .put(AnalogTileEntity.PARAM_MUL_LESS, safeDouble(mulLess.getText()))
                        .put(AnalogTileEntity.PARAM_MUL_GT, safeDouble(mulGreater.getText()))
                        .put(AnalogTileEntity.PARAM_ADD_EQ, safeInt(addEqual.getText()))
                        .put(AnalogTileEntity.PARAM_ADD_LESS, safeInt(addLess.getText()))
                        .put(AnalogTileEntity.PARAM_ADD_GT, safeInt(addGreater.getText()))
                        .build());
    }
}
