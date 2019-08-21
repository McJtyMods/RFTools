package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.util.ResourceLocation;

public class GuiCounter extends GenericGuiContainer<CounterTileEntity> {

    private TextField currentField;

    public GuiCounter(CounterTileEntity counterTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, counterTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "counter");
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

        currentField.setText(String.valueOf(CounterTileEntity.cntReceived));

        drawWindow();
    }

    private void requestCurrentCounter() {
        lastTime = System.currentTimeMillis();
        RFToolsMessages.sendToServer(CommandHandler.CMD_GET_COUNTER_INFO,
                TypedMap.builder().put(CommandHandler.PARAM_DIMENSION, tileEntity.getWorld().getDimension().getType().getId()).put(CommandHandler.PARAM_POS, tileEntity.getPos()));
    }
}
