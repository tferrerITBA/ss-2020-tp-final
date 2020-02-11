package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeemanIntegrator {
	
	private final Grid grid;
	private final PredictiveCollisionAvoidance pca;
	private final double timeStep;
	
	public BeemanIntegrator(final Grid grid, final PredictiveCollisionAvoidance pca) {
		this.grid = grid;
		this.pca = pca;
		this.timeStep = Configuration.TIME_STEP;
	}
	
	public void updateParticleLists(List<Particle> prevParticles, List<Particle> currentParticles,
			List<Particle> updatedParticles) {
		updatePreviousParticles(prevParticles, currentParticles);
		setUpdatedParticlesInGrid(updatedParticles);
	}
	
	public List<Particle> updateParticles(final List<Particle> prevParticles) {
    	List<Particle> currentParticles = new ArrayList<>();
		currentParticles.add(grid.getRebelShip());
		currentParticles.addAll(grid.getDrones());
		currentParticles.addAll(grid.getProjectiles());
		
		List<Particle> predictedParticles = predictParticles(currentParticles, prevParticles);
    	
    	List<Particle> updatedParticles = new ArrayList<>(currentParticles.size());
    	
		for(Particle currentParticle : currentParticles) {
			/* New projectiles do not have a previous reference */
			Optional<Particle> optionalPrevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst();
			Particle prevParticle = null;
			Point prevAcceleration = null;
			if(optionalPrevParticle.isPresent()) {
				prevParticle = optionalPrevParticle.get();
				prevAcceleration = pca.getAcceleration(prevParticle, prevParticles);
			} else {
				prevAcceleration = new Point();
			}
			
			Particle predParticle = predictedParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst().get();
			Particle updatedParticle = currentParticle.clone();
			
			Point currAcceleration = pca.getAcceleration(currentParticle, currentParticles);
			Point predAcceleration = pca.getAcceleration(predParticle, predictedParticles);
			
			Point correctedVelocity = currentParticle.getVelocity()
					.getSumVector(predAcceleration.getScalarMultiplication((1 / 3.0) * timeStep))
					.getSumVector(currAcceleration.getScalarMultiplication((5 / 6.0) * timeStep))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 6.0) * timeStep));
			
			if(currentParticle instanceof RebelShip && correctedVelocity.getNorm() > Configuration.REBEL_SHIP_MAX_VEL) {
				correctedVelocity = correctedVelocity.normalize().getScalarMultiplication(Configuration.REBEL_SHIP_MAX_VEL);
			} else if(currentParticle instanceof Drone && correctedVelocity.getNorm() > Configuration.DRONE_MAX_VEL) {
				correctedVelocity = correctedVelocity.normalize().getScalarMultiplication(Configuration.DRONE_MAX_VEL);
			}

			updatedParticle.setPosition(predParticle.getPosition());
			updatedParticle.setVelocity(correctedVelocity);
			updatedParticles.add(updatedParticle);
		}
		
		updateProjectileReferences(updatedParticles);
			
		return updatedParticles;
	}
	
	private List<Particle> predictParticles(final List<Particle> currentParticles, final List<Particle> prevParticles) {
		List<Particle> predictedParticles = new ArrayList<>();
		
		for(Particle currentParticle : currentParticles) {
			Point currAcceleration = null;
			Point prevAcceleration = null;
			if(currentParticle instanceof Projectile) {
				/* Projectiles have no acceleration */
				currAcceleration = new Point();
				prevAcceleration = new Point();
			} else {
				Particle prevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst().get();
				currAcceleration = pca.getAcceleration(currentParticle, currentParticles);
				prevAcceleration = pca.getAcceleration(prevParticle, prevParticles);
			}
			
			Point newPosition = currentParticle.getPosition()
					.getSumVector(currentParticle.getVelocity().getScalarMultiplication(timeStep))
					.getSumVector(currAcceleration.getScalarMultiplication((2 / 3.0) * Math.pow(timeStep, 2)))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 6.0) * Math.pow(timeStep, 2)));
			
			Point predictedVelocity = currentParticle.getVelocity()
					.getSumVector(currAcceleration.getScalarMultiplication((3 / 2.0) * timeStep))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 2.0) * timeStep));
			
			if(currentParticle instanceof RebelShip && predictedVelocity.getNorm() > Configuration.REBEL_SHIP_MAX_VEL) {
				predictedVelocity = predictedVelocity.normalize().getScalarMultiplication(Configuration.REBEL_SHIP_MAX_VEL);
			} else if(currentParticle instanceof Drone && predictedVelocity.getNorm() > Configuration.DRONE_MAX_VEL) {
				predictedVelocity = predictedVelocity.normalize().getScalarMultiplication(Configuration.DRONE_MAX_VEL);
			}
			
			Particle predictedParticle = currentParticle.clone();
			predictedParticle.setPosition(newPosition);
			predictedParticle.setVelocity(predictedVelocity);
			predictedParticles.add(predictedParticle);
		}
		
		updateProjectileReferences(predictedParticles);
		
		return predictedParticles;
	}
	
	/* Drones & Projectiles still have outdated references between them */
	private void updateProjectileReferences(List<Particle> particles) {
		int referenceId;
		for(Particle particle : particles) {
			if(particle instanceof Drone) {
				Drone drone = (Drone) particle;
				List<Projectile> updatedProjectiles = new ArrayList<>();
				
				for(Projectile projectile : drone.getProjectiles()) {
					referenceId = projectile.getId();
					final int refId = referenceId;
					/* Search for the updated object within the updated particles */
					updatedProjectiles.add((Projectile) particles.stream().filter(p -> p.getId() == refId).findFirst().get());
				}
				/* Update the entire projectiles list */
				drone.setProjectiles(updatedProjectiles);
			} else if(particle instanceof Projectile) {
				Projectile projectile = (Projectile) particle;
				/* Search for the updated object within the updated particles (if shooter is a Turret it remains static) */
				if(projectile.getShooter() != null && !(projectile.getShooter() instanceof Turret)) {
					referenceId = ((Particle) projectile.getShooter()).getId();
					final int refId = referenceId;
					Particle shooter = particles.stream().filter(p -> p.getId() == refId).findFirst().get();
					projectile.setShooter((Shooter) shooter);
				}
			}
		}
	}
	
	private void setUpdatedParticlesInGrid(List<Particle> updatedParticles) {
		RebelShip rebelShip = updatedParticles.stream().filter(p -> p instanceof RebelShip)
				.map(p -> (RebelShip)p).findFirst().get();
		List<Drone> drones = updatedParticles.stream().filter(p -> p instanceof Drone)
				.map(p -> (Drone)p).collect(Collectors.toList());
		List<Projectile> projectiles = updatedParticles.stream().filter(p -> p instanceof Projectile)
				.map(p -> (Projectile)p).collect(Collectors.toList());
		
		grid.setRebelShip(rebelShip);
		grid.setDrones(drones);
		grid.setProjectiles(projectiles);
	}
	
	private void updatePreviousParticles(List<Particle> prevParticles, List<Particle> currentParticles) {
		for(Particle currentParticle : currentParticles) {
			/* New projectiles do not have a previous reference */
			Optional<Particle> optionalPrevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst();
			if(optionalPrevParticle.isPresent()) {
				prevParticles.remove(optionalPrevParticle.get());
			}
			prevParticles.add(currentParticle);
		}
	}

	// Euler Algorithm evaluated in (-dt)
    public List<Particle> initPrevParticles(List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>(currentParticles.size());
    	for(Particle particle : currentParticles) {
			previousParticles.add(initPrevParticle(particle, currentParticles));
		}
    	
		return previousParticles;
	}
    
    private Particle initPrevParticle(Particle particle, List<Particle> currentParticles) {
    	Particle prevParticle = particle.clone();
    	
    	Point acceleration = pca.getAcceleration(particle, currentParticles);
		
    	Point prevPosition = particle.getPosition().getDiffVector(particle.getVelocity().getScalarMultiplication(timeStep))
    			.getSumVector(acceleration.getScalarMultiplication(Math.pow(timeStep, 2) / 2.0));
		Point prevVelocity = particle.getVelocity().getDiffVector(acceleration.getScalarMultiplication(timeStep));
		
		prevParticle.setPosition(prevPosition);
		prevParticle.setVelocity(prevVelocity);
		
		return prevParticle;
    }

}
