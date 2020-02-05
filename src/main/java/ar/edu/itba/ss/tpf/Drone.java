package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Drone extends Particle implements Shooter {
	private double lastTimeSinceFired;
	private List<Projectile> projectiles;
	private static final double EPSILON = Math.pow(10, -10);
	
	public Drone(double x, double y, double z) {
		super(Configuration.DRONE_RADIUS, x, y, z);
		this.projectiles = new ArrayList<>();
		Random r = new Random();
		lastTimeSinceFired = r.nextDouble() * Configuration.TURRET_FIRE_RATE;
	}
	
	public void fire(double timeStep, Grid grid) {
		if(lastTimeSinceFired > Configuration.TURRET_FIRE_RATE) {
			if(!hasLineOfSight(grid.getRebelShip(), grid.getDeathStar())) {
				return;
			}
			for(Drone drone : grid.getDrones()) {
				if(!this.equals(drone) && !hasLineOfSight(grid.getRebelShip(), drone)) {
					return;
				}
			}
			
			lastTimeSinceFired = 0;
			
			/* Projectiles are thrown as a succession of particles to look like laser beams */
			Point targetVector = grid.getRebelShip().getPosition().getDirectionUnitVector(this.getPosition());
			Point initPosition = this.getPosition();
			double deltaPosition = 0.25;
			Point diffProjectilePosition = initPosition
					.getSumVector(targetVector.getScalarMultiplication(deltaPosition)).getDiffVector(initPosition);
			Point projectileVelocity = targetVector.getScalarMultiplication(Configuration.TURRET_PROJECTILE_SPEED);
			for(int i = 0; i < Configuration.PROJECTILE_PARTICLE_COUNT; i++) {
				Point newPosition = initPosition.getSumVector(diffProjectilePosition.getScalarMultiplication(i));
				Projectile projectile = new Projectile(this, newPosition.getX(), newPosition.getY(), newPosition.getZ(),
						projectileVelocity.getX(), projectileVelocity.getY(), projectileVelocity.getZ());
				grid.getParticles().add(projectile);
				grid.getProjectiles().add(projectile);
				projectiles.add(projectile);
			}
		} else {
			lastTimeSinceFired += timeStep;
		}
	}

	private boolean hasLineOfSight(Particle target, Particle possibleObstacle) {
		Point shipToDeathStar = possibleObstacle.getPosition().getDiffVector(target.getPosition());
		Point shipToTurret = this.getPosition().getDiffVector(target.getPosition());
		double normDistFromShip = shipToDeathStar.getDotProduct(shipToTurret) / Math.pow(shipToTurret.getNorm(), 2);
		Point closestPoint;
		if(normDistFromShip < 0) {
			closestPoint = target.getPosition();
		} else if(normDistFromShip > 1) {
			closestPoint = this.getPosition();
		} else {
			closestPoint = target.getPosition().getSumVector(shipToTurret.getScalarMultiplication(normDistFromShip));
		}
		Point closestPointDiff = possibleObstacle.getPosition().getDiffVector(closestPoint);

		return closestPointDiff.getNorm() - Configuration.ENTITY_TO_PROJECTILE_PERSONAL_SPACE >= - EPSILON;
	}
	
	@Override
	public List<Projectile> getProjectiles() {
		return projectiles;
	}

	@Override
	public String toString() {
		return "Drone [getId()=" + getId() + ", getPosition()=" + getPosition() + "]";
	}
	
}
