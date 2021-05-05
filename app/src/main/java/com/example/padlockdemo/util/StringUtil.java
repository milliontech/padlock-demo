package com.example.padlockdemo.util;

public class StringUtil {

    public static String str2Hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }

    public static String hexToStr(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xff);
        }
        return new String(bytes);
    }

    private static byte[] bufread = new byte[20];
    private static byte[] bufread1 = new byte[1];
    private static byte count = 1;
    private static int count1 = 0;
    private static int ii = 0;
    private static byte sign = 0;
    private static byte sign00 = 0;
    private static byte sign01 = 0;
    private static byte sign02 = 0;
    private static byte sign03 = 0;
    private static byte sign1 = 0;

    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int chi = s.charAt(i) & 255;
            str = String.valueOf(str) + (chi < 16 ? String.valueOf('0') + Integer.toHexString(chi) : Integer.toHexString(chi)) + ' ';
        }
        return str;
    }

    public static String StringToByte(String s) {
        String str = "";
        int enable = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (enable == 1 && ch != ' ') {
                str = String.valueOf(str) + ch;
            }
            if (ch == '\n') {
                str = "";
                enable = 1;
            }
        }
        return str;
    }

    public static String StringCutToString(String s) {
        int enable = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                enable = i;
            }
        }
        return s.substring(0, enable);
    }

    public static String StringCutToString1(String s) {
        char ch;
        String str = "";
        int i = 0;
        while (i < s.length() && ((ch = s.charAt(i)) != '\n' || ((i - 1) * 4) + 5 != s.length())) {
            str = String.valueOf(str) + ch;
            i++;
        }
        return str;
    }

    public static String StringToNul(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != ' ') {
                str = String.valueOf(str) + ch;
            }
        }
        return str;
    }

    public static String toHexbyte(byte[] bytein, int count2) {
        String str = "";
        for (int i = 0; i < count2; i++) {
            int chi = bytein[i] & 255;
            str = String.valueOf(str) + (chi < 16 ? String.valueOf('0') + Integer.toHexString(chi) : Integer.toHexString(chi)) + ' ';
        }
        return str;
    }

    public static byte[] StrToHexbyte(String stringin) {
        int chh;
        int chl;
        int count2 = stringin.length();
        byte[] bArr = new byte[count2];
        byte[] data = stringin.getBytes();
        byte[] data1 = new byte[(count2 / 2)];
        for (int i = 0; i < data.length / 2; i++) {
            byte ch = data[i * 2];
            byte cl = data[(i * 2) + 1];
            int chh2 = ch & 255;
            if (chh2 >= 97) {
                chh = chh2 - 87;
            } else if (65 <= chh2) {
                chh = chh2 - 55;
            } else {
                chh = chh2 - 48;
            }
            int chl2 = cl & 255;
            if (chl2 >= 97) {
                chl = chl2 - 87;
            } else if (65 <= chl2) {
                chl = chl2 - 55;
            } else {
                chl = chl2 - 48;
            }
            data1[i] = (byte) ((chh << 4) | chl);
        }
        return data1;
    }

    public static byte[] calreseive(byte[] data2) {
        for (byte b : data2) {
            if (sign03 == 1) {
                sign00 = 0;
                bufread[ii + 3] = b;
                ii++;
                if (ii == count - 3) {
                    ii = 0;
                    sign03 = 0;
                    byte[] bufback = new byte[count];
                    System.arraycopy(bufread, 0, bufback, 0, count);
                    return bufback;
                }
            }
            if (sign02 == 1 && sign03 == 0) {
                sign03 = 1;
                sign02 = 0;
                bufread[2] = b;
                switch (b) {
                    case 3:
                        count = 5;
                        break;
                    case 4:
                        count = 6;
                        break;
                    case 5:
                        count = 7;
                        break;
                    case 7:
                        count = 10;
                        break;
                    case 8:
                        count = 10;
                        break;
                    case 12:
                        count = 15;
                        break;
                }
            }
            if (sign01 == 1 && b == -2) {
                sign02 = 1;
                sign03 = 0;
                bufread[1] = -2;
            }
            if (sign00 == 0 && b == -1) {
                sign01 = 1;
                bufread[0] = -1;
            }
        }
        return bufread1;
    }

    public static int toint(byte[] bytein) {
        return (bytein[0] * 256) + (bytein[1] & 255);
    }

}
