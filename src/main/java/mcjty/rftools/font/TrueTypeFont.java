package mcjty.rftools.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * TrueTyper: Open Source TTF implementation for Minecraft.
 * Modified from Slick2D - under BSD Licensing -  http://slick.ninjacave.com/license/
 * <p/>
 * Copyright (c) 2013 - Slick2D
 * <p/>
 * All rights reserved.
 */

public class TrueTypeFont {
    public final static int
            ALIGN_LEFT = 0,
            ALIGN_RIGHT = 1,
            ALIGN_CENTER = 2;
    /**
     * Array that holds necessary information about the font characters
     */
    private FloatObject[] charArray = new FloatObject[256];

    /**
     * Map of user defined font characters (Character <-> IntObject)
     */
    private Map customChars = new HashMap();

    /**
     * Boolean flag on whether AntiAliasing is enabled or not
     */
    protected boolean antiAlias;

    /**
     * Font's size
     */
    private float fontSize = 0;

    /**
     * Font's height
     */
    private float fontHeight = 0;

    /**
     * Texture used to cache the font 0-255 characters
     */
    private int fontTextureID;

    /**
     * Default font texture width
     */
    private int textureWidth = 1024;

    /**
     * Default font texture height
     */
    private int textureHeight = 1024;

    /**
     * A reference to Java's AWT Font that we create our font texture from
     */
    protected Font font;

    /**
     * The font metrics for our Java AWT font
     */
    private FontMetrics fontMetrics;


    private int correctL = 9, correctR = 8;

    private class FloatObject {
        /**
         * Character's width
         */
        public float width;

        /**
         * Character's height
         */
        public float height;

        /**
         * Character's stored x position
         */
        public float storedX;

        /**
         * Character's stored y position
         */
        public float storedY;
    }


    public TrueTypeFont(Font font, boolean antiAlias, char[] additionalChars) {
        this.font = font;
        this.fontSize = font.getSize() + 3;
        this.antiAlias = antiAlias;

        createSet(additionalChars);
        System.out.println("TrueTypeFont loaded: " + font + " - AntiAlias = " + antiAlias);
        fontHeight -= 1;
        if (fontHeight <= 0) fontHeight = 1;
    }

    public TrueTypeFont(Font font, boolean antiAlias) {
        this(font, antiAlias, null);
    }

    public void setCorrection(boolean on) {
        if (on) {
            correctL = 2;
            correctR = 1;
        } else {
            correctL = 0;
            correctR = 0;
        }
    }

