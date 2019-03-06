package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.items.SyringeItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;


public class GuiSpawner extends GenericGuiContainer<SpawnerTileEntity> {
    private static final int SPAWNER_WIDTH = 180;
    private static final int SPAWNER_HEIGHT = 152;

    private EnergyBar energyBar;
    private BlockRender blocks[] = new BlockRender[3];
    private Label labels[] = new Label[3];
    private Label name;
    private Label rfTick;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spawner.png");

    public GuiSpawner(SpawnerTileEntity spawnerTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, spawnerTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "spawner");
        GenericEnergyStorageTileEntity.setCurrentRF(spawnerTileEntity.getStoredPower());

        xSize = SPAWNER_WIDTH;
        ySize = SPAWNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        long maxEnergyStored = tileEntity.getCapacity();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(10, 7, 8, 54).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        blocks[0] = new BlockRender(mc, this).setLayoutHint(80, 5, 18, 18);
        blocks[1] = new BlockRender(mc, this).setLayoutHint(80, 25, 18, 18);
        blocks[2] = new BlockRender(mc, this).setLayoutHint(80, 45, 18, 18);
        labels[0] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT); labels[0].setLayoutHint(100, 5, 74, 18);
        labels[1] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT); labels[1].setLayoutHint(100, 25, 74, 18);
        labels[2] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT); labels[2].setLayoutHint(100, 45, 74, 18);
        name = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT); name.setLayoutHint(22, 31, 78, 16);
        rfTick = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT); rfTick.setLayoutHint(22, 47, 78, 16);

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChildren(blocks[0], labels[0], blocks[1], labels[1], blocks[2], labels[2], rfTick, name);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private static long lastTime = 0;

    private void showSyringeInfo() {
        for (int i = 0 ; i < 3 ; i++) {
            blocks[i].setRenderItem(null);
            labels[i].setText("");
        }
        name.setText("");
        rfTick.setText("");

        ItemStack stack = tileEntity.getStackInSlot(SpawnerTileEntity.SLOT_SYRINGE);
        if (stack.isEmpty()) {
            return;
        }

        String mobId = SyringeItem.getMobId(stack);
        if (mobId != null) {
            String mobName = SyringeItem.getMobName(stack);
            name.setText(mobName);
            rfTick.setText(SpawnerConfiguration.mobSpawnRf.get(mobId) + "RF");
            int i = 0;
            List<SpawnerConfiguration.MobSpawnAmount> list = SpawnerConfiguration.mobSpawnAmounts.get(mobId);
            if (list != null) {
                if (System.currentTimeMillis() - lastTime > 100) {
                    lastTime = System.currentTimeMillis();
                    tileEntity.requestDataFromServer(RFTools.MODID, SpawnerTileEntity.CMD_GET_SPAWNERINFO, TypedMap.EMPTY);
//                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new SpawnerInfoPacketServer(
//                            tileEntity.getWorld().provider.getDimension(),
//                            tileEntity.getPos())));
                }

                float[] matter = new float[] { SpawnerTileEntity.matterReceived0, SpawnerTileEntity.matterReceived1, SpawnerTileEntity.matterReceived2 };

                for (SpawnerConfiguration.MobSpawnAmount spawnAmount : list) {
                    ItemStack b = spawnAmount.getObject();
                    float amount = spawnAmount.getAmount();
                    if (b.isEmpty()) {
                        Object[] blocks = {Blocks.LEAVES, Blocks.PUMPKIN, Items.WHEAT, Items.POTATO, Items.BEEF};
                        int index = (int) ((System.currentTimeMillis() / 500) % blocks.length);
                        if (blocks[index] instanceof Block) {
                            this.blocks[i].setRenderItem(new ItemStack((Block) blocks[index], 1, 0));
                        } else {
                            this.blocks[i].setRenderItem(new ItemStack((Item) blocks[index], 1, 0));
                        }
                    } else {
                        blocks[i].setRenderItem(b);
                    }
                    DecimalFormat format = new DecimalFormat("#.##");
                    format.setRoundingMode(RoundingMode.DOWN);
                    String mf = format.format(matter[i]);
                    labels[i].setText(mf + "/" + Float.toString(amount));
                    i++;
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        showSyringeInfo();

        drawWindow();
        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
