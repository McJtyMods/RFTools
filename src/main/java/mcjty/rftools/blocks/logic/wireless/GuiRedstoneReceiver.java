package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneReceiver extends GenericGuiContainer<RedstoneReceiverTileEntity> {

    public GuiRedstoneReceiver(RedstoneReceiverTileEntity redstoneReceiverTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, redstoneReceiverTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "redrec");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/redstone_receiver.gui"));
        super.initGui();

        initializeFields();
    }

    private void initializeFields() {
        ToggleButton analog = window.findChild("analog");
        analog.setPressed(tileEntity.getAnalog());
    }
}
