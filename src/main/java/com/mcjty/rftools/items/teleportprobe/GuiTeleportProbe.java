package com.mcjty.rftools.items.teleportprobe;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.DefaultSelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.*;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiTeleportProbe extends GuiScreen {

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
        listDirty = 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        list = new WidgetList(mc, this).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget parent, int index) {
                teleport(index);
            }
        });
        Slider listSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(list);
        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(list).addChild(listSlider);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        serverDestinationList = null;
        destinationList = null;
        requestReceiversFromServer();
    }

    private void teleport(int index) {
        TeleportDestinationClientInfo destination = destinationList.get(index);
        Coordinate c = destination.getCoordinate();
        PacketHandler.INSTANCE.sendToServer(new PacketForceTeleport(c.getX(), c.getY(), c.getZ(), destination.getDimension()));
    }

    public static void setReceivers(List<TeleportDestinationClientInfo> destinationList) {
        serverDestinationList = new ArrayList<TeleportDestinationClientInfo>(destinationList);
    }

    private void requestReceiversFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetAllReceivers());
    }

    private void populateList() {
        if (serverDestinationList == null) {
            return;
        }
        if (serverDestinationList.equals(destinationList)) {
            return;
        }

        destinationList = new ArrayList<TeleportDestinationClientInfo>(serverDestinationList);

        list.removeChildren();

        for (TeleportDestinationClientInfo destination : destinationList) {
            Coordinate coordinate = destination.getCoordinate();
            int dim = destination.getDimension();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());

            panel.addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(destination.getName()).setDesiredWidth(100));
            panel.addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText(coordinate.toString()).setDesiredWidth(75));
            panel.addChild(new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setText("Id " + dim).setDesiredWidth(75));
            list.addChild(panel);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        listDirty--;
        if (listDirty <= 0) {
            populateList();
            listDirty = 10;
        }

        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }


}
