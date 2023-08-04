

conda activate /bwrcq/scratch/skarandikar/tapeout/hyperscale-soc-chipyard-tapeout/.conda-env

./scripts/build-setup.sh --force -s 1 -s 8

source env.sh

./scripts/init-vlsi.sh intech22
