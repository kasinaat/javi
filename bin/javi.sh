#!/bin/bash
if [ -z "$1" ]; then
        echo "Usage : javi <file_name>"
        exit 1
fi
for i in ../lib/*.jar; do
    CLASSPATH=$CLASSPATH:$i
done
java -cp $CLASSPATH org.taanisak.javi.Main $1