import numpy
import matplotlib.pyplot as plt
from analyzer import calculateTraveledDistance, isSuccess, isFailure, isAll
from parser import parseDirectoryFromArgs, parseModeFromArgs, parseGroupDirectoryFromArgs
from calculator import average
import os
import pickle

OUTPUT_FOLDER = 'output'

def saveFig(fig, name):
  if not os.path.exists(OUTPUT_FOLDER):
    os.makedirs(OUTPUT_FOLDER)
  fig.savefig(f'{OUTPUT_FOLDER}/{name}.png')

def rate(simulationGroups, filt = isAll, name = "rate"):
  xs = []
  ys = []
  
  for simulationGroup in simulationGroups:
    simulations = simulationGroup.simulations
    print("Calculating the desired rate")
    desired = len(list(filter(filt, simulations)))
    rate = desired/len(simulations) * 100
    print(f'Success rate: {rate}%')
    xs.append(simulationGroup.name)
    ys.append(rate)
  
  fig, ax = plt.subplots()
  ax.set_ylabel('Tasa de éxito [%]')
  ax.set_xlabel('Cantidad de enemigos')
  fig.tight_layout()
  ax.plot(xs, ys, 'o-', markersize=4)
  
  saveFig(fig, name)

def traveledDistance(simulationGroups, filt = isAll, name = "dist"):
  xs = []
  ys = []
  errs = []
  
  for simulationGroup in simulationGroups:
    simulations = simulationGroup.simulations
    print("Calculating the average traveled distance")
    filtSims = list(filter(filt, simulations))
    distances = [calculateTraveledDistance(simulation) for simulation in filtSims]
    avgDistance = average(distances)
    print(f'Average distance traveled: {avgDistance}')
    xs.append(simulationGroup.name)
    ys.append(avgDistance)
    errs.append(numpy.nanstd(distances))
  
  fig, ax = plt.subplots()
  ax.set_ylabel('Distancia recorrida [m]')
  ax.set_xlabel('Cantidad de enemigos')
  fig.tight_layout()

  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=5, capthick=2, fmt="o-", zorder=1, markersize=4) 
  [bar.set_alpha(0.5) for bar in bars]
  
  saveFig(fig, name)

def traveledTime(simulationGroups, filt = isAll, name = "time"):
  xs = []
  ys = []
  errs = []

  for simulationGroup in simulationGroups:
    simulations = simulationGroup.simulations
    print("Calculating the average time traveled")
    filtSims = list(filter(filt, simulations))
    times = [simulation.steps[-1].time for simulation in filtSims]
    avgTime = average(times)
    print(f'Average time traveled: {avgTime}')
    xs.append(simulationGroup.name)
    ys.append(avgTime)
    errs.append(numpy.nanstd(times))
  
  fig, ax = plt.subplots()
  ax.set_ylabel('Tiempo recorrido [s]')
  ax.set_xlabel('Cantidad de enemigos')
  fig.tight_layout()

  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=5, capthick=2, fmt="o-", zorder=1, markersize=4) 
  [bar.set_alpha(0.5) for bar in bars]
  
  saveFig(fig, name)

def run():
  print("Las imágenes se guardan en la carpeta output de la raiz del proyecto.")
  print("Parse mode")
  mode = parseModeFromArgs()
  
  print("Parse simulations")
  simulationGroups = parseGroupDirectoryFromArgs()

  simulationGroups.sort(key=lambda x: x.name, reverse=True)

  traveledDistance(simulationGroups)
  traveledDistance(simulationGroups, isSuccess, "dist_success")
  traveledTime(simulationGroups)
  traveledTime(simulationGroups, isSuccess, "time_success")
  rate(simulationGroups)
  rate(simulationGroups, isSuccess, "rate_success")
  
  if mode == 1: traveledDistance(simulationGroups)
  elif mode == 2: traveledDistance(simulationGroups, isSuccess, "dist_success")
  elif mode == 3: traveledDistance(simulationGroups, isFailure, "dist_failure")
  elif mode == 4: traveledTime(simulationGroups)
  elif mode == 5: traveledTime(simulationGroups, isSuccess, "time_success")
  elif mode == 6: traveledTime(simulationGroups, isFailure, "time_failure")
  elif mode == 7: rate(simulationGroups)
  elif mode == 8: rate(simulationGroups, isSuccess, "rate_success")
  elif mode == 9: rate(simulationGroups, isFailure, "rate_failure")
run()