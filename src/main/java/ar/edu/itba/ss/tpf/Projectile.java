package ar.edu.itba.ss.tpf;

public class Projectile extends Particle {
	private Shooter shooter;
	
	public Projectile(Shooter shooter, double x, double y, double z, double vx, double vy, double vz) {
		super(Configuration.TURRET_PROJECTILE_RADIUS, x, y, z, vx, vy, vz);
		this.shooter = shooter;
	}

	public Projectile(int id, double radius, double mass, double x, double y, double z, double vx, double vy,
			double vz, Shooter shooter) {
		super(id, radius, mass, x, y, z, vx, vy, vz);
		this.shooter = shooter;
	}

	public Shooter getShooter() {
		return shooter;
	}
	
	public void setShooter(Shooter shooter) {
		this.shooter = shooter;
	}
	
	@Override
	public Projectile clone() {
		return new Projectile(getId(), getRadius(), getMass(), getPosition().getX(), getPosition().getY(), getPosition().getZ(),
				getVelocity().getX(), getVelocity().getY(), getVelocity().getZ(), shooter);
	}

	@Override
	public String toString() {
		return "Projectile [getId()=" + getId() + ", getPosition()=" + getPosition() + "]";
	}
}
