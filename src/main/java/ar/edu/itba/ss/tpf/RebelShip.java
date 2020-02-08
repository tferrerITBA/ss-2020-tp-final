package ar.edu.itba.ss.tpf;

public class RebelShip extends Particle {

	public RebelShip(double radius, double mass, double x, double y, double z) {
		super(radius, mass, x, y, z);
	}

	public RebelShip(int id, double radius, double mass, double x, double y, double z, double vx, double vy, double vz) {
		super(id, radius, mass, x, y, z, vx, vy, vz);
	}
	
	@Override
	public RebelShip clone() {
		return new RebelShip(getId(), getRadius(), getMass(), getPosition().getX(), getPosition().getY(), getPosition().getZ(),
				getVelocity().getX(), getVelocity().getY(), getVelocity().getZ());
	}
	
	@Override
	public String toString() {
		return "RebelShip [getId()=" + getId() + ", getPosition()=" + getPosition() + "]";
	}

}
