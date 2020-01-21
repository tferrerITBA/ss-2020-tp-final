package ar.edu.itba.ss.tpf;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Grid {
	
	private List<Particle> particles;
	private final double width;
	private final double depth;
	private final double height;
	
	public Grid(final List<Particle> particles) {
		this.particles = particles;
		this.width = Configuration.WIDTH;
		this.depth = Configuration.DEPTH;
		this.height = Configuration.HEIGHT;
	}
	
	public List<Particle> getParticles() {
		return Collections.unmodifiableList(particles);
	}
	
	public void setParticles(List<Particle> newParticles) {
		Objects.requireNonNull(newParticles);
		particles = newParticles;
	}

	public double getWidth() {
		return width;
	}

	public double getDepth() {
		return depth;
	}

	public double getHeight() {
		return height;
	}
	
}
