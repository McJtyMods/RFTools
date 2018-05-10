package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Arguments;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiCounter extends GenericGuiContainer<CounterTileEntity> {

    private TextField currentField;

    public GuiCounter(CounterTileEntity counterTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, counterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "counter");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/counter.gui"));
        super.initGui();

        requestCurrentCounter();

        initializeFields();
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
