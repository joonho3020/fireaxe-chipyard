
from collections import defaultdict
import json

gen_col_mems_v = "generated-src/chipyard.harness.TestHarness.HyperscaleSoCRocketClientNodeConfig/gen-collateral/chipyard.harness.TestHarness.HyperscaleSoCRocketClientNodeConfig.top.mems.v"

leaf_name_to_mems = defaultdict(list)
srams_set = set()

#x_snap = 1.08
y_snap = 0.09


def round_yval(orig_yval):
    return round((int(orig_yval / y_snap) + 1) * y_snap, 2)

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

seq_mems = "generated-src/chipyard.harness.TestHarness.HyperscaleSoCRocketClientNodeConfig/gen-collateral/metadata/seq_mems.json"
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

def filter_out_path(input_array, filter_path):
    filter_match = []
    all_else = []

    for x in input_array:
        if x[0].startswith(filter_path):
            filter_match.append(x)
        else:
            all_else.append(x)
    return filter_match, all_else


data_bank_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched/bankedStore/cc_banks")
dir_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/subsystem_l2_wrapper/l2/inclusive_cache_bank_sched/directory/cc_dir/cc_dir_ext")

def map_l2_data(data_bank_paths, dir_paths):
    for vert_index in range(9):
        horiz_count = 0
        if vert_index == 4:
            horiz_count = 4
        else:
            horiz_count = 2
        for horiz_index in range(horiz_count):
            current_path = None
            if vert_index < 4:
                current_path = data_bank_paths[horiz_index * 8 + vert_index][0]
            elif vert_index == 4:
                current_path = dir_paths[horiz_index][0]
            else:
                current_path = data_bank_paths[horiz_index * 8 + vert_index-1][0]

            multiplier = 300
            if vert_index == 4:
                multiplier = 150
            xval = 1000 + horiz_index * multiplier
            yval = round_yval(100 + vert_index * 200)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")


map_l2_data(data_bank_paths, dir_paths)


def convert_rockettile(path):
    return path.replace("ChipTop/system/tile_prci_domain/tile_reset_domain_tile", "RocketTile")

comp_ht_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/compress_accel_compressor/lz77hashmatcher/hash_table/hash_mem/hash_mem_ext")

def map_comp_ht(comp_ht):
    x_start = 0
    y_start = 0

    for horiz_index in range(8):
        for vert_index in range(3):
            current_path = comp_ht[horiz_index + vert_index * 8][0]
            current_path = convert_rockettile(current_path)

            xval = horiz_index * 170 + x_start
            yval = round_yval(vert_index * 140 + y_start)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")



map_comp_ht(comp_ht_paths)


comp_hist_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/compress_accel_compressor/lz77hashmatcher/history_buffer")

comp_hist_paths = list(filter(lambda x: "MEM_0_ext/mem_1_" not in x, comp_hist_paths))


def map_comp_hist(comp_hist):
    x_start = 0
    y_start = 420

    for horiz_index in range(8):
        for vert_index in range(4):
            current_path = comp_hist[horiz_index + vert_index * 8][0]
            current_path = convert_rockettile(current_path)

            xval = horiz_index * 170 + x_start
            yval = round_yval(vert_index * 50 + y_start)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")



map_comp_hist(comp_hist_paths)


decomp_hist_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/compress_accel_decompressor/command_expander/MEM")

def map_decomp_hist(decomp_hist):
    x_start = 0
    y_start = 620

    for horiz_index in range(8):
        for vert_index in range(4):
            current_path = decomp_hist[horiz_index + vert_index * 8][0]
            current_path = convert_rockettile(current_path)

            xval = horiz_index * 170 + x_start
            yval = round_yval(vert_index * 50 + y_start)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")



map_decomp_hist(decomp_hist_paths)



l1_data_bank_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/dcache/data/data_arrays")
l1_data_tag_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/dcache/tag_array/tag_array")

def map_l1_data(l1_data_bank_paths, l1_data_tag_paths):
    for horiz_index in range(9):
        for vert_index in range(2):
            current_path = None
            if horiz_index < 4:
                current_path = l1_data_bank_paths[vert_index * 8 + horiz_index][0]
            elif horiz_index == 4:
                current_path = l1_data_tag_paths[vert_index][0]
            else:
                current_path = l1_data_bank_paths[vert_index * 8 + horiz_index-1][0]
            xval = 0 + horiz_index * 70
            yval = round_yval(820 + vert_index * 150)
            current_path = convert_rockettile(current_path)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")


map_l1_data(l1_data_bank_paths, l1_data_tag_paths)






l1_instruction_bank_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/frontend/icache/data_arrays")
l1_instruction_tag_paths, all_paths = filter_out_path(all_paths, "ChipTop/system/tile_prci_domain/tile_reset_domain_tile/frontend/icache/tag_array")

def map_l1_instruction(l1_instruction_bank_paths, l1_instruction_tag_paths):
    for horiz_index in range(9):
        for vert_index in range(2):
            current_path = None
            if horiz_index < 4:
                current_path = l1_instruction_bank_paths[vert_index * 8 + horiz_index][0]
            elif horiz_index == 4:
                current_path = l1_instruction_tag_paths[vert_index][0]
            else:
                current_path = l1_instruction_bank_paths[vert_index * 8 + horiz_index-1][0]
            xval = 0 + horiz_index * 70
            yval = round_yval(1120 + vert_index * 150)
            current_path = convert_rockettile(current_path)

            outline = f"path: {current_path}, type: hardmacro, x: {xval}, y: {yval}"
            final_placements.append("- {" + outline + "}")


map_l1_instruction(l1_instruction_bank_paths, l1_instruction_tag_paths)

for line in final_placements:
    print(line)

print(all_paths)

if len(final_placements) != all_paths_start_len:
    print(f"FAIL ONLY {len(final_placements)} placed, but started with {all_paths_start_len}")
