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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

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
    private SimpleNetworkWrapper network;
    private int sideLeft;
    private int sideTop;
    private CraftingGridProvider provider;
    private BlockPos pos;

    public void initGui(final ModBase modBase, final SimpleNetworkWrapper network, final Minecraft mc, GenericGuiContainer gui,
                        BlockPos pos, CraftingGridProvider provider,
                        int guiLeft, int guiTop, int xSize, int ySize) {
        this.mc = mc;
        this.gui = gui;
        this.network = network;
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
        sideLeft = guiLeft - CraftingGridInventory.GRID_WIDTH - 2;
        sideTop = guiTop;
        sidePanel.setBounds(new Rectangle(sideLeft, sideTop, CraftingGridInventory.GRID_WIDTH, CraftingGridInventory.GRID_HEIGHT));
        sidePanel.setBackground(iconLocation);
        craftWindow = new Window(gui, sidePanel);
    }

    public Window getWindow() {
        return craftWindow;
    }

    private void craft(int n) {
        RFToolsMessages.INSTANCE.sendToServer(new PacketCraftFromGrid(pos, n));
    }

    private void store() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        provider.getCraftingGrid().storeRecipe(selected);
        if (pos != null) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, provider.getCraftingGrid()));
        }
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        provider.getCraftingGrid().selectRecipe(selected);
        if (pos != null) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, provider.getCraftingGrid()));
        }
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0; i < 6; i++) {
            CraftingRecipe recipe = provider.getCraftingGrid().getRecipe(i);
            addRecipeLine(recipe.getResult());
        }
    }

    public void updateGui() {
        int selected = recipeList.getSelected();
        storeButton.setEnabled(selected != -1);
        populateList();
        testRecipe();
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
        IRecipe recipe = CraftingRecipe.findRecipe(mc.theWorld, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = null;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        provider.getCraftingGrid().getCraftingGridInventory().setInventorySlotContents(0, newResult);
    }

    private void addRecipeLine(ItemStack craftingResult) {
        String readableName = BlockInfo.getReadableName(craftingResult, 0);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult == null) {
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
//                        .setDynamic(true)
                        .setText(readableName)
                        .setLayoutHint(new PositionalLayout.PositionalHint(20, 0, 30, 18)));

        recipeList.addChild(panel);
    }

}
