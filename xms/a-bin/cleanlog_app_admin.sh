#!/bin/bash
# log.out 日志分割 1 day  是当前时间-1天，0当前时间，1当前时间+1天，例如：当前2023-05-24，结果就是2023-05-23
this_path=/data/wwwroot/server/logs/
#日志所在路径
current_date=`date -d "-1 day" "+%Y-%m-%d"`
#时间
cd $this_path
echo $this_path
echo $current_date

log_split(){
	#判断在this_path下面是否有//${current_date}文件夹，没有的话会进行创建，mkdir  -p 如果上级文件夹不存在，也会创建
    [ ! -d ${current_date} ] && mkdir -p ${current_date} 
    	mkdir -p ${current_date}/app-logs
	mkdir -p ${current_date}/admin-logs
	split -b 300M -d -a 4 ./aleo-app.log ./${current_date}/app-logs/
	split -b 300M -d -a 4 ./aleo-admin.log ./${current_date}/admin-logs/
	#切分日志，100M每块，到log文件中，格式为：2023-05-23/0001，4表示文件名是从0000开始累加的
    if [ $? -eq 0 ];then
        echo "切割完成!"
    else
        echo "切割失败!"
        exit 1
    fi
}
 
del_log(){
    cat /dev/null > aleo-app.log 
    cat /dev/null > aleo-admin.log
	#清空日志文件
    # > log.out
}
 
if log_split;then
    del_log
    echo "处理完成" > split.log
else
    echo "处理失败" > split.log
    exit 2
fi

#删除10天前的日志
cd $this_path
log=$(date -d "30 day ago " +%Y-%m-%d)
rm -rf ${log}
echo "delete 完成!"

