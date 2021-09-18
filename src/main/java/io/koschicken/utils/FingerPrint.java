package io.koschicken.utils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.Arrays;

/**
 * 均值哈希实现图像指纹比较
 *
 * @author guyadong
 */
public final class FingerPrint {
    /**
     * 图像指纹的尺寸,将图像resize到指定的尺寸，来计算哈希数组
     */
    private static final int HASH_SIZE = 16;
    /**
     * 保存图像指纹的二值化矩阵
     */
    private final byte[] binaryzationMatrix;

    public FingerPrint(byte[] hashValue) {
        if (hashValue.length != HASH_SIZE * HASH_SIZE)
            throw new IllegalArgumentException(String.format("length of hashValue must be %d", HASH_SIZE * HASH_SIZE));
        this.binaryzationMatrix = hashValue;
    }

    public FingerPrint(String hashValue) {
        this(toBytes(hashValue));
    }

    public FingerPrint(BufferedImage src) {
        this(hashValue(src));
    }

    private static byte[] hashValue(BufferedImage src) {
        BufferedImage hashImage = resize(src, HASH_SIZE, HASH_SIZE);
        byte[] matrixGray = (byte[]) toGray(hashImage).getData().getDataElements(0, 0, HASH_SIZE, HASH_SIZE, null);
        return binaryzation(matrixGray);
    }

    /**
     * 从压缩格式指纹创建{@link FingerPrint}对象
     *
     * @param compactValue
     */
    public static FingerPrint createFromCompact(byte[] compactValue) {
        return new FingerPrint(uncompact(compactValue));
    }

    public static boolean validHashValue(byte[] hashValue) {
        if (hashValue.length != HASH_SIZE)
            return false;
        for (byte b : hashValue) {
            if (0 != b && 1 != b) return false;
        }
        return true;
    }

    public static boolean validHashValue(String hashValue) {
        if (hashValue.length() != HASH_SIZE)
            return false;
        for (int i = 0; i < hashValue.length(); ++i) {
            if ('0' != hashValue.charAt(i) && '1' != hashValue.charAt(i)) return false;
        }
        return true;
    }

    /**
     * 指纹数据按位压缩
     *
     * @param hashValue
     */
    private static byte[] compact(byte[] hashValue) {
        byte[] result = new byte[(hashValue.length + 7) >> 3];
        byte b = 0;
        for (int i = 0; i < hashValue.length; ++i) {
            if (0 == (i & 7)) {
                b = 0;
            }
            if (1 == hashValue[i]) {
                b |= 1 << (i & 7);
            } else if (hashValue[i] != 0)
                throw new IllegalArgumentException("invalid hashValue,every element must be 0 or 1");
            if (7 == (i & 7) || i == hashValue.length - 1) {
                result[i >> 3] = b;
            }
        }
        return result;
    }

    /**
     * 压缩格式的指纹解压缩
     *
     * @param compactValue
     */
    private static byte[] uncompact(byte[] compactValue) {
        byte[] result = new byte[compactValue.length << 3];
        for (int i = 0; i < result.length; ++i) {
            if ((compactValue[i >> 3] & (1 << (i & 7))) == 0)
                result[i] = 0;
            else
                result[i] = 1;
        }
        return result;
    }

    /**
     * 字符串类型的指纹数据转为字节数组
     *
     * @param hashValue
     */
    private static byte[] toBytes(String hashValue) {
        hashValue = hashValue.replaceAll("\\s", "");
        byte[] result = new byte[hashValue.length()];
        for (int i = 0; i < result.length; ++i) {
            char c = hashValue.charAt(i);
            if ('0' == c)
                result[i] = 0;
            else if ('1' == c)
                result[i] = 1;
            else
                throw new IllegalArgumentException("invalid hashValue String");
        }
        return result;
    }

    /**
     * 缩放图像到指定尺寸
     *
     * @param src
     * @param width
     * @param height
     */
    private static BufferedImage resize(Image src, int width, int height) {
        BufferedImage result = new BufferedImage(width, height,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = result.getGraphics();
        try {
            g.drawImage(src.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * 计算均值
     *
     * @param src
     */
    private static int mean(byte[] src) {
        long sum = 0;
        // 将数组元素转为无符号整数
        for (byte b : src) sum += (long) b & 0xff;
        return Math.round((float) sum / src.length);
    }

    /**
     * 二值化处理
     *
     * @param src
     */
    private static byte[] binaryzation(byte[] src) {
        byte[] dst = src.clone();
        int mean = mean(src);
        for (int i = 0; i < dst.length; ++i) {
            // 将数组元素转为无符号整数再比较
            dst[i] = (byte) (((int) dst[i] & 0xff) >= mean ? 1 : 0);
        }
        return dst;
    }

    /**
     * 转灰度图像
     *
     * @param src
     */
    private static BufferedImage toGray(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return src;
        } else {
            // 图像转灰
            BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);
            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, grayImage);
            return grayImage;
        }
    }

    /**
     * 判断两个数组相似度，数组长度必须一致否则抛出异常
     *
     * @param f1
     * @param f2
     * @return 返回相似度(0.0 ~ 1.0)
     */
    private static float compare(byte[] f1, byte[] f2) {
        if (f1.length != f2.length)
            throw new IllegalArgumentException("mismatch FingerPrint length");
        int sameCount = 0;
        for (int i = 0; i < f1.length; ++i) {
            if (f1[i] == f2[i]) ++sameCount;
        }
        return (float) sameCount / f1.length;
    }

    public static float compareCompact(byte[] f1, byte[] f2) {
        return compare(uncompact(f1), uncompact(f2));
    }

    public static float compare(BufferedImage image1, BufferedImage image2) {
        return new FingerPrint(image1).compare(new FingerPrint(image2));
    }

    public byte[] compact() {
        return compact(binaryzationMatrix);
    }

    /**
     * @param multiLine 是否分行
     */
    public String toString(boolean multiLine) {
        StringBuffer buffer = new StringBuffer();
        int count = 0;
        for (byte b : this.binaryzationMatrix) {
            buffer.append(0 == b ? '0' : '1');
            if (multiLine && ++count % HASH_SIZE == 0)
                buffer.append('\n');
        }
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FingerPrint) {
            return Arrays.equals(this.binaryzationMatrix, ((FingerPrint) obj).binaryzationMatrix);
        } else
            return super.equals(obj);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * 与指定的压缩格式指纹比较相似度
     *
     * @param compactValue
     * @see #compare(FingerPrint)
     */
    public float compareCompact(byte[] compactValue) {
        return compare(createFromCompact(compactValue));
    }

    /**
     * @param hashValue
     * @see #compare(FingerPrint)
     */
    public float compare(String hashValue) {
        return compare(new FingerPrint(hashValue));
    }

    /**
     * 与指定的指纹比较相似度
     *
     * @param hashValue
     * @see #compare(FingerPrint)
     */
    public float compare(byte[] hashValue) {
        return compare(new FingerPrint(hashValue));
    }

    /**
     * 与指定图像比较相似度
     *
     * @param image2
     * @see #compare(FingerPrint)
     */
    public float compare(BufferedImage image2) {
        return compare(new FingerPrint(image2));
    }

    /**
     * 比较指纹相似度
     *
     * @param src
     * @see #compare(byte[], byte[])
     */
    public float compare(FingerPrint src) {
        if (src.binaryzationMatrix.length != this.binaryzationMatrix.length)
            throw new IllegalArgumentException("length of hashValue is mismatch");
        return compare(binaryzationMatrix, src.binaryzationMatrix);
    }
}
