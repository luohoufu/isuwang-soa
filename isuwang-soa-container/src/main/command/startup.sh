#!/usr/bin/env bash

workdir=`pwd`
dirname $0|grep "^/" >/dev/null
if [ $? -eq 0 ];then
   workdir=`dirname $0`
else
    dirname $0|grep "^\." >/dev/null
    retval=$?
    if [ $retval -eq 0 ];then
        workdir=`dirname $0|sed "s#^.#$workdir#"`
    else
        workdir=`dirname $0|sed "s#^#$workdir/#"`
    fi
fi

cd $workdir

logdir=$workdir/../logs
if [ ! -d "$logdir" ]; then
	mkdir "$logdir"
fi

jvm_opts="-Xms256m -Xmx256m -Xloggc:$logdir/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGC"
debug_opts="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5000"
soa_base="-Dsoa.base=$workdir/../ -Dsoa.run.mode=native"

nohup java $jvm_opts $soa_base $debug_opts -cp ./isuwang-soa-engine.jar com.isuwang.soa.engine.Engine >> $logdir/catalina.out 2>&1 &
echo $! > $logdir/pid.txt