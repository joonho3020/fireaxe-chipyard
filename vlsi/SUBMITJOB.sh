#!/usr/bin/env bash

THISDIR=$(pwd)

bsub -n 24 -I -q tapeout "bash -c \"$THISDIR/RUNNER.sh $THISDIR\""
