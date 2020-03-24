package com.blues.gifloader;


import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Utils {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String getFileMD5(String filePath) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filePath);
            md5 = MessageDigest.getInstance("md5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            return "";
        }
    }

    public static String getStringMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            return toHexString(md5.digest(str.getBytes()));
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] getBytesMD5(byte[] bytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            return md5.digest(bytes);
        } catch (Exception ex) {
            return null;
        }
    }


    public static String md5(String content) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
            md5.update(content.getBytes());
            // base64 编码
            return Base64.encode(md5.digest(), "US-ASCII");
        } catch (Exception e) {
            return null;
        }
    }
}
