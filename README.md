# quartz-springboot-start
这个项目是为了解决定时任务中日志记录查看的问题；
为了不从众多日志中翻看定时任务的日志;
采用quartz 来做定时任务，采用其数据库来存储任务属性；
集成springboot的web 和 jdbc 自动配置；
支持mysql 和oracle 数据库；
最简单的做法仅仅是将此包导入springboot项目路径下；
然后通过http://xxx.xxx.xxx.xxx:port/{projectName}/quartz 来访问控制台；
web端水平有限，页面样式做的不怎么滴，敬请使用
