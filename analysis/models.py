import numpy
import math

class Particle:
  def __init__(self, id, radius, x, y, z, vx, vy, vz):
    self.id = int(id)
    self.radius = float(radius)
    self.x = float(x)
    self.y = float(y)
    self.z = float(z)
    self.vx = float(vx)
    self.vy = float(vy)
    self.vz = float(vz)
  
  def achievedGoal(self):
    return abs(math.sqrt((self.x - 150) ** 2 + (self.y - 50) ** 2 + (self.z - 50) ** 2) - 22.5) <= 0.5
  
  def getVelocityLength(self):
    return math.sqrt(self.vx ** 2  + self.vy ** 2 + self.vz ** 2)
  
  def position(self):
    return (self.x, self.y, self.z)
  
  def __str__(self):
    return f'Id: {self.id}\nRadius: {self.radius}\nPosition X: {self.x}\nPosition Y: {self.y}\nPosition Z: {self.z}\nVelocity X: {self.vx}\nVelocity Y: {self.vy}\nVelocity Z: {self.vz}\n'

class Step:
  def __init__(self, time, particle):
    self.time = time
    self.particle = particle

class Simulation:
  def __init__(self, steps, name):
    self.steps = steps
    self.name = name
  def getSecondHalf(self):
    return self.steps[len(self.steps)//2:]
  def getLastThird(self):
    return self.steps[-len(self.steps)//3:]

class SimulationGroup:
  def __init__(self, simulations, name):
    self.simulations = simulations
    self.name = name