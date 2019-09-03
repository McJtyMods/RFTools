package mcjty.rftools.items.teleportprobe;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiTeleportProbe extends Screen {

    /** The X size of the window in pixels. */
    private int xSize = 356;
    /** The Y size of the window in pixels. */
    private int ySize = 180;

    private Window window;
    private WidgetList list;

    private static List<TeleportDestinationClientInfo> serverDestinationList = null;
    private static List<TeleportDestinationClientInfo> destinationList = null;

    private int listDirty;

    public GuiTeleportProbe() {
        super(new StringTextComponent("Teleport Probe"));
        listDirty = 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        list = new WidgetList(minecraft, this).setName("list").addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget<?> parent, int index) {
                teleport(index);
            }
        });
        Slider listSlider = new Slider(minecraft, this).setDesiredWidth(11).setVertical().setScrollableName("list");
        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(list).addChild(listSlider);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        serverDestinationList = null;
        destinationList = null;
        requestReceiversFromServer();
    }

    private void teleport(int index) {
        TeleportDestinationClientInfo destination = destinationList.get(index);
        BlockPos c = destination.getCoordinate();
        RFToolsMessages.sendToServer(CommandHandler.CMD_FORCE_TELEPORT,
                TypedMap.builder().put(CommandHandler.PARAM_DIMENSION, destination.getDimension()).put(CommandHandler.PARAM_POS, c));
    }

    public static void setReceivers(List<TeleportDestinationClientInfo> destinationList) {
        serverDestinationList = new ArrayList<>(destinationList);
    }

    private void requestReceiversFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetAllReceivers());
    }

    private void populateList() {
        if (serverDestinationList == null) {
            return;
        }
        if (serverDestinationList.equals(destinationList)) {
            return;
        }

        destinationList = new ArrayList<>(serverDestinationList);

        list.removeChildren();

        for (TeleportDestinationClientInfo destination : destinationList) {
            BlockPos coordinate = destination.getCoordinate();
            int dim = destination.getDimension();

            Panel panel = new Panel(minecraft, this).setLayout(new HorizontalLayout());

            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setText(destination.getName()).setDesiredWidth(100));
            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setText(BlockPosTools.toString(coordinate)).setDesiredWidth(75));
            panel.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setText("Id " + dim).setDesiredWidth(75));
            list.addChild(panel);
        }
    }

    // @todo 1.14
//    @Override
//    protected void mouseClicked(int x, int y, int button) throws IOException {
//        super.mouseClicked(x, y, button);
//        window.mouseClicked(x, y, button);
//    }
//
//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        window.handleMouseInput();
//    }
//
//    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException {
//        super.keyTyped(typedChar, keyCode);
//        window.keyTyped(typedChar, keyCode);
//    }

    @Override
    public void render(int xSize_lo, int ySize_lo, float par3) {
        super.render(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 10;
        }

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            double mouseX = minecraft.mouseHelper.getMouseX();
            double mouseY = minecraft.mouseHelper.getMouseY();
            int x = (int) (mouseX * width / minecraft.mainWindow.getWidth());
            int y = (int) (height - mouseY * height / minecraft.mainWindow.getHeight() - 1);

            renderTooltip(tooltips, x-guiLeft, y-guiTop, minecraft.fontRenderer);
        }
    }


}
