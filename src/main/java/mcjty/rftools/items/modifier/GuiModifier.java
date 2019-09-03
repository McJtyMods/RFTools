package mcjty.rftools.items.modifier;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;


public class GuiModifier extends GenericGuiContainer<GenericTileEntity, GenericContainer> {
    public static final int MODIFIER_WIDTH = 180;
    public static final int MODIFIER_HEIGHT = 228;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/modifier.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ChoiceLabel mode;
    private ChoiceLabel op;
    private Button add;
    private Button del;
    private Button up;
    private Button down;
    private WidgetList list;


    public GuiModifier(ModifierContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, null, container, inventory, GuiProxy.GUI_MANUAL_SHAPE, "modifier");
        xSize = MODIFIER_WIDTH;
        ySize = MODIFIER_HEIGHT;
    }

    private ItemStack getItem() {
        PlayerEntity player = Minecraft.getInstance().player;
        return player.getHeldItem(Hand.MAIN_HAND);
    }

    private boolean isValidItem() {
        ItemStack item = getItem();
        return !item.isEmpty() && item.getItem() == ModItems.modifierItem;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setLayout(new PositionalLayout()).setBackground(iconLocation);

        mode = new ChoiceLabel(minecraft, this).addChoices(
                ModifierFilterType.FILTER_SLOT.getCode(),
                ModifierFilterType.FILTER_ORE.getCode(),
                ModifierFilterType.FILTER_LIQUID.getCode(),
                ModifierFilterType.FILTER_TILEENTITY.getCode());
        mode.setLayoutHint(30, 9, 45, 14);
        toplevel.addChild(mode);

        op = new ChoiceLabel(minecraft, this).addChoices(
                ModifierFilterOperation.OPERATION_SLOT.getCode(),
                ModifierFilterOperation.OPERATION_VOID.getCode());
        op.setLayoutHint(110, 9, 40, 14);
        toplevel.addChild(op);

        add = new Button(minecraft, this).setChannel("add").setText("Add");
        add.setLayoutHint(10, 30, 40, 13);
        toplevel.addChild(add);

        del = new Button(minecraft, this).setChannel("del").setText("Del");
        del.setLayoutHint(52, 30, 40, 13);
        toplevel.addChild(del);

        up = new Button(minecraft, this).setChannel("up").setText("Up");
        up.setLayoutHint(110, 30, 30, 13);
        toplevel.addChild(up);

        down = new Button(minecraft, this).setChannel("down").setText("Down");
        down.setLayoutHint(142, 30, 30, 13);
        toplevel.addChild(down);

        list = new WidgetList(minecraft, this).setName("list");
        list.setLayoutHint(9, 45, 153, 95);
        toplevel.addChild(list);
        Slider slider = new Slider(minecraft, this).setVertical().setScrollableName("list");
        slider.setLayoutHint(162, 45, 10, 95);
        toplevel.addChild(slider);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        window.event("add", (source, params) -> addOp());
        window.event("del", (source, params) -> delOp());
        window.event("up", (source, params) -> upOp());
        window.event("down", (source, params) -> downOp());
    }

    private void refreshList() {
        list.removeChildren();
        List<ModifierEntry> modifiers = getModifiers();
        for (ModifierEntry modifier : modifiers) {
            ItemStack stackIn = modifier.getIn();
            ItemStack stackOut = modifier.getOut();
            ModifierFilterType type = modifier.getType();
            ModifierFilterOperation op = modifier.getOp();
            Panel panel = new Panel(minecraft, this).setLayout(new PositionalLayout()).setDesiredHeight(18).setDesiredWidth(150);
            panel.addChild(new BlockRender(minecraft, this).setLayoutHint(1, 0, 18, 18).setRenderItem(stackIn));
            panel.addChild(new Label(minecraft, this).setText(type.getCode() + " -> " + op.getCode())
                    .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .setLayoutHint(22, 0, 100, 18));
            panel.addChild(new BlockRender(minecraft, this).setLayoutHint(130, 0, 18, 18).setRenderItem(stackOut));

            list.addChild(panel);
        }
    }

    private List<ModifierEntry> getModifiers() {
        if (!isValidItem()) {
            return Collections.emptyList();
        }
        ItemStack item = getItem();
        return ModifierItem.getModifiers(item);
    }

    private void addOp() {
        if (!isValidItem()) {
            return;
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(ModifierCommand.ADD, 0, ModifierFilterType.getByCode(mode.getCurrentChoice()), ModifierFilterOperation.getByCode(op.getCurrentChoice())));
    }

    private void delOp() {
        if (list.getSelected() < 0) {
            return;
        }
        if (!isValidItem()) {
            return;
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(ModifierCommand.DEL, list.getSelected(), ModifierFilterType.getByCode(mode.getCurrentChoice()), ModifierFilterOperation.getByCode(op.getCurrentChoice())));
    }

    private void upOp() {
        if (list.getSelected() <= 0) {
            return;
        }
        if (!isValidItem()) {
            return;
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(ModifierCommand.UP, list.getSelected(), ModifierFilterType.getByCode(mode.getCurrentChoice()), ModifierFilterOperation.getByCode(op.getCurrentChoice())));
        list.setSelected(list.getSelected()-1);
    }

    private void downOp() {
        if (list.getSelected() > list.getChildCount()-1) {
            return;
        }
        if (!isValidItem()) {
            return;
        }
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(ModifierCommand.DOWN, list.getSelected(), ModifierFilterType.getByCode(mode.getCurrentChoice()), ModifierFilterOperation.getByCode(op.getCurrentChoice())));
        list.setSelected(list.getSelected()+1);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        refreshList();
        del.setEnabled(list.getSelected() >= 0 && list.getChildCount() > 0 && list.getSelected() < list.getChildCount());
        up.setEnabled(list.getSelected() > 0 && list.getSelected() < list.getChildCount());
        down.setEnabled(list.getSelected() < list.getChildCount()-1 && list.getSelected() >= 0);

        drawWindow();
    }
}
