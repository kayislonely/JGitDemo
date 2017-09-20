# JGitDemo
##1.概述

  本工具可以分析一定时间段内，多个git库下的多个分支，最终输出每个提交者的增加和减少的代码量.
  输出结果为csv表格，位于target文件夹，header为"Repository", "branch", "author", "addSize", "subSize"
  因为需要拉取配置的所有库和分支，所以运行时间可能会比较长，看到“done , target file is”字样表示完成
  
##2.依赖JDK1.8

  第一次运行请先编译，确保配置文件正确编译至target
  
##3.配置信息说明

  conf.json是jar依赖的配置文件，注意修改时需保证配置文件内为Json

  |数据键名 |说明|
  |:------|:-----|
  |branchList|需要分析的分支列表|
  |repoDirList|需要分析的git库列表，绝对路径，到git库的.git文件夹|
  |since|需分析的提交起始时间|
  |until|需分析的提交结束时间|
  |targetFile|输出结果的文件后缀|
  |detailLog|是否需要输出详细log，"yes"：需要，"no"：不需要|
  
##4.注意事项

###4.1
  
  使用时最好不要随意关闭Cmd，可能会导致git处于拉取或者切换中的状态
  如果出现，If no other git process is currently running, this probably means a
  git process crashed in this repository earlier. Make sure no other git
  process is running and remove the file manually to continue.
  可以到对应的git本地库下执行 rm -f ./.git/index.lock   即可
