package ar.edu.itba.ss.tpf;

public class Point {
	private double x;
	private double y;
	private double z;
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Point normalize() {
		return this.getScalarDivision(this.getNorm());
	}
	
	public double getNorm() {
		return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
	}
	
	public Point getScalarDivision(double n) {
		return this.getScalarMultiplication(1 / n);
	}
	
	public Point getScalarMultiplication(double n) {
		return new Point(this.x * n, this.y * n, this.z * n);
	}
	
	public Point getDiffVector(Point other) {
		return new Point(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
	}
	
	public Point getDirectionUnitVector(Point other) {
		return this.getDiffVector(other).normalize();
	}
	
	public Point getSumVector(Point other) {
		return new Point(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
	}
	
	public double getDotProduct(Point other) {
		return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

}
