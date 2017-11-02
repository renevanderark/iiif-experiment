#!/usr/bin/env bash

cd src/main/native
if [ "$1" = "2.0" ]; then
    echo "compiling version 2.0"
    gcc -I /usr/local/include/openjpeg-2.0 \
        -I $JAVA_HOME/include -I $JAVA_HOME/include/linux -fPIC \
        -shared -o ../resources/native/libjp2j.so log.c opj_res_2.0.c nl_kb_jp2_Jp2Header.c nl_kb_jp2_Jp2Decode.c \
        -lopenjp2
else
    echo "compiling version 2.3"
    gcc -I /usr/local/include/openjpeg-2.3 \
        -I $JAVA_HOME/include -I $JAVA_HOME/include/linux -fPIC \
        -shared -o ../resources/native/libjp2j.so log.c opj_res.c nl_kb_jp2_Jp2Header.c nl_kb_jp2_Jp2Decode.c \
        -lopenjp2
fi
cd ../../../
