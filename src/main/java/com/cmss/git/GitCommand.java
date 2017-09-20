/*------------------------------------------------------------------------------
 *******************************************************************************
 * fengyaunsu
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.cmss.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gitective.core.filter.commit.CommitDiffFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.cmss.git.dto.ConfigDto;
import com.cmss.git.util.ConfUtil;
import com.cmss.git.util.CsvUtil;

public class GitCommand {

    final static Logger LOGGER = LoggerFactory.getLogger(GitCommand.class);

    private static String ADD_SIZE_KEY = "addSize";

    private static String SUB_SIZE_KEY = "subSize";

    private static List<String> REMOTE_BRANCH_LIST = null;

    private static List<String> LOCAL_BRANCH_LIST = null;

    private static String DETAIL_LOG_YES = "yes";

    public static void main(final String[] args) throws Exception {

        LOGGER.info("begin to analyze");
        init();
        // 全局信息Map
        final Map<String, Map<String, Map<String, Map<String, Integer>>>> allMap = new HashMap<String, Map<String, Map<String, Map<String, Integer>>>>();
        // 获取repo信息
        final List<String> repoDirList = ConfUtil.getConfObj().getRepoDirList();
        // 遍历每个repo处理
        for (final String repoDir : repoDirList) {
            LOGGER.info("start : build git Obj for " + repoDir);
            // 创建Git对象
            final FileRepositoryBuilder builder = new FileRepositoryBuilder();
            final Repository repository = builder.setGitDir(new File(repoDir)).readEnvironment().findGitDir().build();
            final Git git = new Git(repository);
            LOGGER.info("completed : build git Obj for " + repoDir);
            // 每个repo信息Map
            final Map<String, Map<String, Map<String, Integer>>> totalMap = new HashMap<String, Map<String, Map<String, Integer>>>();
            REMOTE_BRANCH_LIST = getRemoteBranchNames(git);
            LOCAL_BRANCH_LIST = getLocalBranchNames(git);
            final List<String> branchList = ConfUtil.getConfObj().getBranchList();
            // 遍历每个branch处理
            LOGGER.info("start : analyze " + repoDir);
            for (final String targetBranch : branchList) {
                LOGGER.info("    begin : analyze " + targetBranch);
                final Map<String, Map<String, Integer>> branchMap = getInfoForBranch(git, targetBranch);
                totalMap.put(targetBranch, branchMap);
                LOGGER.info("    end : analyze " + targetBranch);
            }
            // 计算出repo的文件夹名，作为Map的key
            final String[] wordArray = repoDir.split("/");
            final String folder = wordArray[wordArray.length - 2];
            allMap.put(folder, totalMap);
            LOGGER.info("end : analyze " + repoDir);
        }
        genCsvResult(allMap);

    }

    /**
     * @param allMap
     * @throws IOException
     */
    private static void genCsvResult(final Map<String, Map<String, Map<String, Map<String, Integer>>>> allMap)
            throws IOException {

        CsvUtil.writeResultCsv(allMap);

    }

    /**
     * 初始化配置信息
     */
    private static void init() {

        // 获取配置信息
        final ConfigDto cfg = ConfUtil.getConfObj();
        LOGGER.info("since : " + ConfUtil.getConfObj().getSince());
        LOGGER.info("until : " + ConfUtil.getConfObj().getUntil());
        LOGGER.info("detailLog switch : " + ConfUtil.getConfObj().getDetailLog());

    }

    public static Map getInfoForBranch(final Git git, final String branch) throws ParseException, NoHeadException,
            GitAPIException, LargeObjectException, MissingObjectException, IOException {

        // 如果本项目远程无此分支，跳过处理
        if (!REMOTE_BRANCH_LIST.contains(branch)) {
            return null;
        }
        // 拉取分支
        // 如果不是本地分支则拉取；是本地分支则切换分支
        if (!getCurrentBranchName(git).equals(branch)) {
            final CheckoutCommand checkoutCommand = git.checkout();
            // 如果不是本地分支
            if (!LOCAL_BRANCH_LIST.contains(branch)) {
                checkoutCommand.setCreateBranch(true);
                checkoutCommand.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
                checkoutCommand.setStartPoint("origin/" + branch);
            }
            checkoutCommand.setName(branch);
            LOGGER.info("   start : checkout for " + branch);
            checkoutCommand.call();
            LOGGER.info("   end : checkout for " + branch);
        }
        // 拉取最新的代码
        LOGGER.info("   start : pull remote for " + branch);
        git.pull().call();
        LOGGER.info("   end : pull remote for " + branch);

        final Map<String, Map<String, Integer>> branchMap = new HashMap<String, Map<String, Integer>>();

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Date since = sdf.parse(ConfUtil.getConfObj().getSince() + " 00:00:00");
        final Date until = sdf.parse(ConfUtil.getConfObj().getUntil() + " 23:59:59");

        // 设置时间过滤器，获取时间段内的所有提交
        final RevFilter commitTimeRevFilter = CommitTimeRevFilter.between(since, until);
        final CommitDiffFilter commitDiffFilter = new CommitDiffFilter();
        final LogCommand logCommand = git.log();
        logCommand.setRevFilter(commitTimeRevFilter);
        // logCommand.setRevFilter(RevFilter.NO_MERGES).setRevFilter(commitDiffFilter);
        final Iterable<RevCommit> log = logCommand.call();
        final Iterator<RevCommit> logIterator = log.iterator();
        while (logIterator.hasNext()) {
            final RevCommit revCommit = logIterator.next();
            // 过滤掉Merge操作
            if (revCommit.getParentCount() >= 2) {
                continue;
            }
            final String authorName = revCommit.getAuthorIdent().getName();
            final String commitTime = sdf.format(new Date(Long.valueOf(revCommit.getCommitTime()) * 1000L));
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final DiffFormatter df = new DiffFormatter(out);
            df.setRepository(git.getRepository());

            // 忽略第一次提交
            if (revCommit.getParentCount() == 0) {
                continue;
            }
            // 对比提交，算出差异量
            final List<DiffEntry> diffList = df.scan(revCommit.getId(), revCommit.getParent(0).getId());

            for (final DiffEntry diffEntry : diffList) {
                int addSize = 0;
                int subSize = 0;
                final FileHeader fileHeader = df.toFileHeader(diffEntry);
                final List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
                for (final HunkHeader hunkHeader : hunks) {
                    final EditList editList = hunkHeader.toEditList();
                    for (final Edit edit : editList) {
                        addSize += edit.getEndA() - edit.getBeginA();
                        subSize += edit.getEndB() - edit.getBeginB();
                    }
                }
                if (null == branchMap.get(authorName)) {
                    final Map<String, Integer> authorMap = new HashMap<String, Integer>();
                    branchMap.put(authorName, authorMap);
                    authorMap.put(ADD_SIZE_KEY, 0);
                    authorMap.put("subSize", 0);
                }
                final Map<String, Integer> authorMap = branchMap.get(authorName);
                Integer authorAddSize = authorMap.get(ADD_SIZE_KEY);
                authorAddSize += addSize;
                authorMap.put(ADD_SIZE_KEY, authorAddSize);
                Integer authorsubSize = authorMap.get(SUB_SIZE_KEY);
                authorsubSize += subSize;
                authorMap.put(SUB_SIZE_KEY, authorsubSize);
                if (DETAIL_LOG_YES.equals(ConfUtil.getConfObj().getDetailLog().toLowerCase())) {
                    LOGGER.info(commitTime + ", Author :" + authorName + ", subSize :" + subSize + " , addSize : "
                            + addSize);
                }

            }

        }
        return branchMap;
    }

    /**
     * 获取所有本地分支
     * @param git
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static List<String> getLocalBranchNames(final Git git) throws IOException, GitAPIException {

        final List<String> result = new LinkedList<String>();
        final List<Ref> branchList = git.branchList().call();
        for (final Ref ref : branchList) {
            final String branchName = ref.getName();
            if (branchName.indexOf("refs/heads") > -1) {
                final String el = branchName.substring(branchName.lastIndexOf("/") + 1, branchName.length());
                result.add(el);
            }

        }
        return result;
    }

    /**
     * 获取所有远程分支
     * @param git
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static List<String> getRemoteBranchNames(final Git git) throws IOException, GitAPIException {

        final List<String> result = new LinkedList<String>();
        final Map<String, Ref> map = git.getRepository().getAllRefs();
        final Set<String> keys = map.keySet();
        final String index = "refs/remotes/";
        for (final String branchName : keys) {
            if (branchName.indexOf(index) > -1) {
                final String el = branchName.substring(branchName.lastIndexOf("/") + 1, branchName.length());
                result.add(el);
            }
        }
        return result;
    }

    /**
     * 获取当前分支
     * @param git
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static String getCurrentBranchName(final Git git) throws IOException, GitAPIException {

        final String branchName = git.getRepository().getBranch();
        return branchName;
    }

    /**
     * @param totalMap
     */
    private static void resovleMap(final Map<String, Map<String, Map<String, Integer>>> totalMap) {

        for (final String branchKey : totalMap.keySet()) {
            final Map<String, Map<String, Integer>> branchMap = totalMap.get(branchKey);
            LOGGER.info(branchKey);
            if (CollectionUtils.isEmpty(branchMap)) {
                continue;
            }
            for (final String authorKey : branchMap.keySet()) {
                LOGGER.info("author : " + authorKey + "  add : " + branchMap.get(authorKey).get(ADD_SIZE_KEY)
                        + "  delete : " + branchMap.get(authorKey).get(SUB_SIZE_KEY));
            }
        }

    }

}
