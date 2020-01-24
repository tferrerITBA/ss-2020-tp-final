package ar.edu.itba.ss.tpf;

public class Turret extends Particle {
	private double lastTimeSinceFired;
	
	public Turret(double x, double y, double z) {
		super(Configuration.TURRET_RADIUS, x, y, z);
	}
	
	public void fire(Particle rebelShip, Particle deathStar) {
		if(lastTimeSinceFired > Configuration.TURRET_FIRE_RATE && hasLineOfSight(rebelShip, deathStar)) {
			lastTimeSinceFired = 0.0;
			// Create projectile
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
		
		return Double.compare(closestPointDiff.getNorm(), deathStar.getRadius()) >= 0;
	}

	public double getLastTimeSinceFired() {
		return lastTimeSinceFired;
	}
}
