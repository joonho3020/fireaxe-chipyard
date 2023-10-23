#!/usr/bin/env bash

set -e

echo  "Building embench-iot for x86"
BUILDDIR=$(pwd)/build-x86
mkdir -p $BUILDDIR

cd embench-iot
# use the riscv32 target, but use riscv64 compiler
./build_all.py --arch native --chip default --board default --cc gcc --cflags="-c -O2 -ffunction-sections" --user-libs="-lm" --clean -v

echo "Copying binaries to $BUILDDIR"
bmarks=("aha-mont64" "crc32" "cubic" "edn" "huffbench"
        "matmult-int" "minver" "nbody" "nettle-aes"
        "nettle-sha256" "nsichneu" "picojpeg"
        "qrduino" "sglib-combined" "slre" "st"
        "statemate" "ud" "wikisort")
for bmark in "${bmarks[@]}"
do
    cp bd/src/$bmark/$bmark $BUILDDIR/
done

