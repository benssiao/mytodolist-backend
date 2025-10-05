import subprocess 
import os
import sys


state_file = "current_test_number.txt"

optional_test_case = sys.argv[1] if len(sys.argv) > 1 else None
if os.path.exists(state_file):
    with open(state_file, "r") as f:
        current_test_number = int(f.read().strip())
  
else:
    current_test_number = 0
    with open(state_file, "w") as f:
        f.write(str(current_test_number))
i = current_test_number
#increment
with open(state_file, "w") as f:
    f.write(str(i+1))


cmd = ['mvn', 'test']
filename = f"test_output{i}.log"
if optional_test_case:
    cmd.extend(['-Dtest=' + optional_test_case])
if len(cmd) >= 3:
    filename = f"test_output_{optional_test_case}_{i}.log"

with open(filename, "w") as output_file:
  result = subprocess.run(
        cmd,
        stdout=output_file,
        stderr=subprocess.STDOUT,  # Also redirect stderr to the same file
        text=True
    )
  
  
  
  