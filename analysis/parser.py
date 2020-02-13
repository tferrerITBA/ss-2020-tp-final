from models import Particle, Step, Simulation, SimulationGroup
import glob
import sys
import os

def parseGroupDirectoryFromArgs():
  return [SimulationGroup(parseDirectory(f), int(os.path.basename(f))) for f in glob.glob(sys.argv[1] + '/*')]

def parseDirectoryFromArgs():
  return parseDirectory(sys.argv[1])

def parseModeFromArgs():
  return int(sys.argv[2])

def parseTimesFile(filename):
  times = [float(line.rstrip('\n')) for line in open(filename)]
  return times

def parseFile(filename):
  lines = [line.rstrip('\n') for line in open(filename)]
  steps = []
  while len(lines) > 0:
    steps.append(parseStep(lines))
  return Simulation(steps, os.path.basename(filename))

def parseDirectory(directory, parse=parseFile):
  return [parse(f) for f in glob.glob(directory + '/*')]

def parseStep(lines):
  nextLines = int(lines.pop(0))
  time = float(lines.pop(0).split("Time=").pop())
  particle = parseParticle(lines.pop(0))
  for _ in range(nextLines - 1): lines.pop(0)
  return Step(time, particle)

def parseParticle(line):
  properties = line.split(" ")
  particle = Particle(*properties)
  return particle