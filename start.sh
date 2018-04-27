#!/bin/bash
args=()
jvmargs=()
while [ $# -gt 0 ] && [ "$1" != "--" ]
do
    args+=("$1")
    shift
done

if [ "$1" = "--" ]
then
    shift
    while [ $# -gt 0 ]
    do
        jvmargs+=("$1")
        shift
    done
fi

java -cp classes "${jvmargs[@]}" Main "${args[@]}"
