package mcjty.rftools.shapes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static mcjty.rftools.blocks.builder.BuilderConfiguration.useVBO;

public class RenderData {

    private static VertexBuffer vboBuffer = new VertexBuffer(2097152);

    private RenderPlane planes[] = null;
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

    public RenderPlane[] getPlanes() {
        return planes;
    }

    public void setPlanes(RenderPlane[] planes) {
        this.planes = planes;
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

    // A render plane is a horizonal plane of data. It is made out of strips
    public static class RenderPlane {
        private final RenderStrip[] strips;
        private final int y;
        private final int startz;

        public RenderPlane(RenderStrip[] strips, int y, int startz) {
            this.strips = strips;
            this.y = y;
            this.startz = startz;
        }

        public RenderStrip[] getStrips() {
            return strips;
        }

        public int getY() {
            return y;
        }

        public int getStartz() {
            return startz;
        }
    }

    // A render strip is a single horizontal (on z axis) strip of data
    public static class RenderStrip {
        private final List<Pair<Integer, IBlockState>> data = new ArrayList<>();
        private final int x;
        private IBlockState last;
        private int cnt = 0;

        public RenderStrip(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public List<Pair<Integer, IBlockState>> getData() {
            return data;
        }

        public boolean isEmptyAt(int i) {
            if (i < 0) {
                return true;
            }
            if (i >= data.size()) {
                return true;
            }
            return data.get(i).getValue() == null;
        }

        public void add(IBlockState state) {
            if (cnt == 0) {
                last = state;
                cnt = 1;
            } else {
                if (last != state) {
                    data.add(Pair.of(cnt, last));
                    last = state;
                    cnt = 1;
                } else {
                    cnt++;
                }
            }
        }

        public void close() {
            if (cnt > 0) {
                data.add(Pair.of(cnt, last));
                cnt = 0;
            }
        }
    }
}
