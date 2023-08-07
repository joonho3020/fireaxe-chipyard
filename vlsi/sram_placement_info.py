
from collections import defaultdict
import json

CORE = "Rocket" # OR "MegaBoom"
NODE = "Client" # OR "Server"

CONFIGNAME=f"HyperscaleSoC{CORE}{NODE}NodeConfig"
tile_path_name=""
tile_module_name=""

# used for checking bounds violations
tile_max_x_raw = 1860.192
tile_max_y_raw = 2002.41

extra_margin = 15

tile_max_x = tile_max_x_raw - extra_margin
tile_max_y = tile_max_y_raw - extra_margin


# TODO NOT REAL
chip_max_x = 2000
chip_max_y = 2000

if CORE == "Rocket":
    tile_path_name = "tile_reset_domain_tile"
    tile_module_name="RocketTile"
elif CORE == "MegaBoom":
    tile_path_name = "tile_reset_domain_boom_tile"
    tile_module_name="BoomTile"

gen_col_mems_v = f"generated-src/chipyard.harness.TestHarness.{CONFIGNAME}/gen-collateral/chipyard.harness.TestHarness.{CONFIGNAME}.top.mems.v"

leaf_name_to_mems = defaultdict(list)
srams_set = set()

x_snap = 0.108
y_snap = 0.09


def round_yval(orig_yval):
    return round((int(orig_yval / y_snap) + 1) * y_snap, 3)

def round_xval(orig_xval):
    return round((int(orig_xval / x_snap) + 1) * x_snap, 3)



with open(gen_col_mems_v, 'r') as mem_info:
    current_module = None
    for line in mem_info.readlines():
        if line.startswith("module"):
            current_module = line.strip().split()[1].replace("(", "")
        if line.strip().startswith("ip224"):
            this_mem = line.replace("(", "").strip().split()
            sram_name = this_mem[0]
            srams_set.add(sram_name)
            mem_name = this_mem[1]
            leaf_name_to_mems[current_module].append([mem_name, sram_name])

print(leaf_name_to_mems)



full_path_and_module_name = []

seq_mems = f"generated-src/chipyard.harness.TestHarness.{CONFIGNAME}/gen-collateral/metadata/seq_mems.json"
with open(seq_mems, 'r') as seq_mem_info:
    json_data = json.loads(seq_mem_info.read())

    all_module_names = list(map(lambda x: x["module_name"], json_data))
    unique_module_names = set(all_module_names)

    if len(all_module_names) != len(unique_module_names):
        print("ERROR: DUPLICATE MODULES")
        exit(1)

    for item in json_data:
        mod_name = item["module_name"]
        for h in item["hierarchy"]:
            full_path_and_module_name.append([h.replace(".", "/"), mod_name])


sram_info = dict()

for sram in list(sorted(list(srams_set))):
    sram_info_path = "/tools/intech22/local/memory/" + sram + "/physical/lef/" + sram + ".lef"
    with open(sram_info_path, 'r') as sram_info_file:
        for line in sram_info_file.readlines():
            if line.strip().startswith("#"):
                continue
            if line.strip().startswith("SIZE"):
                l = line.replace("SIZE", "").replace(";", "").strip().split(" by ")
                width = float(l[0])
                height = float(l[1])
                sram_info[sram] = { 'width': width, 'height': height }
                break




all_paths = []


for path, module_name in full_path_and_module_name:
    for submem, sram_name in leaf_name_to_mems[module_name]:
        all_paths.append([path + "/" + submem, sram_name, sram_info[sram_name]])

all_paths_start_len = len(all_paths)


print("-------------------------------")
for pair in all_paths:
    print(pair)
print("-------------------------------")


final_placements = []

def filter_out_path(input_array, filter_path, secondary_cond=None):
    filter_match = []
    all_else = []

    for x in input_array:
        if x[0].startswith(filter_path) and (secondary_cond is None or secondary_cond in x[0]):
            filter_match.append(x)
        else:
            all_else.append(x)
    return filter_match, all_else

def identity_path_converter(x):
    return x

