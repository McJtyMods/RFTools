package mcjty.rftools.shapes;

import java.util.ArrayList;
import java.util.List;


public class FastByteArray {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private List<byte[]> buffers;
    private byte[] buffer;

    private int blockSize;
    private int index;
    private int size;


    // Constructors --------------------------------------------------
    public FastByteArray() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public FastByteArray(int aSize) {
        blockSize = aSize;
        buffer = new byte[blockSize];
    }


    public int getSize() {
        return size + index;
    }

    public byte[] toByteArray() {
        byte[] data = new byte[getSize()];

        // Check if we have a list of buffers
        int pos = 0;

        if (buffers != null) {

            for (byte[] bytes : buffers) {
                System.arraycopy(bytes, 0, data, pos, blockSize);
                pos += blockSize;
            }
        }

        // write the internal buffer directly
        System.arraycopy(buffer, 0, data, pos, index);

        return data;
    }

    @Override
    public String toString() {
        return new String(toByteArray());
    }

    // OutputStream overrides ----------------------------------------
    public void write(int datum) {
        if (index == blockSize) {
            addBuffer();
        }

        // store the byte
        buffer[index++] = (byte) datum;
    }

    /**
     * Create a new buffer and store the
     * current one in linked list
     */
    private void addBuffer() {
        if (buffers == null) {
            buffers = new ArrayList<>();
        }

        buffers.add(buffer);

        buffer = new byte[blockSize];
        size += index;
        index = 0;
    }
}
