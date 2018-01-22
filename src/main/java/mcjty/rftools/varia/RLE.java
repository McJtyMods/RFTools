package mcjty.rftools.varia;

import mcjty.rftools.shapes.FastByteArray;

public class RLE {

    private FastByteArray stream = new FastByteArray();
    private byte[] data = null;
    private int cnt = 0;
    private int prev = -1;

    private int pos;
    private int readcnt;
    private int readvalue;

    public void add(int c) {
        if (prev == -1) {
            prev = c;
            cnt = 1;
        } else if (prev == c && cnt < 255) {
            cnt++;
        } else {
            stream.write(cnt);
            stream.write(prev);
            prev = c;
            cnt = 1;
        }
    }

    public void reset() {
        pos = 0;
        readcnt = 0;
    }

    public int read() {
        if (readcnt == 0) {
            if (pos < data.length) {
                readcnt = (data[pos++]) & 0xff;
                readvalue = (data[pos++]) & 0xff;
            } else {
                return 0;
            }
        }
        readcnt--;
        return readvalue;
    }

    public byte[] getData() {
        if (data == null) {
            if (prev != -1) {
                stream.write(cnt);
                stream.write(prev);
                prev = -1;
                cnt = 0;
            }
            data = stream.toByteArray();
        }
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
