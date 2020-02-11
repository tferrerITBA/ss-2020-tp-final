package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
	
	private static Grid grid;
	private static PredictiveCollisionAvoidance pca;
	private static BeemanIntegrator integrator;
	private static double accumulatedTime = 0.0;
	private static double timeStep;
	
	private static final double GOAL_EPSILON = 0.5;
	
	public static void main(String[] args) {
		Configuration.requestParameters();
		long startTime = System.nanoTime();
		executeSingleRun();
		long endTime = System.nanoTime();
		System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
	}
	
	private static void executeSingleRun() {
		timeStep = Configuration.TIME_STEP;
		List<Particle> particles = Configuration.initializeParticles();
		
		grid = new Grid(particles);
		pca = new PredictiveCollisionAvoidance(grid);
		integrator = new BeemanIntegrator(grid, pca);
		
		execute();
	}
	
	public static void execute() {
		double accumulatedPrintingTime = 0.0;
		double printingTimeLimit = 0.05; //s
    	
		List<Particle> currentParticles = new ArrayList<>();
		currentParticles.add(grid.getRebelShip());
		currentParticles.addAll(grid.getDrones());
    	List<Particle> prevParticles = integrator.initPrevParticles(currentParticles);
    	
		while(!simulationEnded()) {
			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid);
				accumulatedPrintingTime = 0;
			}
			accumulatedTime += timeStep;
			accumulatedPrintingTime += timeStep;
			
			for(Turret turret : grid.getTurrets()) {
				turret.fire(timeStep, grid);
			}
			for(Drone drone : grid.getDrones()) {
				drone.fire(timeStep, grid);
			}
			
			List<Particle> updatedParticles = integrator.updateParticles(prevParticles);
			integrator.updateParticleLists(prevParticles, currentParticles, updatedParticles);
			
			grid.deleteOutOfBoundsProjectiles();
			grid.checkDroneCollisions();
		}
	}
	
	private static boolean simulationEnded() {
		if(grid.rebelShipHasCollided()) {
			System.out.println("Ship has been destroyed.");
			return true;
		}
		
		if(pca.getRebelShipGoal().getDiffVector(grid.getRebelShip().getPosition()).getNorm() < GOAL_EPSILON) {
			System.out.println("Death star has been destroyed!");
			return true;
		}
		
		if(!Configuration.isGoalTimeLimit()) {
			return Double.compare(accumulatedTime, Configuration.getTimeLimit()) >= 0;
		}
		
		return false;
	}

}
