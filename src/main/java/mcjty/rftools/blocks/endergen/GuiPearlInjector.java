package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiPearlInjector extends GenericGuiContainer<PearlInjectorTileEntity> {

    public GuiPearlInjector(PearlInjectorTileEntity pearlInjectorTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, pearlInjectorTileEntity, container, RFTools.GUI_MANUAL_MAIN, "powinjector");
    }

    @Override
    public void initGui() {
        window = new Window(this, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/pearl_injector.gui"));
        super.initGui();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
