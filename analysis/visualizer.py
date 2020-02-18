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

def rate(simulationGroups, filt = isAll):
  xs = []
  ys = []
  errs = []
  
  for simulationGroup in simulationGroups:
    simulations = simulationGroup.simulations
    print("Calculating the desired rate")
    desired = len(list(filter(filt, simulations)))
    rate = desired/len(simulations) * 100
    print(f'Success rate: {rate}%')
    xs.append(simulationGroup.name)
    ys.append(rate)
  
  return xs,ys

def rateGraph(simulationGroups):
  fig, ax = plt.subplots()
  ax.set_ylabel('Tasa [%]')
  ax.set_xlabel('Tiempo de recarga [s]')
  # ax.set_xscale('log')

  xs, ys = rate(simulationGroups, isSuccess)
  ax.plot(xs, ys, 'go-', markersize=4, label='Misión Completa')
  
  xs, ys = rate(simulationGroups, isFailure)
  ax.plot(xs, ys, 'ro-', markersize=4, label='Muerte del rebelde')

  ax.legend()
  
  fig.tight_layout()
  saveFig(fig, "rate")

def traveledDistance(simulationGroups, filt = isAll):
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
  
  return xs, ys, errs

def traveledDistanceGraph(simulationGroups):
  fig, ax = plt.subplots()
  ax.set_ylabel('Distancia recorrida [m]')
  ax.set_xlabel('Tiempo de recarga [s]')
  # ax.set_xscale('log')
  
  xs, ys, errs = traveledDistance(simulationGroups)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="o-", zorder=1, markersize=6, label="Todos los escenarios") 
  [bar.set_alpha(0.5) for bar in bars]

  xs, ys, errs = traveledDistance(simulationGroups, isSuccess)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="go", zorder=1, markersize=6, label="Misión Completa") 
  [bar.set_alpha(0.5) for bar in bars]

  xs, ys, errs = traveledDistance(simulationGroups, isFailure)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="ro", zorder=1, markersize=6, label="Muerte del rebelde") 
  [bar.set_alpha(0.5) for bar in bars]
  
  ax.legend()

  fig.tight_layout()
  saveFig(fig, "distance")

def traveledTime(simulationGroups, filt = isAll):
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
  
  return xs, ys, errs  

def traveledTimeGraph(simulationGroups):
  fig, ax = plt.subplots()
  ax.set_ylabel('Tiempo recorrido [s]')
  ax.set_xlabel('Tiempo de recarga [s]')
  # ax.set_xscale('log')

  xs, ys, errs = traveledTime(simulationGroups)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="o-", zorder=1, markersize=6, label="Todos los escenarios") 
  [bar.set_alpha(0.5) for bar in bars]

  xs, ys, errs = traveledTime(simulationGroups, isSuccess)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="go", zorder=1, markersize=6, label="Misión Completa") 
  [bar.set_alpha(0.5) for bar in bars]

  xs, ys, errs = traveledTime(simulationGroups, isFailure)
  markers, caps, bars = ax.errorbar(xs, ys, yerr=errs, capsize=7, capthick=2, fmt="ro", zorder=1, markersize=6, label="Muerte del rebelde") 
  [bar.set_alpha(0.5) for bar in bars]

  ax.legend()

  fig.tight_layout()
  saveFig(fig, "time")

def run():
  print("Las imágenes se guardan en la carpeta output de la raiz del proyecto.")
  print("Parse mode")
  mode = parseModeFromArgs()
  
  print("Parse simulations")
  simulationGroups = parseGroupDirectoryFromArgs()
  simulationGroups.sort(key=lambda x: x.name, reverse=True)

  traveledDistanceGraph(simulationGroups)
  traveledTimeGraph(simulationGroups)
  rateGraph(simulationGroups)
  
run()