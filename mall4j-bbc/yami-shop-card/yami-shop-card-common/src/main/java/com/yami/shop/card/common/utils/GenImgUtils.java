package com.yami.shop.card.common.utils;

import cn.hutool.http.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class GenImgUtils {

    public static BufferedImage genImg(String backUrl, String qrUrlContent, String txt, String money, String timeStr) throws Exception {
//        File imgFile = downloadAndGetCacheFile("https://mall.lzjczl.com/imgApi/2023/09/ff5e6d254c764ffca9987049ae5a2bf8.jpg"); // 下载图片
        File imgFile = downloadAndGetCacheFile(backUrl); // 下载图片
        BufferedImage bufferedImage = ImageIO.read(imgFile);
        Graphics2D graphic = bufferedImage.createGraphics();


        BufferedImage waterQrImg = GoogleBarCodeUtils.getBarCode(qrUrlContent);

        waterQrImg = transparentImage(waterQrImg, 10);

        graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1)); //alpha 水印的透明度  0完全透明， 1不透明
        graphic.drawImage(waterQrImg, 160, 120, waterQrImg.getWidth(), waterQrImg.getHeight(), null);
        graphic.dispose();

        int startX1 = 0;
        int startX2 = 0;
        int startX3 = 0;
        if(txt.length() == 3) {
            startX1 = 50;
            startX2 = 90;
            startX3 = 105;
        }else if(txt.length() == 4) {
            startX1 = 45;
            startX2 = 95;
            startX3 = 110;
        }else if(txt.length() == 5) {
            startX1 = 40;
            startX2 = 95;
            startX3 = 105;
        }else if(txt.length() == 6) {
            startX1 = 30;
            startX2 = 100;
            startX3 = 110;
        }else if(txt.length() == 7) {
            startX1 = 25;
            startX2 = 110;
            startX3 = 120;
        }

        //设置：编号
        graphic = bufferedImage.createGraphics();
        GoogleBarCodeUtils.setGraphics2D(graphic);
        graphic.setColor(new Color(160, 42, 42));
        Font qrIdFont = new Font("微软雅黑", Font.PLAIN, 16);
        graphic.setFont(qrIdFont); //字体、字型、字号
        graphic.drawString(txt, startX1, 160); //画文字
        graphic.drawRenderedImage(bufferedImage, null);
        graphic.dispose();

        graphic = bufferedImage.createGraphics();
        GoogleBarCodeUtils.setGraphics2D(graphic);
        graphic.setColor(Color.BLACK);//背景设置为白色
        qrIdFont = new Font("微软雅黑", Font.PLAIN, 16);
        graphic.setFont(qrIdFont); //字体、字型、字号
        graphic.drawString("/", startX2, 160); //画文字
        graphic.drawRenderedImage(bufferedImage, null);
        graphic.dispose();


        graphic = bufferedImage.createGraphics();
        GoogleBarCodeUtils.setGraphics2D(graphic);
        graphic.setColor(Color.BLACK);//背景设置为白色
        qrIdFont = new Font("微软雅黑", Font.PLAIN, 16);
        graphic.setFont(qrIdFont); //字体、字型、字号
        graphic.drawString(money, startX3, 160); //画文字
        graphic.drawRenderedImage(bufferedImage, null);
        graphic.dispose();



        graphic = bufferedImage.createGraphics();
        GoogleBarCodeUtils.setGraphics2D(graphic);
        graphic.setColor(Color.BLACK);//背景设置为白色
        qrIdFont = new Font("微软雅黑", Font.PLAIN, 20);
        graphic.setFont(qrIdFont); //字体、字型、字号
        graphic.drawString(qrUrlContent, 230, 190); //画文字
        graphic.drawRenderedImage(bufferedImage, null);
        graphic.dispose();


        graphic = bufferedImage.createGraphics();
        GoogleBarCodeUtils.setGraphics2D(graphic);
        graphic.setColor(Color.BLACK);//背景设置为白色
        qrIdFont = new Font("微软雅黑", Font.PLAIN, 15);
        graphic.setFont(qrIdFont); //字体、字型、字号
        graphic.drawString(timeStr, 232, 364); //画文字
        graphic.drawRenderedImage(bufferedImage, null);
        graphic.dispose();
        return bufferedImage;
    }

    public static BufferedImage transparentImage(BufferedImage srcImage, int alpha) throws IOException {
        int imgHeight = srcImage.getHeight();//取得图片的长和宽
        int imgWidth = srcImage.getWidth();
        int c = srcImage.getRGB(3, 3);
        //防止越位
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 10) {
            alpha = 10;
        }
        BufferedImage tmpImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_4BYTE_ABGR);//新建一个类型支持透明的BufferedImage
        for (int i = 0; i < imgWidth; ++i)//把原图片的内容复制到新的图片，同时把背景设为透明
        {
            for (int j = 0; j < imgHeight; ++j) {
                //把背景设为透明
                if (srcImage.getRGB(i, j) == c) {
                    tmpImg.setRGB(i, j, c & 0x00ffffff);
                }
                //设置透明度
                else {
                    int rgb = tmpImg.getRGB(i, j);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    tmpImg.setRGB(i, j, rgb);
                }
            }
        }
        return tmpImg;
    }


    public static String convertImgBase64(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//io流
        ImageIO.write(bufferedImage, "jpg", baos);//写入流中
        byte[] bytes = baos.toByteArray();//转换成字节
        BASE64Encoder encoder = new BASE64Encoder();
        String imgBase64 = encoder.encodeBuffer(bytes).trim();//转换成base64串
        return imgBase64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
    }

    /**
     * 下载文件进行缓存 & 获取图片
     **/
    public static File downloadAndGetCacheFile(String url) {

        // 下载地址为空
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        String fileSavePath = "/vdb/mall4jFile/cardImg/";
        String filePath = fileSavePath + getUrlFileName(url);
        // 存在
        if (new File(filePath).exists()) {
            return new File(filePath);
        }

        // 下载
        HttpUtil.downloadFile(url, filePath);
        return new File(filePath);
    }

    public static String getUrlFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
