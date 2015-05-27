package mcjty.rftools.items.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiStorageFilter extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/storagefilter.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiStorageFilter(StorageFilterContainer container) {
        super(null, container);
        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
    }
}