    private BufferedImage getFontImage(char ch) {
        // Create a temporary image to extract the character's size
        BufferedImage tempfontImage = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        fontMetrics = g.getFontMetrics();
        float charwidth = fontMetrics.charWidth(ch) + 8;

        if (charwidth <= 0) {
            charwidth = 7;
        }
        float charheight = fontMetrics.getHeight() + 3;
        if (charheight <= 0) {
            charheight = fontSize;
        }

        // Create another image holding the character we are creating
        BufferedImage fontImage;
        fontImage = new BufferedImage((int) charwidth, (int) charheight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D gt = (Graphics2D) fontImage.getGraphics();
        if (antiAlias) {
            gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        gt.setFont(font);

        gt.setColor(Color.WHITE);
        int charx = 3;
        int chary = 1;
        gt.drawString(String.valueOf(ch), (charx), (chary)
                + fontMetrics.getAscent());

        return fontImage;

    }

    private void createSet(char[] customCharsArray) {
        // If there are custom chars then I expand the font texture twice
        if (customCharsArray != null && customCharsArray.length > 0) {
            textureWidth *= 2;
        }

        // In any case this should be done in other way. Texture with size 512x512
        // can maintain only 256 characters with resolution of 32x32. The texture
        // size should be calculated dynamicaly by looking at character sizes.

        try {

            BufferedImage imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) imgTemp.getGraphics();

            g.setColor(new Color(0, 0, 0, 1));
            g.fillRect(0, 0, textureWidth, textureHeight);

            float rowHeight = 0;
            float positionX = 0;
            float positionY = 0;

            int customCharsLength = (customCharsArray != null) ? customCharsArray.length : 0;

            for (int i = 0; i < 256 + customCharsLength; i++) {

                // get 0-255 characters and then custom characters
                char ch = (i < 256) ? (char) i : customCharsArray[i - 256];

                BufferedImage fontImage = getFontImage(ch);

                FloatObject newIntObject = new FloatObject();

                newIntObject.width = fontImage.getWidth();
                newIntObject.height = fontImage.getHeight();

                if (positionX + newIntObject.width >= textureWidth) {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }

                newIntObject.storedX = positionX;
                newIntObject.storedY = positionY;

                if (newIntObject.height > fontHeight) {
                    fontHeight = newIntObject.height;
                }

                if (newIntObject.height > rowHeight) {
                    rowHeight = newIntObject.height;
                }

                // Draw it here
                g.drawImage(fontImage, (int) positionX, (int) positionY, null);

                positionX += newIntObject.width;

                if (i < 256) { // standard characters
                    charArray[i] = newIntObject;
                } else { // custom characters
                    customChars.put(new Character(ch), newIntObject);
                }

                fontImage = null;
            }

            fontTextureID = loadImage(imgTemp);


            //.getTexture(font.toString(), imgTemp);

        } catch (Exception e) {
            System.err.println("Failed to create font.");
            e.printStackTrace();
        }
    }

    private void drawQuad(float drawX, float drawY, float drawX2, float drawY2,
                          float srcX, float srcY, float srcX2, float srcY2) {
        float DrawWidth = drawX2 - drawX;
        float DrawHeight = drawY2 - drawY;
        float TextureSrcX = srcX / textureWidth;
        float TextureSrcY = srcY / textureHeight;
        float SrcWidth = srcX2 - srcX;
        float SrcHeight = srcY2 - srcY;
        float RenderWidth = (SrcWidth / textureWidth);
        float RenderHeight = (SrcHeight / textureHeight);
        //WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        //worldRenderer.setColorRGBA_F(0f, 0f, 0f, 1f);

        //worldRenderer.pos(drawX, drawY, 0).tex(TextureSrcX, TextureSrcY).endVertex();
        GL11.glTexCoord2f(TextureSrcX, TextureSrcY);
        GL11.glVertex2f(drawX, drawY);

        //worldRenderer.pos(drawX, drawY + DrawHeight, 0).tex(TextureSrcX, TextureSrcY + RenderHeight).endVertex();
        GL11.glTexCoord2f(TextureSrcX, TextureSrcY + RenderHeight);
        GL11.glVertex2f(drawX, drawY + DrawHeight);

        //worldRenderer.pos(drawX + DrawWidth, drawY + DrawHeight, 0).tex(TextureSrcX + RenderWidth, TextureSrcY + RenderHeight).endVertex();
        GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY + RenderHeight);
        GL11.glVertex2f(drawX + DrawWidth, drawY + DrawHeight);

        //worldRenderer.pos(drawX + DrawWidth, drawY, 0).tex(TextureSrcX + RenderWidth, TextureSrcY).endVertex();
        GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY);
        GL11.glVertex2f(drawX + DrawWidth, drawY);
    }

    public float getWidth(String whatchars) {
        float totalwidth = 0;
        FloatObject floatObject = null;
        int currentChar = 0;
        float lastWidth = -10f;
        for (int i = 0; i < whatchars.length(); i++) {
            currentChar = whatchars.charAt(i);
            if (currentChar < 256) {
                floatObject = charArray[currentChar];
            } else {
                floatObject = (FloatObject) customChars.get(new Character((char) currentChar));
            }

            if (floatObject != null) {
                totalwidth += floatObject.width / 2;
                lastWidth = floatObject.width;
            }
        }
        //System.out.println("Size: "+totalwidth);
        return this.fontMetrics.stringWidth(whatchars);
        //return (totalwidth);
    }

    public float getHeight() {
        return fontHeight;
    }


    public float getHeight(String HeightString) {
        return fontHeight;
    }

    public float getLineHeight() {
        return fontHeight;
    }

    public String trimStringToWidth(String text, int width) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = 0;
        int k = 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
            char c0 = text.charAt(l);
            int i1 = (int) this.getWidth("" + c0);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;

                if (flag1) {
                    ++i;
                }
            }

            if (i > width) {
                break;
            }

            stringbuilder.append(c0);
        }

        return stringbuilder.toString();
    }


    public void drawString(float x, float y, String whatchars, float scaleX, float scaleY, float yoffset, float... rgba) {
        if (rgba.length == 0) rgba = new float[]{1f, 1f, 1f, 1f};
        drawString(x, y, whatchars, 0, whatchars.length() - 1, scaleX, scaleY, ALIGN_LEFT, yoffset, rgba);
    }

    public void drawString(float x, float y, String whatchars, float scaleX, float scaleY, int format, float yoffset, float... rgba) {
        if (rgba.length == 0) rgba = new float[]{1f, 1f, 1f, 1f};

        drawString(x, y, whatchars, 0, whatchars.length() - 1, scaleX, scaleY, format, yoffset, rgba);
    }


    public void drawString(float x, float y, String whatchars, int startIndex, int endIndex, float scaleX, float scaleY, int format, float yoffset, float... rgba) {
        if (rgba.length == 0) rgba = new float[]{1f, 1f, 1f, 1f};
        GlStateManager.pushMatrix();
        GlStateManager.scale(-scaleX, -scaleY, 1.0f);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.translate(0, yoffset, 0);

        FloatObject floatObject = null;
        int charCurrent;


        float totalwidth = 0;
        int i = startIndex, d, c;
        float startY = 0;


        switch (format) {
            case ALIGN_RIGHT: {
                d = -1;
                c = correctR;

                while (i < endIndex) {
                    if (whatchars.charAt(i) == '\n') startY -= fontHeight;
                    i++;
                }
                break;
            }
            case ALIGN_CENTER: {
                for (int l = startIndex; l <= endIndex; l++) {
                    charCurrent = whatchars.charAt(l);
                    if (charCurrent == '\n') break;
                    if (charCurrent < 256) {
                        floatObject = charArray[charCurrent];
                    } else {
                        floatObject = (FloatObject) customChars.get((char) charCurrent);
                    }
                    totalwidth += floatObject.width - correctL;
                }
                totalwidth /= -2;
            }
            case ALIGN_LEFT:
            default: {
                d = 1;
                c = correctL;
                break;
            }

        }
//        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        GlStateManager.bindTexture(fontTextureID);
        //WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        //worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        GlStateManager.glBegin(GL11.GL_QUADS);
        if (rgba.length == 4)
            //worldRenderer.color(rgba[0], rgba[1], rgba[2], rgba[3]);
            GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);
        while (i >= startIndex && i <= endIndex) {

            charCurrent = whatchars.charAt(i);
            if (charCurrent < 256) {
                floatObject = charArray[charCurrent];
            } else {
                floatObject = (FloatObject) customChars.get(new Character((char) charCurrent));
            }

            if (floatObject != null) {
                if (d < 0) totalwidth += (floatObject.width - c) * d;
                if (charCurrent == '\n') {
                    startY -= fontHeight * d;
                    totalwidth = 0;
                    if (format == ALIGN_CENTER) {
                        for (int l = i + 1; l <= endIndex; l++) {
                            charCurrent = whatchars.charAt(l);
                            if (charCurrent == '\n') break;
                            if (charCurrent < 256) {
                                floatObject = charArray[charCurrent];
                            } else {
                                floatObject = (FloatObject) customChars.get(new Character((char) charCurrent));
                            }
                            totalwidth += floatObject.width - correctL;
                        }
                        totalwidth /= -2;
                    }
                    //if center get next lines total width/2;
                } else {
                    drawQuad((totalwidth + floatObject.width) + x / scaleX,
                            startY + y / scaleY,
                            totalwidth + x / scaleX,
                            (startY + floatObject.height) + y / scaleY,
                            floatObject.storedX + floatObject.width,
                            floatObject.storedY + floatObject.height,
                            floatObject.storedX,
                            floatObject.storedY);
                    if (d > 0) totalwidth += (floatObject.width - c) * d;
                }
                i += d;

            }
        }
        //Tessellator.getInstance().draw();
        GlStateManager.glEnd();

        GlStateManager.popMatrix();
