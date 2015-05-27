package mcjty.gui.widgets;

import mcjty.gui.RenderHelper;
import mcjty.gui.Window;
import mcjty.gui.layout.LayoutHint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWidget<P extends AbstractWidget> implements Widget<P> {
    protected Rectangle bounds;
    private int desiredWidth = SIZE_UNKNOWN;
    private int desiredHeight = SIZE_UNKNOWN;
    protected Minecraft mc;
    protected Gui gui;
    private LayoutHint layoutHint = null;
    private boolean enabled = true;
    protected boolean visible = true;
    protected List<String> tooltips = null;

    private boolean layoutDirty = true;
    private Object userObject = null;

    private ResourceLocation background1 = null;
    private ResourceLocation background2 = null;
    private boolean background2Horizontal = true;
    private int backgroundOffset = 256;
    private int filledRectThickness = 0;
    private int filledBackground = -1;

    protected AbstractWidget(Minecraft mc, Gui gui) {
        this.mc = mc;
        this.gui = gui;
    }

    protected void drawBox(int xx, int yy, int color) {
        gui.drawRect(xx, yy, xx, yy + bounds.height, color);
        gui.drawRect(xx + bounds.width, yy, xx + bounds.width, yy + bounds.height, color);
        gui.drawRect(xx, yy, xx + bounds.width, yy, color);
        gui.drawRect(xx, yy + bounds.height, xx + bounds.width, yy + bounds.height, color);
    }

    @Override
    public Widget getWidgetAtPosition(int x, int y) {
        return this;
    }

    @Override
    public P setTooltips(String... tooltips) {
        if (tooltips.length > 0) {
            this.tooltips = new ArrayList<String>();
            for (String s : tooltips) {
                this.tooltips.add(s);
            }
        } else {
            this.tooltips = null;
        }
        return (P) this;
    }

    @Override
    public List<String> getTooltips() {
        return tooltips;
    }

    @Override
    public P setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (P) this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isEnabledAndVisible() {
        return enabled && visible;
    }

    @Override
    public P setVisible(boolean visible) {
        this.visible = visible;
        return (P) this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public int getDesiredSize(Dimension dimension) {
        if (dimension == Dimension.DIMENSION_WIDTH) {
            return getDesiredWidth();
        } else {
            return getDesiredHeight();
        }
    }

    @Override
    public int getDesiredWidth() {
        return desiredWidth;
    }

    @Override
    public P setDesiredWidth(int desiredWidth) {
        this.desiredWidth = desiredWidth;
        return (P) this;
    }

    @Override
    public int getDesiredHeight() {
        return desiredHeight;
    }

    @Override
    public P setDesiredHeight(int desiredHeight) {
        this.desiredHeight = desiredHeight;
        return (P) this;
    }

    /**
     * Use this for a textured background.
     * @param bg
     * @return
     */
    public P setBackground(ResourceLocation bg) {
        return setBackgrounds(bg, null);
    }

    public P setBackgrounds(ResourceLocation bg1, ResourceLocation bg2) {
        this.background1 = bg1;
        this.background2 = bg2;
        this.background2Horizontal = true;
        this.backgroundOffset = 256;
        return (P) this;
    }

    public P setBackgroundLayout(boolean horizontal, int offset) {
        this.background2Horizontal = horizontal;
        this.backgroundOffset = offset;
        return (P) this;
    }

    /**
     * Use this instead of a textured background.
     * @param thickness use 0 to disable
     * @return
     */
    public P setFilledRectThickness(int thickness) {
        filledRectThickness = thickness;
        return (P) this;
    }

    public int getFilledBackground() {
        return filledBackground;
    }

    public P setFilledBackground(int filledBackground) {
        this.filledBackground = filledBackground;
        return (P) this;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public boolean in(int x, int y) {
        if (bounds == null) {
            return false;
        } else {
            return bounds.contains(x, y);
        }
    }

    protected void drawBackground(int x, int y) {
        if (!visible) {
            return;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int xx = x + bounds.x;
        int yy = y + bounds.y;
        if (background1 != null) {
            mc.getTextureManager().bindTexture(background1);
            if (background2 == null) {
                gui.drawTexturedModalRect(xx, yy, 0, 0, bounds.width, bounds.height);
            } else {
                if (background2Horizontal) {
                    gui.drawTexturedModalRect(xx, yy, 0, 0, backgroundOffset, bounds.height);
                    mc.getTextureManager().bindTexture(background2);
                    gui.drawTexturedModalRect(xx + backgroundOffset, yy, 0, 0, bounds.width - backgroundOffset, bounds.height);
                } else {
                    gui.drawTexturedModalRect(xx, yy, 0, 0, bounds.width, backgroundOffset);
                    mc.getTextureManager().bindTexture(background2);
                    gui.drawTexturedModalRect(xx, yy + backgroundOffset, 0, 0, bounds.width, bounds.height - backgroundOffset);
                }
            }
        } else if (filledRectThickness > 0) {
            RenderHelper.drawThickBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, filledRectThickness, 0xffffffff, 0xff555555, 0xffc6c6c6);
        } else if (filledRectThickness < 0) {
            RenderHelper.drawThickBeveledBox(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, -filledRectThickness, 0xffc6c6c6, 0xff555555, 0xffffffff);
        } else if (filledBackground != -1) {
            RenderHelper.drawHorizontalGradientRect(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, filledBackground, filledBackground);
        }
    }

    @Override
    public void draw(Window window, int x, int y) {
        drawBackground(x, y);
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
    }

    @Override
    public void mouseMove(int x, int y) {
    }

    @Override
    public boolean mouseWheel(int amount, int x, int y) {
        return false;
    }

    @Override
    public boolean keyTyped(Window window, char typedChar, int keyCode) {
        return false;
    }

    /**
     * Mark this widget as dirty so that the system knows a new relayout is needed.
     */
    void markDirty() {
        layoutDirty = true;
    }

    void markClean() {
        layoutDirty = false;
    }

    boolean isDirty() {
        return layoutDirty;
    }

    @Override
    public P setLayoutHint(LayoutHint hint) {
        layoutHint = hint;
        layoutDirty = true;
        return (P) this;
    }

    @Override
    public LayoutHint getLayoutHint() {
        return layoutHint;
    }

    @Override
    public Object getUserObject() {
        return userObject;
    }

    @Override
    public P setUserObject(Object obj) {
        userObject = obj;
        return (P) this;
    }
}