def map_grid_helper(similar_sram_paths, x_start, y_start, grid_width, grid_height, path_converter, bounds_check_x, bounds_check_y):
    x_start_rd = round_xval(x_start)
    y_start_rd = round_yval(y_start)

    sr_width = similar_sram_paths[0][2]['width']
    sr_height = similar_sram_paths[0][2]['height']

    x_offset = round_xval(sr_width)
    y_offset = round_yval(sr_height)

    sram_names_check = set(map(lambda x: x[1], similar_sram_paths))
    if len(sram_names_check) != 1:
        print("ERR MULTIPLE SRAM TYPES")
        print(sram_names_check)
        exit(1)

    sram_name = similar_sram_paths[0][1]

    for x_ind in range(grid_width):
        for y_ind in range(grid_height):
            xval = round_xval(x_start_rd + x_offset * x_ind)
            yval = round_yval(y_start_rd + y_offset * y_ind)

            current_path = path_converter(similar_sram_paths[y_ind + x_ind*grid_height][0])

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"

            aligned_right_edge = round_xval(xval + x_offset)
            aligned_top_edge = round_yval(yval + y_offset)
            comment = f"# below sram properties: width: {sr_width}, height: {sr_height}, sram: {sram_name}, x_end: {aligned_right_edge}, y_end: {aligned_top_edge}"
            final_placements.append(comment)
            final_placements.append("- {" + outline + "}")

    x_end = round_xval(x_start_rd + x_offset * grid_width)
    y_end = round_yval(y_start_rd + y_offset * grid_height)

    bounds_fail = False
    if x_end >= bounds_check_x:
        print(f"FAILED X BOUNDS CHECK: {x_end} >= {bounds_check_x}")
        bounds_fail = True
    if y_end >= bounds_check_y:
        print(f"FAILED Y BOUNDS CHECK: {y_end} >= {bounds_check_y}")
        bounds_fail = True
    if bounds_fail:
        print(similar_sram_paths)
        exit(1)

    return x_end, y_end


def convert_inclusivecache(path):
    return path.replace(f"ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched", "InclusiveCacheBankScheduler")



inclusive_cache_max_x = 700.056
inclusive_cache_max_y = 425.07

data_bank_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched/bankedStore/cc_banks")
dir_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched/directory/cc_dir/cc_dir_ext")

def map_l2_data(data_bank_paths, dir_paths):
    x_start = 10
    y_start = 0


    end_x, end_y = map_grid_helper(data_bank_paths, x_start=x_start, y_start=y_start, grid_width=8, grid_height=2, path_converter=convert_inclusivecache, bounds_check_x=inclusive_cache_max_x, bounds_check_y=inclusive_cache_max_y)


    end_x2, end_y2 = map_grid_helper(dir_paths, x_start=x_start, y_start=end_y, grid_width=2, grid_height=2, path_converter=convert_inclusivecache, bounds_check_x=inclusive_cache_max_x, bounds_check_y=inclusive_cache_max_y)


map_l2_data(data_bank_paths, dir_paths)

# get rid of the other paths, since they're hierarch copies
l2_copy_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched_")

for x in l2_copy_paths:
    final_placements.append("# skipped for hierarch")
    final_placements.append("# skipped for hierarch")

def convert_tile(path):
    return path.replace(f"ChipTop/system/tile_prci_domain/{tile_path_name}", tile_module_name)

convert_rockettile = convert_tile


if NODE == "Client":
    decomp_unit_path = "compress_accel_decompressor/command_expander/MEM"
elif NODE == "Server":
    decomp_unit_path = "zstd_decompressor/frame_decompressor_block_decompressor/seqExecWriter/MEM"


decomp_hb_path = f"ChipTop/system/tile_prci_domain/{tile_path_name}/{decomp_unit_path}"

decomp_hist_paths, all_paths = filter_out_path(all_paths, decomp_hb_path)

