#----------------------------------------------------------------------------------------
# common gcc configuration/optimization
#----------------------------------------------------------------------------------------
SIM_OPT_CXXFLAGS := -O3

SIM_CXXFLAGS = \
	$(CXXFLAGS) \
	$(SIM_OPT_CXXFLAGS) \
	-std=c++17 \
	-I$(RISCV)/include \
	-I$(dramsim_dir) \
	-I$(OUT_DIR) \
	$(EXTRA_SIM_CXXFLAGS)

SIM_LDFLAGS = \
	$(LDFLAGS) \
	-L$(RISCV)/lib \
	-Wl,-rpath,$(RISCV)/lib \
	-L$(sim_dir) \
	-L$(dramsim_dir) \
	-lfesvr \
	-ldramsim \
	$(EXTRA_SIM_LDFLAGS)

SIM_FILE_REQS += \
	$(ROCKETCHIP_RSRCS_DIR)/vsrc/EICG_wrapper.v
