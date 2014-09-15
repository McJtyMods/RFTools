package com.mcjty.rftools.blocks.crafter;

import com.mcjty.gui.events.SelectionEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.layout.VerticalAlignment;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.Window;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiCrafter extends GuiContainer {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 224;

    private Window window;
    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;

    private final CrafterBlockTileEntity crafterBlockTileEntity;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = crafterBlockTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(crafterBlockTileEntity.getCurrentRF());

        keepItem = new ChoiceLabel(mc, this).
                addChoices("All", "Keep").
                setHorizontalAlignment(HorizontalAlignment.ALIGN_CENTER).
                setVerticalAlignment(VerticalAlignment.ALIGN_CENTER).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 7, 38, 14));
        internalRecipe = new ChoiceLabel(mc, this).
                addChoices("Int", "Ext").
                setHorizontalAlignment(HorizontalAlignment.ALIGN_CENTER).
                setVerticalAlignment(VerticalAlignment.ALIGN_CENTER).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 24, 38, 14));

        recipeList = new WidgetList(mc, this).
                setRowheight(16).
                addSelectionEvent(new SelectionEvent() {
                    @Override
                    public void select(Widget parent, int index) {
                        selectRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 125, 80));
        addRecipeLine("1", Blocks.brick_stairs);
        addRecipeLine("2", Blocks.crafting_table);
        addRecipeLine("3", Items.apple);
        addRecipeLine("4", Items.arrow);
        addRecipeLine("5", null);
        addRecipeLine("6", null);
        addRecipeLine("7", null);
        addRecipeLine("8", null);

        Slider listSlider = new Slider(mc, this).setVertical().setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(137, 7, 11, 80));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(keepItem).addChild(internalRecipe).
                addChild(recipeList).addChild(listSlider);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        for (int i = 0 ; i < 10 ; i++) {
            inventorySlots.getSlot(i).putStack(craftingRecipe.getItemStack(i));
        }
    }

    private void rememberRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        ItemStack[] items = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            items[i] = inventorySlots.getSlot(i).getStack();
        }
        craftingRecipe.setRecipe(items);


        InventoryCrafting inv = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        for(int i=0 ; i<9 ; i++) {
            inv.setInventorySlotContents(i, inventorySlots.getSlot(i).getStack());
        }
        ItemStack matches = CraftingManager.getInstance().findMatchingRecipe(inv, mc.theWorld);
        inventorySlots.getSlot(9).putStack(matches);
    }

    private void addRecipeLine(String label, Object craftingResult) {
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, this).setText(label).setDesiredWidth(8)).
                addChild(new BlockRender(mc, this).setRenderItem(craftingResult)).
                addChild(new Label(mc, this).setText(BlockInfo.getReadableName(craftingResult, 0)));
        recipeList.addChild(panel);
    }

    public GuiCrafter(CrafterBlockTileEntity crafterBlockTileEntity, CrafterContainer container) {
        super(container);
        this.crafterBlockTileEntity = crafterBlockTileEntity;
        crafterBlockTileEntity.setOldRF(-1);
        crafterBlockTileEntity.setCurrentRF(crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
    }

    private void sendChangeToServer(Coordinate c) {
//        PacketHandler.INSTANCE.sendToServer(new PacketCrafter(crafterBlockTileEntity.xCoord, crafterBlockTileEntity.yCoord, crafterBlockTileEntity.zCoord, c));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        rememberRecipe();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = crafterBlockTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
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
}
