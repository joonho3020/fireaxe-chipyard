#!/usr/bin/env bash

THISDIR="$1"
DATE=$(date +%Y-%m-%d__%H-%M-%S)
#CONFIG_TO_RUN=HyperscaleSoCTapeout
CONFIG_TO_RUN=HyperscaleSoCRocketClientNodeConfig
MY_BUILD_DIR=build26_800M_300delay_registeredhist

#TOP_TO_RUN=SnappyDecompressor

mkdir -p $HOME/.calibrewb_workspace/tmp


{

source /users/skarandikar/.bashrc
source /tools/flexlm/flexlm.sh

ulimit -n $(ulimit -Hn)
ulimit -u $(ulimit -Hu)


cd $THISDIR/../
source env.sh
cd vlsi

#make -j32 buildfile CONFIG=$CONFIG_TO_RUN

#make -j32 syn-ChipTop CONFIG=$CONFIG_TO_RUN

#make -j32 redo-par-RocketTile CONFIG=$CONFIG_TO_RUN HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
#make -j32 redo-syn-ChipTop CONFIG=$CONFIG_TO_RUN HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
#make -j32 redo-syn-to-par-ChipTop CONFIG=$CONFIG_TO_RUN HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
make -j32 par-ChipTop CONFIG=$CONFIG_TO_RUN VLSI_OBJ_DIR=$MY_BUILD_DIR #HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
make -j32 redo-par-to-drc-ChipTop CONFIG=$CONFIG_TO_RUN VLSI_OBJ_DIR=$MY_BUILD_DIR HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"

make -j32 fill CONFIG=$CONFIG_TO_RUN HAMMER_LVS_TARGET=lvs-ChipTop HAMMER_DRC_TARGET=drc-ChipTop VLSI_OBJ_DIR=$MY_BUILD_DIR #HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
make -j32 drc-final-drcd CONFIG=$CONFIG_TO_RUN HAMMER_LVS_TARGET=lvs-ChipTop HAMMER_DRC_TARGET=drc-ChipTop VLSI_OBJ_DIR=$MY_BUILD_DIR #HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"
make -j32 drc-final-fullchip CONFIG=$CONFIG_TO_RUN HAMMER_LVS_TARGET=lvs-ChipTop HAMMER_DRC_TARGET=drc-ChipTop VLSI_OBJ_DIR=$MY_BUILD_DIR #HAMMER_EXTRA_ARGS="-p $THISDIR/specs/proj-design.yml"

} 2>&1 | tee $THISDIR/bsub-runlog-$DATE.log
