package mcjty.rftools.blocks.spaceprojector;

import mcjty.gui.RenderHelper;
import mcjty.gui.Window;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.rftools.network.PacketHandler;
import mcjty.varia.BlockMeta;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiChamberDetails extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 410;
    /** The Y size of the window in pixels. */
    protected int ySize = 210;

    private static Map<BlockMeta,Integer> items = null;
    private static Map<BlockMeta,Integer> costs = null;
    private static Map<String,Integer> entities = null;
    private static Map<String,Integer> entityCosts = null;

    private Window window;
    private WidgetList blockList;
    private Label infoLabel;
    private Label info2Label;

    public GuiChamberDetails() {
        requestChamberInfoFromServer();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void setItemsWithCount(Map<BlockMeta,Integer> items, Map<BlockMeta,Integer> costs, Map<String,Integer> entities, Map<String,Integer> entityCosts) {
        GuiChamberDetails.items = new HashMap<BlockMeta, Integer>(items);
        GuiChamberDetails.costs = new HashMap<BlockMeta, Integer>(costs);
        GuiChamberDetails.entities = new HashMap<String, Integer>(entities);
        GuiChamberDetails.entityCosts = new HashMap<String, Integer>(entityCosts);
    }

    private void requestChamberInfoFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketGetChamberInfo());
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        blockList = new WidgetList(mc, this);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(12).setVertical().setScrollable(blockList);
        Panel listPanel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new HorizontalLayout()).addChild(blockList).addChild(listSlider);

        infoLabel = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        infoLabel.setDesiredWidth(400).setDesiredHeight(14);
        info2Label = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        info2Label.setDesiredWidth(400).setDesiredHeight(14);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(listPanel).addChild(infoLabel).addChild(info2Label);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void populateLists() {
        blockList.removeChildren();
        if (items == null) {
            return;
        }

        int totalCost = 0;
        for (Map.Entry<BlockMeta, Integer> entry : items.entrySet()) {
            BlockMeta bm = entry.getKey();
            int count = entry.getValue();
            int cost = costs.get(bm);
            Panel panel = new Panel(mc,this).setLayout(new HorizontalLayout()).setDesiredHeight(16);
            ItemStack stack = new ItemStack(bm.getBlock(), 0, bm.getMeta());
            BlockRender blockRender = new BlockRender(mc, this).setRenderItem(stack).setOffsetX(-1).setOffsetY(-1);

            Label nameLabel = new Label(mc,this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
            nameLabel.setText(stack.getDisplayName()).setDesiredWidth(160);

            Label countLabel = new Label(mc, this).setText(String.valueOf(count));
            countLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(50);

            Label costLabel = new Label(mc, this);
            costLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);

            if (cost == -1) {
                costLabel.setText("NOT MOVABLE!");
            } else {
                costLabel.setText("Move Cost " + cost + " RF");
                totalCost += cost;
            }
            panel.addChild(blockRender).addChild(nameLabel).addChild(countLabel).addChild(costLabel);
            blockList.addChild(panel);
        }

        int totalCostEntities = 0;
        RenderHelper.rot += .5f;
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            String className = entry.getKey();
            Class<?> aClass = null;
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
            }
            int count = entry.getValue();
            int cost = entityCosts.get(className);
            Panel panel = new Panel(mc,this).setLayout(new HorizontalLayout()).setDesiredHeight(16);
            Entity entity = null;
            try {
                entity = (Entity) aClass.getConstructor(World.class).newInstance(mc.theWorld);
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
            BlockRender blockRender = new BlockRender(mc, this).setRenderItem(entity).setOffsetX(-1).setOffsetY(-1);

            Label nameLabel = new Label(mc,this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
            nameLabel.setText(aClass.getSimpleName()).setDesiredWidth(160);

            Label countLabel = new Label(mc, this).setText(String.valueOf(count));
            countLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(50);

            Label costLabel = new Label(mc, this);
            costLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);

            if (cost == -1) {
                costLabel.setText("NOT MOVABLE!");
            } else {
                costLabel.setText("Move Cost " + cost + " RF");
                totalCostEntities += cost;
            }
            panel.addChild(blockRender).addChild(nameLabel).addChild(countLabel).addChild(costLabel);
            blockList.addChild(panel);
        }


        infoLabel.setText("Total cost blocks: " + totalCost + " RF");
        info2Label.setText("Total cost entities: " + totalCostEntities + " RF");
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

        populateLists();

        window.draw();
        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}