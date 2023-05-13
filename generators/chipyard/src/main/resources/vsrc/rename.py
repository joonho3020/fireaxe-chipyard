import re

f = open("FireSim-generated.sv", "r")
lines = f.readlines()
f.close()

module_names = list()


sfx = "_BLACKBOX"

for line in lines:
  words = line.split()
  if len(words) > 0 and words[0] == "module":
    module_names.append(words[1].replace("(", ""))

module_names_set = set(module_names)



new_lines = list()
for line in lines:
  words = line.split(" ")
  new_words = words.copy()
  for (i, w) in enumerate(new_words):
    if w == "module":
      new_words[i+1] = new_words[i+1].replace("(", sfx + "(")
      break


  for (i, w) in enumerate(new_words):
    if w in module_names_set:
      new_words[i] = new_words[i] + sfx
      print(new_words)
      break

  new_line = " ".join(new_words)
  new_lines.append(new_line)


new_file = open("RocketCore.sv", "w")
new_file.writelines(new_lines)
new_file.close()

