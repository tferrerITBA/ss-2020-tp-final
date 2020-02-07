from ovito.io import import_file
from ovito.modifiers import *

node = import_file("./test.xyz", multiple_frames = True, columns = [
  'Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Position.Z',
  'Velocity.X', 'Velocity.Y', 'Velocity.Z'
])
# id:I:1:radius:R:1:pos:R:3:velo:R:3
node.add_to_scene()

# Periodic Boundary Condition (X,Y,Z)
# node.source.cell.pbc = (False, False, False)

selection = ExpressionSelectionModifier(
  operate_on = 'particles',
  expression = 'ParticleIdentifier == 0'
)

trajectory = GenerateTrajectoryLinesModifier(only_selected = True)

color = ColorCodingModifier(
  particle_property = 'Radius',
  start_value = 2,
  end_value = 0
)

node.modifiers.append(selection)
node.modifiers.append(trajectory)
node.modifiers.append(color)

trajectory.generate()

node.modifiers.remove(selection)

node.compute()
