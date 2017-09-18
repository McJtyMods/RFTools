package mcjty.rftools.items.modifier;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.awt.*;


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
        NBTTagList taglist = getOpList();
        for (int i = 0 ; i < taglist.tagCount() ; i++) {
            NBTTagCompound compound = taglist.getCompoundTagAt(i);
            ItemStack stackIn = ItemStackTools.loadFromNBT(compound.getCompoundTag("in"));
            ItemStack stackOut = ItemStackTools.loadFromNBT(compound.getCompoundTag("out"));
            ModifierFilterType type = ModifierFilterType.getByCode(compound.getString("type"));
            ModifierFilterOperation op = ModifierFilterOperation.getByCode(compound.getString("op"));
            Panel panel = new Panel(mc, this).setLayout(new PositionalLayout()).setDesiredHeight(18).setDesiredWidth(150);
            panel.addChild(new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(1, 0, 18, 18)).setRenderItem(stackIn));
            panel.addChild(new Label<>(mc, this).setText(type.getCode() + " -> " + op.getCode()).setLayoutHint(new PositionalLayout.PositionalHint(22, 0, 110, 18)));
            panel.addChild(new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(130, 0, 18, 18)).setRenderItem(stackOut));

            list.addChild(panel);
        }
    }

    private NBTTagList getOpList() {
        ItemStack item = getItem();
        if (ItemStackTools.isValid(item) && item.getItem() == ModItems.modifierItem) {
            if (!item.hasTagCompound()) {
                item.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = item.getTagCompound();
            return tag.getTagList("ops", Constants.NBT.TAG_COMPOUND);
        }
        return new NBTTagList();
    }

    private void addOp() {
        if (!isValidItem()) {
            return;
        }

        NBTTagList taglist = getOpList();
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagCompound tc = new NBTTagCompound();
        new ItemStack(Blocks.DIAMOND_BLOCK).writeToNBT(tc);
        tag.setTag("in", tc);

        tc = new NBTTagCompound();
        new ItemStack(Blocks.DOUBLE_STONE_SLAB).writeToNBT(tc);
        tag.setTag("out", tc);

        tag.setString("type", ModifierFilterType.FILTER_ORE.getCode());
        tag.setString("op", ModifierFilterOperation.OPERATION_SLOT.getCode());

        taglist.appendTag(tag);

        RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateModifier(getItem()));
    }

    private void delOp() {

    }

    private void upOp() {

    }

    private void downOp() {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        refreshList();
        del.setEnabled(list.getSelected() >= 0);
        up.setEnabled(list.getSelected() > 0);
        down.setEnabled(list.getSelected() < list.getChildCount()-1);

        drawWindow();
    }
}
