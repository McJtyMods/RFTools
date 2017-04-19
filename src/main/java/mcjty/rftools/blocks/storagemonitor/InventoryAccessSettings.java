package mcjty.rftools.blocks.storagemonitor;

public class InventoryAccessSettings {
    private boolean blockInputGui = false;
    private boolean blockInputAuto = false;
    private boolean blockInputScreen = false;
    private boolean blockOutputGui = false;
    private boolean blockOutputAuto = false;
    private boolean blockOutputScreen = false;

    public boolean isBlockInputGui() {
        return blockInputGui;
    }

    public void setBlockInputGui(boolean blockInputGui) {
        this.blockInputGui = blockInputGui;
    }

    public boolean isBlockInputAuto() {
        return blockInputAuto;
    }

    public void setBlockInputAuto(boolean blockInputAuto) {
        this.blockInputAuto = blockInputAuto;
    }

    public boolean isBlockInputScreen() {
        return blockInputScreen;
    }

    public void setBlockInputScreen(boolean blockInputScreen) {
        this.blockInputScreen = blockInputScreen;
    }

    public boolean isBlockOutputGui() {
        return blockOutputGui;
    }

    public void setBlockOutputGui(boolean blockOutputGui) {
        this.blockOutputGui = blockOutputGui;
    }

    public boolean isBlockOutputScreen() {
        return blockOutputScreen;
    }

    public void setBlockOutputScreen(boolean blockOutputScreen) {
        this.blockOutputScreen = blockOutputScreen;
    }

    public boolean isBlockOutputAuto() {
        return blockOutputAuto;
    }

    public void setBlockOutputAuto(boolean blockOutputAuto) {
        this.blockOutputAuto = blockOutputAuto;
    }

    public boolean inputBlocked() {
        return blockInputGui || blockInputScreen || blockInputAuto;
    }

    public boolean outputBlocked() {
        return blockOutputGui || blockOutputScreen || blockOutputAuto;
    }
}
