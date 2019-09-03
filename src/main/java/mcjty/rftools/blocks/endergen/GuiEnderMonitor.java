package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

public class GuiEnderMonitor extends GenericGuiContainer<EnderMonitorTileEntity, EmptyContainer> {

    public GuiEnderMonitor(EnderMonitorTileEntity enderMonitorTileEntity, EmptyContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, enderMonitorTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "endermon");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/endermonitor.gui"));
        super.init();

        ChoiceLabel mode = window.findChild("mode");
        mode.setChoice(tileEntity.getMode().getDescription());
    }
}
