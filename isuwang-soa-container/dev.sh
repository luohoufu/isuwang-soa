#!/usr/bin/env bash

#脚本目录
basedir=`pwd`
dirname $0|grep "^/" >/dev/null
if [ $? -eq 0 ];then
   basedir=`dirname $0`
else
    dirname $0|grep "^\." >/dev/null
    retval=$?
    if [ $retval -eq 0 ];then
        basedir=`dirname $0|sed "s#^.#$basedir#"`
    else
        basedir=`dirname $0|sed "s#^#$basedir/#"`
    fi
fi

# soa-parent
soaparentdir=${basedir}/../
cd ${soaparentdir}
mvn clean install

# container
containerdir=${basedir}
cd ${containerdir}
mvn clean package -Pdev

# engine
enginedir=${basedir}/../isuwang-soa-bootstrap
cd ${enginedir}
mvn clean package -Pdev

cp ${enginedir}/target/isuwang-soa-bootstrap*.jar ${containerdir}/target/isuwang-soa-container/bin/
