package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Arguments;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import java.awt.Rectangle;

public class GuiCounter extends GenericGuiContainer<CounterTileEntity> {
    public static final int COUNTER_WIDTH = 200;
    public static final int COUNTER_HEIGHT = 30;

    private TextField currentField;

    public GuiCounter(CounterTileEntity counterTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, counterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "counter");
        xSize = COUNTER_WIDTH;
        ySize = COUNTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        TextField counterField = new TextField(mc, this)
                .setName("counter").setChannel("counter")
                .setTooltips("Set the counter in pulses");

        currentField = new TextField(mc, this)
                .setName("current").setChannel("current")
                .setTooltips("Set the current value", "(fires when it reaches counter)");

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChildren(new Label(mc, this).setText("Counter:"), counterField,
                new Label(mc, this).setText("Current:"), currentField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, COUNTER_WIDTH, COUNTER_HEIGHT));
        window = new Window(this, toplevel);

        requestCurrentCounter();
    }

    private void initializeFields() {
        TextField counterField = window.findChild("counter");
        int delay = tileEntity.getCounter();
        if (delay <= 0) {
            delay = 1;
        }
        counterField.setText(String.valueOf(delay));

        currentField = window.findChild("current");
        int current = tileEntity.getCurrent();
        if (current < 0) {
            current = 0;
        }
        currentField.setText(String.valueOf(current));
    }

    private void setupEvents() {
        window.addChannelEvent("counter", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, CounterTileEntity.CMD_SETCOUNTER, params));
        window.addChannelEvent("current", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, CounterTileEntity.CMD_SETCURRENT, params));
    }

    private static long lastTime = 0;

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (System.currentTimeMillis() - lastTime > 500) {
            requestCurrentCounter();
        }

        currentField.setText(String.valueOf(CounterBlock.cntReceived));

        drawWindow();
    }

    private void requestCurrentCounter() {
        lastTime = System.currentTimeMillis();
        RFToolsMessages.sendToServer(CommandHandler.CMD_GET_COUNTER_INFO,
                Arguments.builder().value(tileEntity.getWorld().provider.getDimension()).value(tileEntity.getPos()));
    }
}
