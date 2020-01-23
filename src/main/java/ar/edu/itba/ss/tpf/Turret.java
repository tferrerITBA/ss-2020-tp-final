package ar.edu.itba.ss.tpf;

public class Turret extends Particle {
	private double lastTimeSinceFired;
	
	public Turret(double x, double y, double z) {
		super(Configuration.TURRET_RADIUS, x, y, z);
	}
	
	public void fire() {
		lastTimeSinceFired = 0.0;
	}
}