//        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
    }

    public static int loadImage(BufferedImage bufferedImage) {
        try {
            short width = (short) bufferedImage.getWidth();
            short height = (short) bufferedImage.getHeight();
            //textureLoader.bpp = bufferedImage.getColorModel().hasAlpha() ? (byte)32 : (byte)24;
            int bpp = (byte) bufferedImage.getColorModel().getPixelSize();
            ByteBuffer byteBuffer;
            DataBuffer db = bufferedImage.getData().getDataBuffer();
            if (db instanceof DataBufferInt) {
                int intI[] = ((DataBufferInt) (bufferedImage.getData().getDataBuffer())).getData();
                byte newI[] = new byte[intI.length * 4];
                for (int i = 0; i < intI.length; i++) {
                    byte b[] = intToByteArray(intI[i]);
                    int newIndex = i * 4;

                    newI[newIndex] = b[1];
                    newI[newIndex + 1] = b[2];
                    newI[newIndex + 2] = b[3];
                    newI[newIndex + 3] = b[0];
                }

                byteBuffer = ByteBuffer.allocateDirect(
                        width * height * (bpp / 8))
                        .order(ByteOrder.nativeOrder())
                        .put(newI);
            } else {
                byteBuffer = ByteBuffer.allocateDirect(
                        width * height * (bpp / 8))
                        .order(ByteOrder.nativeOrder())
                        .put(((DataBufferByte) (bufferedImage.getData().getDataBuffer())).getData());
            }
            byteBuffer.flip();


            int internalFormat = GL11.GL_RGBA8,
                    format = GL11.GL_RGBA;
            IntBuffer textureId = BufferUtils.createIntBuffer(1);

            GL11.glGenTextures(textureId);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId.get(0));

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);


            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);

            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
            //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);

            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, internalFormat, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
            return textureId.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean isSupported(String fontname) {
        Font font[] = getFonts();
        for (int i = font.length - 1; i >= 0; i--) {
            if (font[i].getName().equalsIgnoreCase(fontname))
                return true;
        }
        return false;
    }

    public static Font[] getFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public void destroy() {
        IntBuffer scratch = BufferUtils.createIntBuffer(1);
        scratch.put(0, fontTextureID);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDeleteTextures(scratch);
    }
}