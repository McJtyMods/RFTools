package mcjty.rftools.items.builder;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.lib.varia.BlockMeta;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.PacketGetChamberInfo;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class GuiChamberDetails extends GuiItemScreen {

    private static final int CHAMBER_XSIZE = 390;
    private static final int CHAMBER_YSIZE = 210;

    private static Map<BlockMeta,Integer> items = null;
    private static Map<BlockMeta,Integer> costs = null;
    private static Map<BlockMeta,ItemStack> stacks = null;
    private static Map<String,Integer> entities = null;
    private static Map<String,Integer> entityCosts = null;
    private static Map<String,Entity> realEntities = null;
    private static Map<String,String> playerNames = null;

    private WidgetList blockList;
    private Label infoLabel;
    private Label info2Label;

    public GuiChamberDetails() {
        super(RFTools.instance, RFToolsMessages.INSTANCE, CHAMBER_XSIZE, CHAMBER_YSIZE, RFTools.GUI_MANUAL_MAIN, "chambercard");
        requestChamberInfoFromServer();
    }

    public static void setItemsWithCount(Map<BlockMeta,Integer> items, Map<BlockMeta,Integer> costs,
                                         Map<BlockMeta,ItemStack> stacks,
                                         Map<String,Integer> entities, Map<String,Integer> entityCosts,
                                         Map<String,Entity> realEntities,
                                         Map<String,String> playerNames) {
        GuiChamberDetails.items = new HashMap<>(items);
        GuiChamberDetails.costs = new HashMap<>(costs);
        GuiChamberDetails.stacks = new HashMap<>(stacks);
        GuiChamberDetails.entities = new HashMap<>(entities);
        GuiChamberDetails.entityCosts = new HashMap<>(entityCosts);
        GuiChamberDetails.realEntities = new HashMap<>(realEntities);
        GuiChamberDetails.playerNames = new HashMap<>(playerNames);
    }

    private void requestChamberInfoFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetChamberInfo());
    }

    @Override
    public void initGui() {
        super.initGui();

        blockList = new WidgetList(mc, this);
        Slider listSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(blockList);
        Panel listPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(3)).addChild(blockList).addChild(listSlider);

        infoLabel = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        infoLabel.setDesiredWidth(380).setDesiredHeight(14);
        info2Label = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        info2Label.setDesiredWidth(380).setDesiredHeight(14);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setSpacing(1).setVerticalMargin(3)).addChild(listPanel).addChild(infoLabel).addChild(info2Label);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

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
            ItemStack stack;
            if (stacks.containsKey(bm)) {
                stack = stacks.get(bm);
            } else {
                stack = new ItemStack(bm.getBlock(), 0, bm.getMeta());
            }
            BlockRender blockRender = new BlockRender(mc, this).setRenderItem(stack).setOffsetX(-1).setOffsetY(-1);

            Label nameLabel = new Label(mc,this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setColor(StyleConfig.colorTextInListNormal);
            if (stack.getItem() == null) {
                nameLabel.setText("?").setDesiredWidth(160);
            } else {
                nameLabel.setText(stack.getDisplayName()).setDesiredWidth(160);
            }

            Label countLabel = new Label(mc, this).setText(String.valueOf(count)).setColor(StyleConfig.colorTextInListNormal);
            countLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(50);

            Label costLabel = new Label(mc, this).setColor(StyleConfig.colorTextInListNormal);
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
            int count = entry.getValue();
            int cost = entityCosts.get(className);
            Panel panel = new Panel(mc,this).setLayout(new HorizontalLayout()).setDesiredHeight(16);

            String entityName = "<?>";
            Entity entity = null;
            if (realEntities.containsKey(className)) {
                entity = realEntities.get(className);
                entityName = EntityList.getEntityString(entity);
                if (entity instanceof EntityItem) {
                    EntityItem entityItem = (EntityItem) entity;
                    if (ItemStackTools.isValid(entityItem.getEntityItem())) {
                        String displayName = entityItem.getEntityItem().getDisplayName();
                        entityName += " (" + displayName + ")";
                    }
                }
            } else {
                try {
                    Class<?> aClass = Class.forName(className);
                    entity = (Entity) aClass.getConstructor(World.class).newInstance(MinecraftTools.getWorld(mc));
                    entityName = aClass.getSimpleName();
                } catch (ClassNotFoundException e) {
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                } catch (NoSuchMethodException e) {
                }
            }

            if (playerNames.containsKey(className)) {
                entityName = playerNames.get(className);
            }

            BlockRender blockRender = new BlockRender(mc, this).setRenderItem(entity).setOffsetX(-1).setOffsetY(-1);

            Label nameLabel = new Label(mc,this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
            nameLabel.setText(entityName).setDesiredWidth(160);

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
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        populateLists();

        drawWindow();
    }
}