package mcjty.rftools.blocks.logic.analog;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;
import java.text.DecimalFormat;

public class GuiAnalog extends GenericGuiContainer<AnalogTileEntity> {
    public static final int ANALOG_WIDTH = 194;
    public static final int ANALOG_HEIGHT = 154;

    private TextField mulEqual;
    private TextField mulLess;
    private TextField mulGreater;
    private TextField addEqual;
    private TextField addLess;
    private TextField addGreater;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/analog.png");

    public GuiAnalog(AnalogTileEntity te, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "analog");
        xSize = ANALOG_WIDTH;
        ySize = ANALOG_HEIGHT;
    }

    private static final DecimalFormat fmt = new DecimalFormat("#.#");


    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());
        mulEqual = new TextField(mc, this).setName("mul_eq").setLayoutHint(106, 78, 30, 16);
        mulLess = new TextField(mc, this).setName("mul_less").setLayoutHint(106, 104, 30, 16);
        mulGreater = new TextField(mc, this).setName("mul_greater").setLayoutHint(106, 130, 30, 16);
        addEqual = new TextField(mc, this).setName("add_eq").setLayoutHint(153, 78, 30, 16);
        addLess = new TextField(mc, this).setName("add_less").setLayoutHint(153, 104, 30, 16);
        addGreater = new TextField(mc, this).setName("add_greater").setLayoutHint(153, 130, 30, 16);
        toplevel.addChildren(mulEqual, mulLess, mulGreater, addEqual, addLess, addGreater);

        initializeFields();

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, ANALOG_WIDTH, ANALOG_HEIGHT));
        window = new Window(this, toplevel);

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
        window.addChannelEvent("update", (source, params) -> updateAnalog());
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
                new Argument("mulE", safeDouble(mulEqual.getText())),
                new Argument("mulL", safeDouble(mulLess.getText())),
                new Argument("mulG", safeDouble(mulGreater.getText())),
                new Argument("addE", safeInt(addEqual.getText())),
                new Argument("addL", safeInt(addLess.getText())),
                new Argument("addG", safeInt(addGreater.getText())));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
