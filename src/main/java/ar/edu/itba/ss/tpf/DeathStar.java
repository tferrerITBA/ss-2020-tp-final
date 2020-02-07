package ar.edu.itba.ss.tpf;

public class DeathStar extends Particle {

	public DeathStar(double deathStarRadius, double x, double y, double z) {
		super(deathStarRadius, x, y, z);
	}
	
	public DeathStar(int id, double radius, double x, double y, double z, double vx, double vy, double vz) {
		super(id, radius, x, y, z, vx, vy, vz);
	}

}
