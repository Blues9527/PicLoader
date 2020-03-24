package com.blues.gifloader;

import java.io.UnsupportedEncodingException;
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * *This class implements Base64 encoding/decoding functionality
 * as specified in RFC 2045 (http://www.ietf.org/rfc/rfc2045.txt).
 *
 * @author Alexander Y. Kleymenov
 * @version $Revision: 1.2 $
 */


public class Base64 {

    public static byte[] decode(byte[] in) {
        return decode(in, in.length);
    }

    private static byte[] decode(byte[] in, int len) {
        // approximate output length
        int length = len / 4 * 3;
        // return an empty array on emtpy or short input without padding
        if (length == 0) {
            return new byte[0];
        }
        // temporary array
        byte[] out = new byte[length];
        // number of padding characters ('=')
        int pad = 0;
        byte chr;
        // compute the number of the padding characters
        // and adjust the length of the input
        for (; ; len--) {
            chr = in[len - 1];
            // skip the neutral characters
            if ((chr == '\n') || (chr == '\r') ||
                    (chr == ' ') || (chr == '\t')) {
                continue;
            }
            if (chr == '=') {
                pad++;
            } else {
                break;
            }
        }
        // index in the output array
        int outIndex = 0;
        // index in the input array
        int inIndex = 0;
        // holds the value of the input character
        int bits;
        // holds the value of the input quantum
        int quantum = 0;
        for (int i = 0; i < len; i++) {
            chr = in[i];
            // skip the neutral characters
            if ((chr == '\n') || (chr == '\r') ||
                    (chr == ' ') || (chr == '\t')) {
                continue;
            }
            if ((chr >= 'A') && (chr <= 'Z')) {
                // char ASCII value
                //  A    65    0
                //  Z    90    25 (ASCII - 65)
                bits = chr - 65;
            } else if ((chr >= 'a') && (chr <= 'z')) {
                // char ASCII value
                //  a    97    26
                //  z    122   51 (ASCII - 71)
                bits = chr - 71;
            } else if ((chr >= '0') && (chr <= '9')) {
                // char ASCII value
                //  0    48    52
                //  9    57    61 (ASCII + 4)
                bits = chr + 4;
            } else if (chr == '+') {
                bits = 62;
            } else if (chr == '/') {
                bits = 63;
            } else {
                return null;
            }
            // append the value to the quantum
            quantum = (quantum << 6) | (byte) bits;
            if (inIndex % 4 == 3) {
                // 4 characters were read, so make the output:
                out[outIndex++] = (byte) ((quantum & 0x00FF0000) >> 16);
                out[outIndex++] = (byte) ((quantum & 0x0000FF00) >> 8);
                out[outIndex++] = (byte) (quantum & 0x000000FF);
            }
            inIndex++;
        }
        if (pad > 0) {
            // adjust the quantum value according to the padding
            quantum = quantum << (6 * pad);
            // make output
            out[outIndex++] = (byte) ((quantum & 0x00FF0000) >> 16);
            if (pad == 1) {
                out[outIndex++] = (byte) ((quantum & 0x0000FF00) >> 8);
            }
        }
        // create the resulting array
        byte[] result = new byte[outIndex];
        System.arraycopy(out, 0, result, 0, outIndex);
        return result;
    }

    private static final byte[] MAP = new byte[]
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                    'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
                    'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                    'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
                    '4', '5', '6', '7', '8', '9', '+', '/'};

    public static String encode(byte[] in, String charsetName) throws UnsupportedEncodingException {
        return encode(in, charsetName, true);
    }

    /**
     * 将 byte 数组 base64 编码
     *
     * @param in          传入的要加密的 byte 数组
     * @param charsetName 用于构建base64返回字符串的编译，不能为 null，建议“US-ASCII”
     * @return 返回的 base64 编码的字符串
     */
    public static String encode(byte[] in, String charsetName, boolean insertLFs) throws UnsupportedEncodingException {
        int length = in.length * 4 / 3;
        // for crlr
        length += length / 76 + 3;
        byte[] out = new byte[length];
        int index = 0, i, crlr = 0, end = in.length - in.length % 3;
        for (i = 0; i < end; i += 3) {
            out[index++] = MAP[(in[i] & 0xff) >> 2];
            out[index++] = MAP[((in[i] & 0x03) << 4)
                    | ((in[i + 1] & 0xff) >> 4)];
            out[index++] = MAP[((in[i + 1] & 0x0f) << 2)
                    | ((in[i + 2] & 0xff) >> 6)];
            out[index++] = MAP[(in[i + 2] & 0x3f)];
            if (((index - crlr) % 76 == 0) && (index != 0) && insertLFs) {
                out[index++] = '\n';
                crlr++;
                //out[index++] = '\r';
                //crlr++;
            }
        }
        switch (in.length % 3) {
            case 1:
                out[index++] = MAP[(in[end] & 0xff) >> 2];
                out[index++] = MAP[(in[end] & 0x03) << 4];
                out[index++] = '=';
                out[index++] = '=';
                break;
            case 2:
                out[index++] = MAP[(in[end] & 0xff) >> 2];
                out[index++] = MAP[((in[end] & 0x03) << 4)
                        | ((in[end + 1] & 0xff) >> 4)];
                out[index++] = MAP[((in[end + 1] & 0x0f) << 2)];
                out[index++] = '=';
                break;
            default:
                break;
        }
        return new String(out, 0, index, charsetName == null ? "US-ASCII" : charsetName);
    }

    public static String encode(String str) throws UnsupportedEncodingException {
        if (null == str) {
            return null;
        }
        return encode(str.getBytes(), null);
    }

    public static String decode(String str) {
        if (str == null) {
            return null;
        }
        return new String(decode(str.getBytes()));
    }
}


