#!/bin/bash
# log.out 日志分割 1 day  是当前时间-1天，0当前时间，1当前时间+1天，例如：当前2023-05-24，结果就是2023-05-23

#*/5 * * * *
#0 1 * * * /data/wwwroot/filcat-api.filcatswap.com/cleanlog.sh &>/var/log/ntpdate.log
this_path=/data/wwwroot/xms-api-demo/logs/
#日志所在路径
current_date=`date -d "-1 day" "+%Y-%m-%d"`
# 定义日志文件名称
log_name=xms-app
#时间
cd $this_path
echo $this_path
echo $current_date
echo $log_name

log_split(){
	#判断在this_path下面是否有//${current_date}文件夹，没有的话会进行创建，mkdir  -p 如果上级文件夹不存在，也会创建
    [ ! -d ${current_date} ] && mkdir -p ${current_date}
	split -b 300M -d -a 4 ./${log_name}.log ./${current_date}/
	#切分日志，100M每块，到log文件中，格式为：2023-05-23/0001，4表示文件名是从0000开始累加的
    if [ $? -eq 0 ];then
        echo "切割完成!"
    else
        echo "切割失败!"
        exit 1
    fi
}

del_log(){
	#清空日志文件
    cat /dev/null > ${log_name}.log
    #删除60天的日志
    cd $this_path
	log=$(date -d "60 day ago " +%Y-%m-%d)
	echo ${log}
	echo 'sys-error.'${log}'.log'
	echo 'sys-info.'${log}'.log'
	echo 'sys-user.'${log}'.log'

	rm -rf ${log}
	rm -rf 'sys-error.'${log}'.log'
	rm -rf 'sys-info.'${log}'.log'
	rm -rf 'sys-user.'${log}'.log'
	echo  ${log_name} '日志 delete 完成'
}

if log_split;then
    del_log
    echo "处理完成" > split.log
else
    echo "处理失败" > split.log
    exit 2
fi



# 处理admin 管理后台的
this_path=/data/wwwroot/xms-admin-demo/logs/
# 定义日志文件名称
log_name=xms-admin

#admin切割的干活
if log_split;then
    del_log
    echo "处理完成" > split.log
else
    echo "处理失败" > split.log
    exit 2
fi


