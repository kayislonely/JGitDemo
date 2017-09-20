/*------------------------------------------------------------------------------
 *******************************************************************************
 * fengyuansu
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.cmss.git.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class CsvUtil {

    final static Logger LOGGER = LoggerFactory.getLogger(CsvUtil.class);

    final static String[] FILE_HEADER = { "Repository", "branch", "author", "addSize", "subSize" };

    final static String FILE_NAME = "result.csv";

    private static String ADD_SIZE_KEY = "addSize";

    private static String SUB_SIZE_KEY = "subSize";

    public static void writeResultCsv(final Map<String, Map<String, Map<String, Map<String, Integer>>>> allMap)
            throws IOException {

        final CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER).withRecordSeparator("\n");
        Writer out = null;
        CSVPrinter printer = null;
        try {
            final String timeTag = ConfUtil.getConfObj().getSince() + "~" + ConfUtil.getConfObj().getUntil();
            final String file = JarToolUtil.getJarDir() + timeTag + "-" + FILE_NAME;
            out = new FileWriter(file);
            printer = new CSVPrinter(out, format);
            for (final String folder : allMap.keySet()) {
                final Map<String, Map<String, Map<String, Integer>>> totalMap = allMap.get(folder);
                if (CollectionUtils.isEmpty(totalMap)) {
                    continue;
                }
                for (final String branch : totalMap.keySet()) {
                    final Map<String, Map<String, Integer>> branchMap = totalMap.get(branch);
                    if (CollectionUtils.isEmpty(branchMap)) {
                        continue;
                    }
                    for (final String author : branchMap.keySet()) {
                        final Map<String, Integer> authorMap = branchMap.get(author);
                        if (CollectionUtils.isEmpty(authorMap)) {
                            continue;
                        }

                        final String addSize = String.valueOf(authorMap.get(ADD_SIZE_KEY));
                        final String subSize = String.valueOf(authorMap.get(SUB_SIZE_KEY));
                        printer.printRecord(new String[] { folder, branch, author, addSize, subSize });
                    }

                }

            }
            printer.flush();
            LOGGER.info("done , target file is " + file);
        } catch (final Exception e) {
            LOGGER.error("writeResultCsv error ", e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (printer != null) {
                printer.close();
            }
        }

    }

}
