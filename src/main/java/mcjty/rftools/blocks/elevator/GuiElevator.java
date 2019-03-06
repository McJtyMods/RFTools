package mcjty.rftools.blocks.elevator;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

public class GuiElevator extends GenericGuiContainer<ElevatorTileEntity> {

    public GuiElevator(ElevatorTileEntity tileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, tileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "elevator");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/elevator.gui"));
        super.initGui();

        TextField elevator = window.findChild("name");
        elevator.setText(tileEntity.getName());
    }
}
