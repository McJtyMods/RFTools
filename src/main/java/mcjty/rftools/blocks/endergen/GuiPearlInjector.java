package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftools.RFTools;
import mcjty.rftools.proxy.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiPearlInjector extends GenericGuiContainer<PearlInjectorTileEntity> {

    public GuiPearlInjector(PearlInjectorTileEntity pearlInjectorTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, pearlInjectorTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "powinjector");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/pearl_injector.gui"));
        super.initGui();
    }
}
