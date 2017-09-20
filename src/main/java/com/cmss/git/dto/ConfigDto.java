/*------------------------------------------------------------------------------
 *******************************************************************************
 * fengyuansu
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.cmss.git.dto;

import java.util.List;

public class ConfigDto {

    private String since = "2017-08-01 00:00:00";

    private String until = "2017-08-31 23:59:59";

    private List<String> branchList;

    private final String targetFile = "result.csv";

    private List<String> repoDirList;

    private String detailLog = "no";

    /**
     * @return the detailLog
     */
    public String getDetailLog() {

        return detailLog;
    }

    /**
     * @param detailLog the detailLog to set
     */
    public void setDetailLog(final String detailLog) {

        this.detailLog = detailLog;
    }

    /**
     * @return the since
     */
    public String getSince() {

        return since;
    }

    /**
     * @param since the since to set
     */
    public void setSince(final String since) {

        this.since = since;
    }

    /**
     * @return the until
     */
    public String getUntil() {

        return until;
    }

    /**
     * @param until the until to set
     */
    public void setUntil(final String until) {

        this.until = until;
    }

    /**
     * @return the branchList
     */
    public List<String> getBranchList() {

        return branchList;
    }

    /**
     * @param branchList the branchList to set
     */
    public void setBranchList(final List<String> branchList) {

        this.branchList = branchList;
    }

    /**
     * @return the repoDirList
     */
    public List<String> getRepoDirList() {

        return repoDirList;
    }

    /**
     * @param repoDirList the repoDirList to set
     */
    public void setRepoDirList(final List<String> repoDirList) {

        this.repoDirList = repoDirList;
    }

    /**
     * @return the targetFile
     */
    public String getTargetFile() {

        return targetFile;
    }

}
