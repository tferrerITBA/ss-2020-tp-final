package ar.edu.itba.ss.tpf;

public class RebelShip extends Particle {

	public RebelShip(double rebelShipRadius, double x, double y, double z) {
		super(rebelShipRadius, x, y, z);
	}

	public RebelShip(int id, double radius, double x, double y, double z, double vx, double vy, double vz) {
		super(id, radius, x, y, z, vx, vy, vz);
	}

}
