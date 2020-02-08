package ar.edu.itba.ss.tpf;

import java.util.Objects;

public class Particle implements Cloneable {
	
	private static int count = 0;
	
	private int id;
	private double radius;
	private double mass;
	private Point position;
	private Point velocity;
	
	public Particle(final double radius, final double mass, final double x, final double y, final double z) {
		this.id = count++;
		this.radius = radius;
		this.mass = mass;
		this.position = new Point(x, y, z);
		this.velocity = new Point(0, 0, 0);
	}
	
	public Particle(final double radius, final double x, final double y, final double z) {
		this.id = count++;
		this.radius = radius;
		this.position = new Point(x, y, z);
		this.velocity = new Point(0, 0, 0);
	}
	
	public Particle(final double radius, final double x, final double y, final double z,
			final double vx, final double vy, final double vz) {
		this.id = count++;
		this.radius = radius;
		this.position = new Point(x, y, z);
		this.velocity = new Point(vx, vy, vz);
	}
	
//	public Particle(final double radius, final double mass, final double x, final double y, final double vx, final double vy) {
//		this.id = count++;
//		this.radius = radius;
//		this.mass = mass;
//		this.position = new Point2D.Double(x, y);
//		this.velocity = new Point2D.Double(vx, vy);
//		this.neighbors = new HashSet<>();
//	}
//	
	public Particle(int id, double radius, double mass, double x, double y, double z, double vx, double vy, double vz) {
		count++;
		this.id = id;
		this.radius = radius;
		this.mass = mass;
		this.position = new Point(x, y, z);
		this.velocity = new Point(vx, vy, vz);
	}
	
	public Particle(int id, double radius, double x, double y, double z, double vx, double vy, double vz) {
		count++;
		this.id = id;
		this.radius = radius;
		this.position = new Point(x, y, z);
		this.velocity = new Point(vx, vy, vz);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof Particle))
			return false;
		Particle other = (Particle) o;
		return this.id == other.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
//	@Override
//	public String toString() {
//		return "id: " + id + "; radius: " + radius + " ; mass: " + mass + " ; x: " + position.x
//				+ " ; y: " + position.y + " ; vx: " + velocity.x + " ; vy: " + velocity.y;
//	}
	
	@Override
	public Particle clone() {
		return new Particle(id, radius, mass, position.getX(), position.getY(), position.getZ(), 
				velocity.getX(), velocity.getY(), velocity.getZ());
	}

	public int getId() {
		return id;
	}

	public double getRadius() {
		return radius;
	}

	public double getMass() {
		return mass;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(double x, double y, double z) {
		position.setX(x);
		position.setY(y);
		position.setZ(z);
	}
	
	public void setPosition(Point p) {
		position = p;
	}

	public Point getVelocity() {
		return velocity;
	}

	public void setVelocity(double vx, double vy, double vz) {
		velocity.setX(vx);
		velocity.setY(vy);
		velocity.setZ(vz);
	}
	
	public void setVelocity(Point v) {
		velocity = v;
	}
	
	public boolean inContact(Particle other) {
		return this.getPosition().getDiffVector(other.getPosition()).getNorm() < this.getRadius() + other.getRadius();
	}
	
//	public double getBorderToBorderDistance(final Particle other) {
//		double horizontalDistance = Math.abs(getPosition().x - other.getPosition().x);
//		double verticalDistance = Math.abs(getPosition().y - other.getPosition().y);
//		return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2)) - getRadius() - other.getRadius();
//	}
//	
//	public double getCenterToCenterDistance(final Particle other) {
//		double horizontalDistance = Math.abs(getPosition().x - other.getPosition().x);
//		double verticalDistance = Math.abs(getPosition().y - other.getPosition().y);
//		return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2));
//	}
//	
//	public Point2D.Double getRelativeVelocity(final Particle other) {
//		return new Point2D.Double(velocity.getX() - other.getVelocity().getX(), velocity.getY() - other.getVelocity().getY());
//	}

}
