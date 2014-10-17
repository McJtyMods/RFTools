package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextPage extends AbstractWidget<TextPage> {
    private final List<Page> pages = new ArrayList<Page>();
    private final Map<String,Integer> nodes = new HashMap<String, Integer>();

    private int pageIndex = 0;
    private final List<Line> lines = new ArrayList<Line>();
    private final List<Link> links = new ArrayList<Link>();

    public TextPage(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    private void setPage(Page page) {
        lines.clear();
        links.clear();
        if (!pages.isEmpty()) {
            int y = 3;
            for (Line line : page.lines) {
                lines.add(line);
                if (line.isLink()) {
                    links.add(new Link(y, y+13, line.node));
                }
                y += 14;
            }
        }
    }

    private void newPage(TextPage.Page page) {
        if (!page.isEmpty()) {
            pages.add(page);
        }
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageCount() {
        return pages.size();
    }

    public TextPage setText(ResourceLocation manualResource) {
        TextPage.Page page = new TextPage.Page();
        try {
            IResourceManager resourceManager = mc.getResourceManager();
            IResource iresource = resourceManager.getResource(manualResource);
            InputStream inputstream = iresource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
            String line = br.readLine();
            while (line != null) {
                if ("{}".equals(line)) {
                    newPage(page);
                    page = new TextPage.Page();
                } else {
                    Line l = page.addLine(line);
                    if (l.isNode()) {
                        nodes.put(l.node, pages.size());
                    }
                }
                line = br.readLine();
            }
            newPage(page);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showCurrentPage();
        return this;
    }

    public void prevPage() {
        pageIndex--;
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        showCurrentPage();
    }

    public void nextPage() {
        pageIndex++;
        if (pageIndex >= pages.size()) {
            pageIndex = pages.size()-1;
        }
        showCurrentPage();
    }

    private void showCurrentPage() {
        setPage(pages.get(pageIndex));
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (enabled) {
            window.setTextFocus(this);
            for (Link link : links) {
                if (link.y1 <= y && y <= link.y2) {
                    Integer page = nodes.get(link.node);
                    if (page != null) {
                        pageIndex = page;
                        showCurrentPage();
                        return this;
                    }
                }
            }
            return this;
        }
        return null;
    }

    @Override
    public boolean keyTyped(Window window, char typedChar, int keyCode) {
        boolean rc = super.keyTyped(window, typedChar, keyCode);
        if (rc) {
            return true;
        }
        if (enabled) {
            if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_LEFT) {
                prevPage();
                return true;
            } else if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_RIGHT) {
                nextPage();
                return true;
            } else if (keyCode == Keyboard.KEY_HOME) {
                pageIndex = 0;
                showCurrentPage();
            } else if (keyCode == Keyboard.KEY_END) {
                if (!pages.isEmpty()) {
                    pageIndex = pages.size()-1;
                    showCurrentPage();
                }
            }
        }
        return false;
    }

    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);

        y += 3;
        int dx;
        for (Line line : lines) {
            System.out.println("line.line = " + line.line);
            if (line.line != null) {
                String s = "";
                int col = 0xFF000000;
                dx = 0;
                if (line.isBold()) {
                    char c = 167;
                    s = Character.toString(c) + "l";
                }
                if (line.isLink()) {
                    char c = 167;
                    s = Character.toString(c) + "n";
                    col = 0xFF0040AA;
                    dx = 25;
                }
                s += line.line;
                mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(s, bounds.width-dx), x + dx + bounds.x, y + bounds.y, col);
                y += 14;
            }
        }
    }

    private static class Line {
        private boolean bold;
        private boolean islink;
        private boolean isnode;
        String node;
        String line;

        public boolean isBold() {
            return bold;
        }

        public boolean isLink() {
            return islink;
        }

        public boolean isNode() {
            return isnode;
        }

        Line(String line) {
            bold = false;
            islink = false;
            node = null;

            if (line.startsWith("{b}")) {
                bold = true;
                this.line = line.substring(3);
            } else if (line.startsWith("{n:")) {
                int end = line.indexOf('}');
                if (end == -1) {
                    // Error, just put in the entire line
                    this.line = line;
                } else {
                    node = line.substring(3, end);
                    isnode = true;
                    this.line = null;
                }
            } else if (line.startsWith("{l:")) {
                int end = line.indexOf('}');
                if (end == -1) {
                    // Error, just put in the entire line
                    this.line = line;
                } else {
                    node = line.substring(3, end);
                    islink = true;
                    this.line = line.substring(end+1);
                }
            } else {
                this.line = line;
            }
        }
    }

    public static class Page {
        final List<Line> lines = new ArrayList<Line>();

        public boolean isEmpty() {
            return lines.isEmpty();
        }

        public Line addLine(String line) {
            Line l = new Line(line);
            lines.add(l);
            return l;
        }
    }

    public static class Link {
        int y1;
        int y2;
        String node;

        public Link(int y1, int y2, String node) {
            this.y1 = y1;
            this.y2 = y2;
            this.node = node;
        }
    }

}
