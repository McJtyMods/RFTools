package mcjty.rftools.font;

/**
 * TrueTyper: Open Source TTF implementation for Minecraft.
 * Copyright (C) 2013 - Mr_okushama
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Formatter {

    public static float[] getFormatted(char c) {
        int[] outrgba = null;
        float[] outfloat;
        switch (c) {
            case '0':
                outrgba = new int[]{0, 0, 0, 0, 255};
                break;
            case '1':
                outrgba = new int[]{0, 0, 170, 255};
                break;
            case '2':
                outrgba = new int[]{0, 170, 0, 255};
                break;
            case '3':
                outrgba = new int[]{0, 170, 170, 255};
                break;
            case '4':
                outrgba = new int[]{170, 0, 0, 255};
                break;
            case '5':
                outrgba = new int[]{170, 0, 170, 255};
                break;
            case '6':
                outrgba = new int[]{255, 170, 0, 255};
                break;
            case '7':
                outrgba = new int[]{170, 170, 170, 255};
                break;
            case '8':
                outrgba = new int[]{85, 85, 85, 255};
                break;
            case '9':
                outrgba = new int[]{85, 85, 255, 255};
                break;
            case 'a':
                outrgba = new int[]{85, 255, 85, 255};
                break;
            case 'b':
                outrgba = new int[]{85, 255, 255, 255};
                break;
            case 'c':
                outrgba = new int[]{255, 85, 85, 255};
                break;
            case 'd':
                outrgba = new int[]{85, 255, 255, 255};
                break;
            case 'e':
                outrgba = new int[]{255, 255, 85, 255};
                break;
            case 'f':
                outrgba = new int[]{255, 255, 255, 255};
                break;
            default:
                outrgba = new int[]{255, 255, 255, 255};
                break;
        }
        outfloat = new float[outrgba.length];
        for (int i = 0; i < outrgba.length; i++) {
            outfloat[i] = outrgba[i] > 0 ? outrgba[i] / 255 : 0;
        }
        return outfloat;
    }

}