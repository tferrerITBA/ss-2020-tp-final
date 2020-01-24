package ar.edu.itba.ss.tpf;

public class Projectile extends Particle {
	
	public Projectile(double x, double y, double z, double vx, double vy, double vz) {
		super(Configuration.TURRET_PROJECTILE_RADIUS, x, y, z, vx, vy, vz);
	}

}
