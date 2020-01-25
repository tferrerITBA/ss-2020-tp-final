package ar.edu.itba.ss.tpf;

import java.util.List;

public class Turret extends Particle {
	private double lastTimeSinceFired;
	private static final double EPSILON = Math.pow(10, -10);
	
	public Turret(double x, double y, double z) {
		super(Configuration.TURRET_RADIUS, x, y, z);
	}
	
	public void fire(double timeStep, Particle rebelShip, Particle deathStar, List<Particle> particles, List<Projectile> projectiles) {
		if(lastTimeSinceFired > Configuration.TURRET_FIRE_RATE && hasLineOfSight(rebelShip, deathStar)) {
			lastTimeSinceFired = 0;
			
			/* Projectiles are thrown as a succession of particles to look like laser beams */
			Point targetVector = rebelShip.getPosition().getDirectionUnitVector(this.getPosition());
			Point initPosition = this.getPosition();
			double deltaPosition = 0.25;
			Point diffProjectilePosition = initPosition
					.getSumVector(targetVector.getScalarMultiplication(deltaPosition)).getDiffVector(initPosition);
			Point projectileVelocity = targetVector.getScalarMultiplication(Configuration.TURRET_PROJECTILE_SPEED);
			for(int i = 0; i < Configuration.PROJECTILE_PARTICLE_COUNT; i++) {
				Point newPosition = initPosition.getSumVector(diffProjectilePosition.getScalarMultiplication(i));
				Projectile projectile = new Projectile(newPosition.getX(), newPosition.getY(), newPosition.getZ(),
						projectileVelocity.getX(), projectileVelocity.getY(), projectileVelocity.getZ());
				particles.add(projectile);
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

		return Math.abs(closestPointDiff.getNorm() - deathStar.getRadius()) >= EPSILON;
	}

	public double getLastTimeSinceFired() {
		return lastTimeSinceFired;
	}

	@Override
	public String toString() {
		return "Turret [id=" + this.getId() + ", lastTimeSinceFired=" + lastTimeSinceFired + "]";
	}
}
