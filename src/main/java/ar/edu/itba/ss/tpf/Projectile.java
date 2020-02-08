package ar.edu.itba.ss.tpf;

public class Projectile extends Particle {
	private Shooter shooter;
	
	public Projectile(Shooter shooter, double x, double y, double z, double vx, double vy, double vz) {
		super(Configuration.TURRET_PROJECTILE_RADIUS, x, y, z, vx, vy, vz);
		this.shooter = shooter;
	}

	public Shooter getShooter() {
		return shooter;
	}
	
	public void setShooter(Shooter shooter) {
		this.shooter = shooter;
	}

	@Override
	public String toString() {
		return "Projectile [getId()=" + getId() + ", getPosition()=" + getPosition() + "]";
	}
}
