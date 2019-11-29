#!/bin/sh
base_dir=$(dirname $0)

echo $base_dir

for file in "$base_dir"/../phantom-acceptor/target/phantom-acceptor*.jar;
do
    CLASSPATH=${CLASSPATH}:"$file"
done

echo "classpath = "${CLASSPATH};

exec java -cp ${CLASSPATH} com.phantom.acceptor.Bootstrap