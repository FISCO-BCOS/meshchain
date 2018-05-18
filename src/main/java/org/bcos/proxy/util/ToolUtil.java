package org.bcos.proxy.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by fisco-dev on 17/8/29.
 */
public class ToolUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public  static String byte32ToString(byte[] data) throws UnsupportedEncodingException {
        int offset = searchByte(data,(byte)0);
        String info2 = new String(data, 0, offset,"UTF-8");
        return info2;
    }

    public  static int searchByte(byte[] data, byte value) {
        int size = data.length;
        for (int i = 0; i < size; ++i) {
            if (data[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
