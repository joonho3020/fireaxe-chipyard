

x_alignment = 0.108
y_alignment = 0.09

x_base = 1308.312
y_base = 1361.7

x_offset = 1308.312
y_offset = 1361.7

cd_to_reg_offset_x = 49.68


def x_assert(xval):
    rounded_check = round(xval / x_alignment, 3)
    if rounded_check % 1 != 0:
        print(f"FAIL: {xval} not aligned to {x_alignment}")
        exit(1)

def y_assert(yval):
    rounded_check = round(yval / y_alignment, 3)
    if rounded_check % 1 != 0:
        print(f"FAIL: {yval} not aligned to {y_alignment}")
        exit(1)

def pair_edge_check(x, y):
    chip_width = 3924.72
    chip_height = 4084.92
    left_x_dist = x
    right_x_dist = chip_width - x
    min_x_dist = min(left_x_dist, right_x_dist)
    x_in_range = min_x_dist < 1500

    bottom_y_dist = y
    top_y_dist = chip_height - y
    min_y_dist = min(bottom_y_dist, top_y_dist)
    y_in_range = min_y_dist < 1500

    if not (x_in_range or y_in_range):
        print(f"FAIL. DIC at {x}, {y} is not within 1500um of chip edge")
        exit(1)

x_assert(x_base)
x_assert(x_offset)
x_assert(cd_to_reg_offset_x)

y_assert(y_base)
y_assert(y_offset)

pairs_cd = []
pairs_reg = []

for x in range(2):
    for y in range(2):

        x_val_cd = round(x_base + x_offset * x, 3)
        y_val_cd = round(y_base + y_offset * y, 3)


        x_assert(x_val_cd)
        y_assert(y_val_cd)

        pair_edge_check(x_val_cd, y_val_cd)

        print(f"({x_val_cd}, {y_val_cd})")
        pairs_cd.append([x_val_cd, y_val_cd])

        x_val_reg = round(cd_to_reg_offset_x + x_val_cd, 3)
        y_val_reg = y_val_cd

        x_assert(x_val_reg)
        y_assert(y_val_reg)

        pair_edge_check(x_val_reg, y_val_reg)

        print(f"({x_val_reg}, {y_val_reg})")
        pairs_reg.append([x_val_reg, y_val_reg])

rtile_xmin = 1949.4
rtile_width = 1900.044
rtile_xmax = rtile_xmin + rtile_width

rtile_ymin = 20.16
rtile_height = 2000.07
rtile_ymax = rtile_ymin + rtile_height

def is_in_rocket_tile(x, y):
    x_match = x >= rtile_xmin and x <= rtile_xmax
    y_match = y >= rtile_ymin and y <= rtile_ymax

    return x_match and y_match


rt_placements = []
ct_placements = []

base_num = -1
for cd, reg in zip(pairs_cd, pairs_reg):
    base_num += 1

    in_rt_cd = is_in_rocket_tile(*cd)
    in_rt_reg = is_in_rocket_tile(*reg)

    if (in_rt_reg and (not in_rt_cd)) or ((not in_rt_reg) and in_rt_cd):
        print("ERROR: only one of CD or REG is in RocketTile")
        exit(1)

    # at this point either both or neither are in rockettile

    # special handling to prevent L2 collision:
    if cd[0] == 1410.048 and cd[1] == 300.06:
        # avoid collision w/L2
        l2_bank_base = 1300
        l2_bank_width = 247.148
        margin = 50

        new_x_edge = round((int((l2_bank_base + l2_bank_width + margin) / 0.108)+1)*0.108, 3)

        reg_to_cd = reg[0] - cd[0]

        new_cd_0 = new_x_edge
        new_reg_0 = new_x_edge + reg_to_cd
        ct_placements.append(f"        # WARNING: MOVED RIGHT TO AVOID L2. ORIGINAL IDEAL COORDS:  CD: ({cd[0]}, {cd[1]})")
        ct_placements.append(f"        # WARNING: MOVED RIGHT TO AVOID L2. ORIGINAL IDEAL COORDS: REG: ({reg[0]}, {reg[1]})")

        cd[0] = new_cd_0
        x_assert(cd[0])
        reg[0] = new_reg_0
        x_assert(reg[0])



    if in_rt_cd and in_rt_reg:
        rt_placements.append(f"        # PAIR MUST BE PLACED IN RocketTile (assumed to be at: ({rtile_xmin},{rtile_ymin})).")
        rt_placements.append(f"        # EXPECTED ABSOLUTE PLACEMENT: CD : ({cd[0]}, {cd[1]})")
        rt_placements.append(f"        # EXPECTED ABSOLUTE PLACEMENT: REG: ({reg[0]}, {reg[1]})")

        cd[0] = round(cd[0] - rtile_xmin, 3)
        cd[1] = round(cd[1] - rtile_ymin, 3)
        x_assert(cd[0])
        y_assert(cd[1])

        reg[0] = round(reg[0] - rtile_xmin, 3)
        reg[1] = round(reg[1] - rtile_ymin, 3)
        x_assert(reg[0])
        y_assert(reg[1])

    else:
        ct_placements.append(f"        # PAIR MUST BE PLACED IN ChipTop.")



    cd_out_str = "        - {path: ChipTop/DICCD" + str(base_num) + ", type: hardmacro, master: \"fdk22tic4m1_diccd_cont\", create_physical: true, x: " + str(cd[0]) + ",   y: " + str(cd[1]) + ", top_layer: m6}"
    reg_out_str = "        - {path: ChipTop/DICREG" + str(base_num) + ", type: hardmacro, master: \"fdk22tic4m1_dicreg_cont\", create_physical: true, x: " + str(reg[0]) + ",   y: " + str(reg[1]) + ", top_layer: m6}"




    if in_rt_cd and in_rt_reg:
        rt_placements.append(cd_out_str)
        rt_placements.append(reg_out_str)
    else:
        ct_placements.append(cd_out_str)
        ct_placements.append(reg_out_str)



print("\n".join(ct_placements))
print("\n".join(rt_placements))

