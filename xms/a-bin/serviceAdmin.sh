# jar名称
APP_NAME=xms-admin
#设置jvm调优参数，环境不同，读取对应的配置文件不同，需要改造
JAVA_OPTS='-Dspring.profiles.active=test -verbose:gc -Xms512M -Xmx512M -Xmn200M -XX:SurvivorRatio=8 -XX:MetaspaceSize=64M -XX:MaxMetaspaceSize=128M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs/xms-oom.dump -XX:+PrintGCDetails'

#使用说明，用来提示输入参数
usage() {
echo "Usage: sh 执行脚本.sh [start|stop|restart|status]"
exit 1
}

#检查程序是否在运行
is_exist(){
pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
#如果不存在返回1，存在返回0
if [ -z "${pid}" ]; then
return 1
else
return 0
fi
}

#启动方法
start(){
is_exist
if [ $? -eq "0" ]; then
echo "${APP_NAME} is already running. pid=${pid} ."
else
 nohup java $JAVA_OPTS -jar $APP_NAME.jar >>logs/$APP_NAME.log 2>&1 &
fi
}

#停止方法
stop(){
is_exist
if [ $? -eq "0" ]; then
kill -9 $pid
else
echo "${APP_NAME} is not running"
fi
}

#输出运行状态
status(){
is_exist
if [ $? -eq "0" ]; then
echo "${APP_NAME} is running. Pid is ${pid}"
else
echo "${APP_NAME} is NOT running."
fi
}

#重启
restart(){
stop
start
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
"start")
start
;;
"stop")
stop
;;
"status")
status
;;
"restart")
restart
;;
*)
usage
;;
esac
