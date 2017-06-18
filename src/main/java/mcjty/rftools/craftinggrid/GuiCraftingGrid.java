package mcjty.rftools.craftinggrid;

import mcjty.lib.base.ModBase;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.crafter.CraftingRecipe;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.lwjgl.input.Mouse;

import java.awt.*;


public class GuiCraftingGrid {

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/craftinggrid.png");

    protected Window craftWindow;
    private Button craft1Button;
    private Button craft4Button;
    private Button craft8Button;
    private Button craftSButton;
    private Button storeButton;
    private WidgetList recipeList;

    private Minecraft mc;
    private GenericGuiContainer gui;
    private CraftingGridProvider provider;
    private BlockPos pos;

    public static int[] testResultFromServer = null;
    private int lastTestAmount = -2;
    private int lastTestTimer = 0;

    public void initGui(final ModBase modBase, final SimpleNetworkWrapper network, final Minecraft mc, GenericGuiContainer gui,
                        BlockPos pos, CraftingGridProvider provider,
                        int guiLeft, int guiTop, int xSize, int ySize) {
        this.mc = mc;
        this.gui = gui;
        SimpleNetworkWrapper network1 = network;
        this.provider = provider;
        this.pos = pos;

        recipeList = new WidgetList(mc, gui).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 56, 102));
        recipeList.addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
            }

            @Override
            public void doubleClick(Widget parent, int index) {
                selectRecipe();
            }
        });
        craft1Button = new Button(mc, gui).setText("1").setLayoutHint(new PositionalLayout.PositionalHint(29, 183, 14, 10))
                .setTooltips("Craft one")
                .addButtonEvent(parent -> craft(1));
        craft4Button = new Button(mc, gui).setText("4").setLayoutHint(new PositionalLayout.PositionalHint(45, 183, 14, 10))
                .setTooltips("Craft four")
                .addButtonEvent(parent -> craft(4));
        craft8Button = new Button(mc, gui).setText("8").setLayoutHint(new PositionalLayout.PositionalHint(29, 195, 14, 10))
                .setTooltips("Craft eight")
                .addButtonEvent(parent -> craft(8));
        craftSButton = new Button(mc, gui).setText("*").setLayoutHint(new PositionalLayout.PositionalHint(45, 195, 14, 10))
                .setTooltips("Craft a stack")
                .addButtonEvent(parent -> craft(-1));
        storeButton = new Button(mc, gui).setText("Store").setLayoutHint(new PositionalLayout.PositionalHint(5, 109, 56, 14))
                .setTooltips("Store the current recipe")
                .addButtonEvent(parent -> store());
        Panel sidePanel = new Panel(mc, gui).setLayout(new PositionalLayout())
                .addChild(craft1Button)
                .addChild(craft4Button)
                .addChild(craft8Button)
                .addChild(craftSButton)
                .addChild(storeButton)
                .addChild(recipeList);
        int sideLeft = guiLeft - CraftingGridInventory.GRID_WIDTH - 2;
        int sideTop = guiTop;
        sidePanel.setBounds(new Rectangle(sideLeft, sideTop, CraftingGridInventory.GRID_WIDTH, CraftingGridInventory.GRID_HEIGHT));
        sidePanel.setBackground(iconLocation);
        craftWindow = new Window(gui, sidePanel);
    }

    public Window getWindow() {
        return craftWindow;
    }

    private void craft(int n) {
        RFToolsMessages.INSTANCE.sendToServer(new PacketCraftFromGrid(pos, n, false));
    }

    private void testCraft(int n) {
        if (lastTestAmount != n || lastTestTimer <= 0) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCraftFromGrid(pos, n, true));
            lastTestAmount = n;
            lastTestTimer = 20;
        }
        lastTestTimer--;
    }

    private void store() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        provider.storeRecipe(selected);
        RFToolsMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, provider.getCraftingGrid()));
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        provider.getCraftingGrid().selectRecipe(selected);
        RFToolsMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, provider.getCraftingGrid()));
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0; i < 6; i++) {
            CraftingRecipe recipe = provider.getCraftingGrid().getRecipe(i);
            addRecipeLine(recipe.getResult());
        }
    }

    public void draw() {
        int selected = recipeList.getSelected();
        storeButton.setEnabled(selected != -1);
        populateList();
        testRecipe();

        int x = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
        int y = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
        Widget widget = craftWindow.getToplevel().getWidgetAtPosition(x, y);
        if (widget == craft1Button) {
            testCraft(1);
        } else if (widget == craft4Button) {
            testCraft(4);
        } else if (widget == craft8Button) {
            testCraft(8);
        } else if (widget == craftSButton) {
            testCraft(-1);
        } else {
            testResultFromServer = null;
            lastTestAmount = -2;
            lastTestTimer = 0;
        }

        craftWindow.draw();

        if (testResultFromServer != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(gui.getGuiLeft(), gui.getGuiTop(), 0.0F);

            if (testResultFromServer[9] > 0) {
                Slot slot = gui.inventorySlots.getSlotFromInventory(provider.getCraftingGrid().getCraftingGridInventory(), CraftingGridInventory.SLOT_GHOSTOUTPUT);

                if (slot != null) {
                    GlStateManager.colorMask(true, true, true, false);
                    int xPos = slot.xPos;
                    int yPos = slot.yPos;
                    gui.drawRect(xPos, yPos, xPos + 16, yPos + 16, 0xffff0000);
                }
            }
            for (int i = 0 ; i < 9 ; i++) {
                if (testResultFromServer[i] > 0) {
                    Slot slot = gui.inventorySlots.getSlotFromInventory(provider.getCraftingGrid().getCraftingGridInventory(), CraftingGridInventory.SLOT_GHOSTINPUT + i);

                    if (slot != null) {
                        GlStateManager.colorMask(true, true, true, false);
                        int xPos = slot.xPos;
                        int yPos = slot.yPos;
                        gui.drawRect(xPos, yPos, xPos + 16, yPos + 16, 0xffff0000);
                    }
                }
            }
            GlStateManager.popMatrix();
        }
    }

    private void testRecipe() {
        InventoryCrafting inv = new InventoryCrafting(new net.minecraft.inventory.Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            inv.setInventorySlotContents(i, provider.getCraftingGrid().getCraftingGridInventory().getStackInSlot(i + 1));
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(mc.world, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = ItemStack.EMPTY;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        provider.getCraftingGrid().getCraftingGridInventory().setInventorySlotContents(0, newResult);
    }

    private void addRecipeLine(ItemStack craftingResult) {
        String readableName = BlockInfo.getReadableName(craftingResult, 0);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult.isEmpty()) {
            readableName = "<recipe>";
            color = 0xFF505050;
        }
        Panel panel = new Panel(mc, gui).setLayout(new PositionalLayout()).
                addChild(new BlockRender(mc, gui)
                        .setRenderItem(craftingResult)
                        .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 18, 18))).
                addChild(new mcjty.lib.gui.widgets.Label(mc, gui)
                        .setColor(color)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setText(readableName)
                        .setLayoutHint(new PositionalLayout.PositionalHint(20, 0, 30, 18)));

        recipeList.addChild(panel);
    }

}
