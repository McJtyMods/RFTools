package mcjty.rftools.items.modifier;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;


public class GuiModifier extends GenericGuiContainer {
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


    public GuiModifier(ModifierContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, null, container, RFTools.GUI_MANUAL_MAIN, "modifier");
        xSize = MODIFIER_WIDTH;
        ySize = MODIFIER_HEIGHT;
    }

    private ItemStack getItem() {
        EntityPlayerSP player = MinecraftTools.getPlayer(Minecraft.getMinecraft());
        return player.getHeldItem(EnumHand.MAIN_HAND);
    }

    private boolean isValidItem() {
        ItemStack item = getItem();
        return ItemStackTools.isValid(item) && item.getItem() == ModItems.modifierItem;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation);

        mode = new ChoiceLabel(mc, this).addChoices(
                ModifierFilterType.FILTER_SLOT.getCode(),
                ModifierFilterType.FILTER_ORE.getCode(),
                ModifierFilterType.FILTER_LIQUID.getCode(),
                ModifierFilterType.FILTER_TILEENTITY.getCode());
        mode.setLayoutHint(new PositionalLayout.PositionalHint(30, 9, 45, 14));
        toplevel.addChild(mode);

        op = new ChoiceLabel(mc, this).addChoices(
                ModifierFilterOperation.OPERATION_SLOT.getCode(),
                ModifierFilterOperation.OPERATION_VOID.getCode());
        op.setLayoutHint(new PositionalLayout.PositionalHint(110, 9, 40, 14));
        toplevel.addChild(op);

        add = new Button(mc, this).setText("Add");
        add.setLayoutHint(new PositionalLayout.PositionalHint(10, 30, 40, 13));
        add.addButtonEvent(parent -> addOp());
        toplevel.addChild(add);

        del = new Button(mc, this).setText("Del");
        del.setLayoutHint(new PositionalLayout.PositionalHint(52, 30, 40, 13));
        del.addButtonEvent(parent -> delOp());
        toplevel.addChild(del);

        up = new Button(mc, this).setText("Up");
        up.setLayoutHint(new PositionalLayout.PositionalHint(110, 30, 30, 13));
        up.addButtonEvent(parent -> upOp());
        toplevel.addChild(up);

        down = new Button(mc, this).setText("Down");
        down.setLayoutHint(new PositionalLayout.PositionalHint(142, 30, 30, 13));
        down.addButtonEvent(parent -> downOp());
        toplevel.addChild(down);

        list = new WidgetList(mc, this);
        list.setLayoutHint(new PositionalLayout.PositionalHint(9, 45, 153, 95));
        toplevel.addChild(list);
        Slider slider = new Slider(mc, this).setVertical().setScrollable(list);
        slider.setLayoutHint(new PositionalLayout.PositionalHint(162, 45, 10, 95));
        toplevel.addChild(slider);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void refreshList() {
        list.removeChildren();
        List<ModifierEntry> modifiers = getModifiers();
        for (ModifierEntry modifier : modifiers) {
            ItemStack stackIn = modifier.getIn();
            ItemStack stackOut = modifier.getOut();
            ModifierFilterType type = modifier.getType();
            ModifierFilterOperation op = modifier.getOp();
            Panel panel = new Panel(mc, this).setLayout(new PositionalLayout()).setDesiredHeight(18).setDesiredWidth(150);
            panel.addChild(new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(1, 0, 18, 18)).setRenderItem(stackIn));
            panel.addChild(new Label<>(mc, this).setText(type.getCode() + " -> " + op.getCode())
                    .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                    .setLayoutHint(new PositionalLayout.PositionalHint(22, 0, 100, 18)));
            panel.addChild(new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(130, 0, 18, 18)).setRenderItem(stackOut));

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

    private NBTTagList getTagList(List<ModifierEntry> modifiers) {
        NBTTagList taglist = new NBTTagList();
        for (ModifierEntry modifier : modifiers) {
            NBTTagCompound tag = new NBTTagCompound();

            if (ItemStackTools.isValid(modifier.getIn())) {
                NBTTagCompound tc = new NBTTagCompound();
                modifier.getIn().writeToNBT(tc);
                tag.setTag("in", tc);
            }
            if (ItemStackTools.isValid(modifier.getOut())) {
                NBTTagCompound tc = new NBTTagCompound();
                modifier.getOut().writeToNBT(tc);
                tag.setTag("out", tc);
            }

            tag.setString("type", modifier.getType().getCode());
            tag.setString("op", modifier.getOp().getCode());

            taglist.appendTag(tag);

        }

        return taglist;
    }

    private void updateModifiers(List<ModifierEntry> modifiers) {
        NBTTagList tagList = getTagList(modifiers);
        ItemStack item = getItem();
        item.getTagCompound().setTag("ops", tagList);
        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(item));
    }

    private void addOp() {
        if (!isValidItem()) {
            return;
        }

        List<ModifierEntry> modifiers = getModifiers();
        ItemStack stackIn = inventorySlots.getSlot(ModifierContainer.SLOT_FILTER).getStack();
        ItemStack stackOut = inventorySlots.getSlot(ModifierContainer.SLOT_REPLACEMENT).getStack();
        modifiers.add(new ModifierEntry(stackIn, stackOut, ModifierFilterType.getByCode(mode.getCurrentChoice()), ModifierFilterOperation.getByCode(op.getCurrentChoice())));
        inventorySlots.getSlot(ModifierContainer.SLOT_FILTER).putStack(ItemStackTools.getEmptyStack());
        inventorySlots.getSlot(ModifierContainer.SLOT_REPLACEMENT).putStack(ItemStackTools.getEmptyStack());
        updateModifiers(modifiers);
    }

    private void delOp() {
        if (list.getSelected() < 0) {
            return;
        }
        if (!isValidItem()) {
            return;
        }

        List<ModifierEntry> modifiers = getModifiers();
        ModifierEntry entry = modifiers.get(list.getSelected());
        ItemStack in = entry.getIn();
        ItemStack out = entry.getOut();
        if (ItemStackTools.isValid(in) && inventorySlots.getSlot(ModifierContainer.SLOT_FILTER).getHasStack()) {
            // Something is in the way
            return;
        }
        if (ItemStackTools.isValid(out) && inventorySlots.getSlot(ModifierContainer.SLOT_REPLACEMENT).getHasStack()) {
            // Something is in the way
            return;
        }
        if (ItemStackTools.isValid(in)) {
            inventorySlots.getSlot(ModifierContainer.SLOT_FILTER).putStack(in);
        }
        if (ItemStackTools.isValid(out)) {
            inventorySlots.getSlot(ModifierContainer.SLOT_REPLACEMENT).putStack(out);
        }

        modifiers.remove(list.getSelected());
        updateModifiers(modifiers);
    }

    private void upOp() {
        if (list.getSelected() <= 0) {
            return;
        }
        if (!isValidItem()) {
            return;
        }

        List<ModifierEntry> modifiers = getModifiers();
        ModifierEntry entry = modifiers.get(list.getSelected());
        modifiers.remove(list.getSelected());
        modifiers.add(list.getSelected()-1, entry);
        list.setSelected(list.getSelected()-1);
        updateModifiers(modifiers);
    }

    private void downOp() {
        if (list.getSelected() > list.getChildCount()-1) {
            return;
        }
        if (!isValidItem()) {
            return;
        }

        List<ModifierEntry> modifiers = getModifiers();
        ModifierEntry entry = modifiers.get(list.getSelected());
        modifiers.remove(list.getSelected());
        modifiers.add(list.getSelected()+1, entry);
        list.setSelected(list.getSelected()+1);
        updateModifiers(modifiers);
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
