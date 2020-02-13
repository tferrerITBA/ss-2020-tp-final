import os

RESULTS_FOLDER = 'analysis/results'
DEFAULT_OUTPUT = 'ovito_output.xyz'
REPEAT = 30
SIMULATION = 'java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt &'

# create results folder if it does not exist
if not os.path.exists(RESULTS_FOLDER):
  os.makedirs(RESULTS_FOLDER)

# Generate multiple simulations
for simNum in range(REPEAT):
  os.system(f'echo "100\nn\n{simNum}" > params.txt')
  MOVE = f'mv {simNum}.txt {RESULTS_FOLDER}/{simNum}.xyz'
  os.system(SIMULATION) # run simulation
  os.system("sleep 5")