def map_decomp_hist(decomp_hist):
    x_start = 150
    y_start = 10

    end_x, end_y = map_grid_helper(decomp_hist, x_start=x_start, y_start=y_start, grid_width=8, grid_height=4, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

map_decomp_hist(decomp_hist_paths)


if NODE == "Server":
    # client doesn't have these SRAMs at all
    zstd_extra_path1 = f"ChipTop/system/tile_prci_domain/{tile_path_name}/zstd_decompressor/frame_decompressor_block_decompressor/off_dt"
    zstd_extra_paths1, all_paths = filter_out_path(all_paths, zstd_extra_path1)

    zstd_extra_path2 = f"ChipTop/system/tile_prci_domain/{tile_path_name}/zstd_decompressor/frame_decompressor_block_decompressor"
    zstd_extra_paths2, all_paths = filter_out_path(all_paths, zstd_extra_path2)

    def map_extras(zstd_extra1, zstd_extra2):

        y_start = 250
        x_start = 100

        end_x, end_y = map_grid_helper(zstd_extra1, x_start=x_start, y_start=y_start, grid_width=1, grid_height=2, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

        end_x2, end_y2 = map_grid_helper(zstd_extra2, x_start=end_x, y_start=y_start, grid_width=4, grid_height=1, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

    map_extras(zstd_extra_paths1, zstd_extra_paths2)


if NODE == "Client":
    comp_unit_path = "compress_accel_compressor/lz77hashmatcher/history_buffer"
elif NODE == "Server":
    comp_unit_path = "merged_compressor/matchfinder/lz77hashmatcher/history_buffer"

comp_hb_path = f"ChipTop/system/tile_prci_domain/{tile_path_name}/{comp_unit_path}"


comp_hist_paths, all_paths = filter_out_path(all_paths, comp_hb_path)

comp_hist_paths = list(filter(lambda x: "MEM_0_ext/mem_1_" not in x, comp_hist_paths))


def map_comp_hist(comp_hist):
    x_start = 600+65
    y_start = 10

    end_x, end_y = map_grid_helper(comp_hist, x_start=x_start, y_start=y_start, grid_width=8, grid_height=4, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

map_comp_hist(comp_hist_paths)


if NODE == "Client":
    comp_unit_path2 = "compress_accel_compressor/lz77hashmatcher/hash_table/hash_mem/hash_mem_ext"
elif NODE == "Server":
    comp_unit_path2 = "merged_compressor/matchfinder/lz77hashmatcher/hash_table/hash_mem/hash_mem_ext"

comp_ht_path = f"ChipTop/system/tile_prci_domain/{tile_path_name}/{comp_unit_path2}"

comp_ht_paths, all_paths = filter_out_path(all_paths, comp_ht_path)

def map_comp_ht(comp_ht):
    x_start = 1690 + 100
    y_start = 300

    end_x, end_y = map_grid_helper(comp_ht, x_start=x_start, y_start=y_start, grid_width=1, grid_height=3, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

map_comp_ht(comp_ht_paths)


if CORE == "Rocket":
    dcache_suffix_data = "dcache/data/data_arrays"
    dcache_suffix_tags = "dcache/tag_array/tag_array"
elif CORE == "MegaBoom":
    dcache_suffix_data = "dcache/data/array"
    dcache_suffix_tags = "dcache/meta"

l1_data_bank_paths, all_paths = filter_out_path(all_paths, f"ChipTop/system/tile_prci_domain/{tile_path_name}/{dcache_suffix_data}")
l1_data_tag_paths, all_paths = filter_out_path(all_paths, f"ChipTop/system/tile_prci_domain/{tile_path_name}/{dcache_suffix_tags}")

def map_l1_data(l1_data_bank_paths, l1_data_tag_paths):

    x_start = 400
    y_start = 1865

    end_x, end_y = map_grid_helper(l1_data_bank_paths, x_start=x_start, y_start=y_start, grid_width=16, grid_height=1, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

    x_start2 = end_x + 75
    y_start2 = 1890
    end_x2, end_y2 = map_grid_helper(l1_data_tag_paths, x_start=x_start2, y_start=y_start2, grid_width=2, grid_height=1, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

map_l1_data(l1_data_bank_paths, l1_data_tag_paths)



if CORE == "Rocket":
    icache_suffix_data = "frontend/icache/data_arrays"
    icache_suffix_tags = "frontend/icache/tag_array"
elif CORE == "MegaBoom":
    icache_suffix_data = "frontend/icache/dataArray"
    icache_suffix_tags = "frontend/icache/tag_array/tag_array"

l1_instruction_bank_paths, all_paths = filter_out_path(all_paths, f"ChipTop/system/tile_prci_domain/{tile_path_name}/{icache_suffix_data}")
l1_instruction_tag_paths, all_paths = filter_out_path(all_paths, f"ChipTop/system/tile_prci_domain/{tile_path_name}/{icache_suffix_tags}")

def map_l1_instruction(l1_instruction_bank_paths, l1_instruction_tag_paths):

    x_start = 1550+85
    y_start = 1500+15

    end_x, end_y = map_grid_helper(l1_instruction_bank_paths, x_start=x_start, y_start=y_start, grid_width=4, grid_height=4, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

    x_start2 = x_start - 175
    y_start2 = 1900
    end_x2, end_y2 = map_grid_helper(l1_instruction_tag_paths, x_start=x_start2, y_start=y_start2, grid_width=2, grid_height=1, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)

map_l1_instruction(l1_instruction_bank_paths, l1_instruction_tag_paths)


#predictors_1, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_boom_tile/frontend/bpd/banked_predictors", "tage")
#
#def map_predictors1(predictors_1):
#
#    x_start = 200
#    y_start = 1000
#
#    end_x, end_y = map_grid_helper(predictors_1, x_start=x_start, y_start=y_start, grid_width=6, grid_height=8, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)
#
#map_predictors1(predictors_1)
#
#
#
#predictors_2, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_boom_tile/frontend/bpd/banked_predictors", "ebtb_ext")
#
#def map_predictors2(predictors_1):
#
#    x_start = 200
#    y_start = 1000
#
#    #end_x, end_y = map_grid_helper(predictors_2, x_start=x_start, y_start=y_start, grid_width=6, grid_height=8, path_converter=convert_tile, bounds_check_x=tile_max_x, bounds_check_y=tile_max_y)
#
#map_predictors1(predictors_2)



print("PLACEMENTS:")
print("------------------------------")

for line in final_placements:
    print(line)

print("------------------------------")
print("END PLACEMENTS")


print("MISSED PATHS:")
print("------------------------------")
for path in all_paths:
    print(path)
print("------------------------------")
print("END MISSED PATHS")

placed_srams = len(final_placements)/2

if placed_srams != all_paths_start_len:
    print(f"FAIL ONLY {placed_srams} placed, but started with {all_paths_start_len}")
