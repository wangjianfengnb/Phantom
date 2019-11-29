#!/bin/bash


if [ $# -lt 1 ];
then
    echo "USAGE $0 [modulename] classname [-daemon]"
    exit 1
fi


base_dir=$(dirname $0)/..

module=$1;

echo ${module}

if [ $module == 'acceptor' ]; then
    for file in "$base_dir"/plantom-acceptor/target/*.jar;
    do
        CLASSPATH=${CLASSPATH}:"$file"
    done
fi

if [ $module == 'business' ]; then
    for file in "$base_dir"/plantom-business/target/*.jar;
    do
        CLASSPATH=${CLASSPATH}:"$file"
    done
fi

if [ $module == 'dispatcher' ]; then
    for file in "$base_dir"/plantom-dispatcher/target/*.jar;
    do
        CLASSPATH=${CLASSPATH}:"$file"
    done
fi

echo "classpath = "${CLASSPATH};

if [ $# -gt 1 ] && [ $3 == '-daemon' ];
then
    DAEMON=true;
fi

if [ $DEAMON ] ; then
    nohup java -cp ${CLASSPATH} "$2" >/dev/null 2>&1 &
else
    exec java -cp ${CLASSPATH} "$2"
fi