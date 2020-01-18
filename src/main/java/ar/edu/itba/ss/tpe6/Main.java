package ar.edu.itba.ss.tpe6;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
	
	public static void main(String[] args) {
		Configuration.requestParameters();
		long startTime = System.nanoTime();
		executeSingleRun();
		long endTime = System.nanoTime();
		System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
	}
	
	private static void executeSingleRun() {
		List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
		Configuration.writeOvitoOutputFile(0, particles);
		Grid grid = new Grid(particles);
		SocialForceManager manager = new SocialForceManager(grid);
		manager.execute();
	}

}
