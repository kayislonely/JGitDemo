/*------------------------------------------------------------------------------
 *******************************************************************************
 * fengyuansu
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.cmss.git.util;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarToolUtil {

    public final static Logger LOGGER = LoggerFactory.getLogger(JarToolUtil.class);

    /**
     * 获取jar绝对路径
     *
     * @return
     */
    public static String getJarPath() {

        final File file = getFile();
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取jar目录
     *
     * @return
     */
    public static String getJarDir() {

        final File file = getFile();
        if (file == null) {
            return null;
        }
        String filePath = file.getAbsolutePath();
        filePath = filePath.substring(0, filePath.lastIndexOf("\\") + 1);
        return filePath;
    }

    /**
     * 获取jar包名
     *
     * @return
     */
    public static String getJarName() {

        final File file = getFile();
        if (file == null) {
            return null;
        }
        return file.getName();
    }

    /**
     * 获取当前Jar文件
     *
     * @return
     */
    private static File getFile() {

        final URL url = JarToolUtil.class.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = java.net.URLDecoder.decode(url.getPath(), "UTF-8"); // 转换处理中文及空格
        } catch (final java.io.UnsupportedEncodingException e) {
            return null;
        }
        final File file = new File(path);
        return file;
    }
}
