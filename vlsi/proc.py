



with open('ROCKETTILETOP', 'r') as rttop:
    last_dir = "input"
    for line in rttop.readlines():
        l = line.strip().replace(",", "").split()
        io = None
        name = None
        if len(l) == 3:
            io = l[0]
            last_dir = io
            name = l[2]
        elif len(l) == 2:
            io = l[0]
            last_dir = io
            name = l[1]
        elif len(l) == 1:
            io = last_dir
            name = l[0]
        else:
            print("ERR")
            exit(1)

        outline = f""" name: "{name}", direction: "{io}", clock: "clock", delay: "100ps" """
        outline = "        - {" + outline + "},"
        print(outline)

