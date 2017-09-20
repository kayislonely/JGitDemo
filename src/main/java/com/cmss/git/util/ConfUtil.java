/*------------------------------------------------------------------------------
 *******************************************************************************
 * fengyuansu
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.cmss.git.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cmss.git.dto.ConfigDto;

public class ConfUtil {

    public final static Logger LOGGER = LoggerFactory.getLogger(ConfUtil.class);

    private static String CONF_FILE_NAME = "conf.json";

    private static ConfigDto CFG = null;

    public static ConfigDto getConfObj() {

        if (CFG == null) {
            synchronized (ConfUtil.class) {
                if (CFG == null) {
                    // 如果是编译成jar运行，替换成readFileForJar
                    final String fileContent = readFileForIDE();
                    CFG = JSON.parseObject(fileContent, ConfigDto.class);
                }
            }
        }
        return CFG;

    }

    public static String readFileForJar() {

        final String confFilePath = JarToolUtil.getJarDir() + CONF_FILE_NAME;
        LOGGER.info("config file is :" + confFilePath);

        final File file = new File(confFilePath);
        BufferedReader reader = null;
        final StringBuffer contentSb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            final int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                contentSb.append(tempString);
            }
        } catch (final IOException e) {
            LOGGER.error("readFile error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e1) {
                }
            }
        }
        return contentSb.toString();
    }

    public static String readFileForIDE() {

        final URL fileUrl = ConfUtil.class.getClassLoader().getResource(CONF_FILE_NAME);
        final String filePath = fileUrl.getPath();
        final File file = new File(filePath);
        BufferedReader reader = null;
        final StringBuffer contentSb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            final int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                contentSb.append(tempString);
            }
        } catch (final IOException e) {
            LOGGER.error("readFile error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e1) {
                }
            }
        }
        return contentSb.toString();
    }
}
