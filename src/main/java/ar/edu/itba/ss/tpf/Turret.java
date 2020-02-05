package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;

public class Turret extends Particle implements Shooter {
	private double lastTimeSinceFired;
	private List<Projectile> projectiles;
	private static final double EPSILON = Math.pow(10, -3);
	
	public Turret(double x, double y, double z) {
		super(Configuration.TURRET_RADIUS, x, y, z);
		this.projectiles = new ArrayList<>();
	}
	
	public void fire(double timeStep, Grid grid) {
		if(lastTimeSinceFired > Configuration.TURRET_FIRE_RATE && hasLineOfSight(grid.getRebelShip(), grid.getDeathStar())) {
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

	private boolean hasLineOfSight(Particle rebelShip, Particle deathStar) {
		Point shipToDeathStar = deathStar.getPosition().getDiffVector(rebelShip.getPosition());
		Point shipToTurret = this.getPosition().getDiffVector(rebelShip.getPosition());
		double normDistFromShip = shipToDeathStar.getDotProduct(shipToTurret) / Math.pow(shipToTurret.getNorm(), 2);
		Point closestPoint;
		if(normDistFromShip < 0) {
			closestPoint = rebelShip.getPosition();
		} else if(normDistFromShip > 1) {
			closestPoint = this.getPosition();
		} else {
			closestPoint = rebelShip.getPosition().getSumVector(shipToTurret.getScalarMultiplication(normDistFromShip));
		}
		Point closestPointDiff = deathStar.getPosition().getDiffVector(closestPoint);
		
		return closestPointDiff.getNorm() - deathStar.getRadius() >= - EPSILON;
	}

	public double getLastTimeSinceFired() {
		return lastTimeSinceFired;
	}

	@Override
	public String toString() {
		return "Turret [id=" + this.getId() + ", lastTimeSinceFired=" + lastTimeSinceFired + "]";
	}
	
	@Override
	public List<Projectile> getProjectiles() {
		return projectiles;
	}
}
