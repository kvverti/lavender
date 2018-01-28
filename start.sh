#!/bin/bash
args=()
jvmargs=()
filepath=""
while [ $# -gt 0 ] && [ "$1" != "--" ]
do
    args+=("$1")
    if [ "$1" = "-fp" ]
    then
        filepath="$2"
    fi
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

java -cp classes:"$filepath" "${jvmargs[@]}" Main "${args[@]}"
