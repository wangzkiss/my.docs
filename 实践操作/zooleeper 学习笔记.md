zooleeper 学习笔记
zkCli.sh 进入控制台
查看节点信息    
ls 和 ls2 命令
查看zookeeper目录（节点信息）
ls2 可查看更详细的信息

get  和  stat
获取数据和状态数据
查看状态数据

常用命令
create  [-s] [-e]  path data
-e 临时节点  
-s 顺序节点
set path data [version]
设置值
delete path  [version]  
删除【节点】


watcher  监督机制
1.增删改节点都会触发watcher事件
2.事件根据不同的操作会有不同

事件类型
nodecreateed 节点创建
nodedatachanged 修改节点
nodedeleted  删除节点


权限控制acl
getAcl 获取权限
setAcl 设置权限
crdwa  
