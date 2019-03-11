package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.proxy.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiEnderMonitor extends GenericGuiContainer<EnderMonitorTileEntity> {

    public GuiEnderMonitor(EnderMonitorTileEntity enderMonitorTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, enderMonitorTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "endermon");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/endermonitor.gui"));
        super.initGui();

        ChoiceLabel mode = window.findChild("mode");
        mode.setChoice(tileEntity.getMode().getDescription());
    }
}
