package mcjty.rftools.shapes;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import static mcjty.rftools.blocks.builder.BuilderConfiguration.useVBO;

public class RenderData {

    private static VertexBuffer vboBuffer = new VertexBuffer(2097152);

    private ShapeRenderer.RenderColumn columns[] = null;
    public int shapeCount = 0;
    public String previewMessage = "";
    private int glList = -1;
    private net.minecraft.client.renderer.vertex.VertexBuffer vbo;
    private long touchTime = 0;

    public boolean hasData() {
        if (useVBO) {
            return vbo != null;
        } else {
            return glList != -1;
        }
    }

    public void setColumns(ShapeRenderer.RenderColumn[] columns) {
        this.columns = columns;
    }

    public ShapeRenderer.RenderColumn[] getColumns() {
        return columns;
    }

    public void touch() {
        touchTime = System.currentTimeMillis();
    }

    public boolean tooOld() {
        return touchTime + 5000 < System.currentTimeMillis();
    }


    public void cleanup() {
        if (useVBO) {
            if (vbo != null) {
                vbo.deleteGlBuffers();
                vbo = null;
            }
        } else {
            if (glList != -1) {
                GLAllocation.deleteDisplayLists(glList);
                glList = -1;
            }
        }
    }

    public void render() {
        if (useVBO) {
            //...
            vbo.bindBuffer();
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 16, 12);
            vbo.drawArrays(7);
            vbo.unbindBuffer();
            GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        } else {
            GlStateManager.callList(glList);
        }
    }

    public VertexBuffer createRenderList(VertexBuffer buffer) {
        if (useVBO) {
            vbo = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
            buffer = vboBuffer;
        } else {
            glList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(glList, GL11.GL_COMPILE);
        }
        return buffer;
    }

    public void performRender(Tessellator tessellator, VertexBuffer buffer) {
        if (useVBO) {
            buffer.finishDrawing();
            buffer.reset();
            vbo.bufferData(buffer.getByteBuffer());

        } else {
            tessellator.draw();
            GlStateManager.glEndList();
        }
    }
}
