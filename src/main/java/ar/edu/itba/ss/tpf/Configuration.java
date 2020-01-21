package ar.edu.itba.ss.tpf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Configuration {

	private static String INPUT_FILE_NAME = "config.txt";
	private static String OUTPUT_FILE_NAME = "ovito_output.xyz";
	public static final double WIDTH = 200.0; // m
	public static final double HEIGHT = 50.0; // m
	public static final double DEPTH = 100.0; // m
	
	public static final double REBEL_SHIP_RADIUS = 2.0; // m
	public static final double REBEL_SHIP_MASS = 1.0; // m CAMBIAR
	public static final Point REBEL_SHIP_INIT_POSITION = new Point(WIDTH / 8, HEIGHT / 2, DEPTH / 2);
	
	public static final double DEATH_STAR_RADIUS = 20.0; // m
	public static final double DEATH_STAR_MASS = 10.0; // m CAMBIAR
	
	public static final int PARTICLE_COUNT = 2;
	
	public static final double A_CONSTANT = 2000; // N
	public static final double B_CONSTANT = 0.08; // m
	public static final double MIN_PARTICLE_RADIUS = 0.25; // m
	public static final double MAX_PARTICLE_RADIUS = 0.35; // m
	public static final double K_NORM = 1.2e5; // kg/s^2
	public static final double K_TANG = 2 * K_NORM; // kg/(m s)
	//public static final double PARTICLE_MASS = 80; // kg
	public static final double TAU = 0.5; // s
	public static final double DESIRED_VEL = 0.8; // m/s
	//private static double timeStep = 0.1 * Math.sqrt(PARTICLE_MASS / K_NORM);
	public static final double TIME_STEP = 0.1;// * Math.sqrt(PARTICLE_MASS / K_NORM); // s
	private static double timeLimit;
//	private static final int INVALID_POSITION_LIMIT = 500;
	private static String fileName = "";
	
	public static void requestParameters() {
		Scanner scanner = new Scanner(System.in);
	    
	    System.out.println("Enter Time Limit:");
    	Double selectedTimeLimit = null;
	    while(selectedTimeLimit == null || selectedTimeLimit <= 0) {
	    	selectedTimeLimit = stringToDouble(scanner.nextLine());
	    }
	    timeLimit = selectedTimeLimit;
	    
	    System.out.println("Enter Filename:");
	    while(fileName == "") {
	    	fileName = scanner.nextLine();
		}
		INPUT_FILE_NAME = fileName + "-input.txt";
		OUTPUT_FILE_NAME = fileName + ".xyz";
	    
	    scanner.close();
	}
	
	/* Parameters must have already been requested */
	public static List<Particle> generateRandomInputFilesAndParseConfiguration() {
		List<Particle> particles = generateParticles();
		generateInputFile(particles);
		generateOvitoOutputFile();
		return particles;
	}

	private static void generateInputFile(List<Particle> particles) {
//		int invalidPositions = 0;
		File inputFile = new File(INPUT_FILE_NAME);
		inputFile.delete();
//		Random r = new Random();

		try(FileWriter fw = new FileWriter(inputFile)) {
			inputFile.createNewFile();
			fw.write("0\n");

			int i;
			for(i = 0; i < particles.size(); i++) {
				Particle p = particles.get(i);
				
				if(i > 1) {
	//				double randomPositionX = 0;
	//				double randomPositionY = 0;
	//				boolean isValidPosition = false;
	//
	//				while (!isValidPosition) {
	//					//randomPositionX = (externalRadius * 2 - 2 * p.getRadius()) * r.nextDouble() + p.getRadius();
	//					//randomPositionY = (externalRadius * 2 - 2 * p.getRadius()) * r.nextDouble() + p.getRadius();
	//					isValidPosition = validateParticlePosition(particles, randomPositionX, randomPositionY, p.getRadius());
	//
	//					invalidPositions += (isValidPosition) ? 0 : 1;
	//				}
	//				if (invalidPositions > INVALID_POSITION_LIMIT) break;
	//				invalidPositions = 0;
	//				p.setPosition(randomPositionX, randomPositionY);
				}

				fw.write(p.getId() + " " + p.getRadius() + " " 
						+ p.getPosition().getX() + " " + p.getPosition().getY() + " " + p.getPosition().getZ() + " " 
						+ p.getVelocity().getX() + " " + p.getVelocity().getY() + " " + p.getVelocity().getX() + "\n");
			}
//			particles.removeAll(particles.subList(i, particles.size()));
//			particleCount = particles.size();
		} catch (IOException e) {
			System.err.println("Failed to create input file.");
			e.printStackTrace();
		}
	}
	
//	private static Integer stringToInt(String s) {
//		Integer i = null;
//		try {
//			i = Integer.valueOf(s);
//		} catch(NumberFormatException e) {
//			return null;
//		}
//		return i;
//	}
	
	private static Double stringToDouble(String s) {
		Double d = null;
		try {
			d = Double.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return d;
	}
	
	/* Time (0) */
    private static List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>();

//		Random r = new Random();
		
		particles.add(createRebelShip());
		particles.add(createDeathStar());

//		for(int i = 0; i < particleCount; i++) {
//			double radius = r.nextDouble() * (MAX_PARTICLE_RADIUS - MIN_PARTICLE_RADIUS) + MIN_PARTICLE_RADIUS;
//			Particle p = new Particle(radius, PARTICLE_MASS, 0, 0, INIT_VEL, INIT_VEL);
//			particles.add(p);
//		}

		return particles;
    }
    
    private static Particle createRebelShip() {
    	return new Particle(REBEL_SHIP_RADIUS, REBEL_SHIP_MASS, REBEL_SHIP_INIT_POSITION.getX(), 
				REBEL_SHIP_INIT_POSITION.getY(), REBEL_SHIP_INIT_POSITION.getZ());
	}
    
    private static Particle createDeathStar() {
    	double initX = WIDTH - DEATH_STAR_RADIUS - (DEPTH - 2 * DEATH_STAR_RADIUS) / 2;
    	return new Particle(DEATH_STAR_RADIUS, DEATH_STAR_MASS, initX, HEIGHT / 2, DEPTH / 2);
	}

//	public static boolean validateParticlePosition(final List<Particle> particles,
//												   final double randomPositionX,
//												   final double randomPositionY,
//												   final double radius) {
//        if(false) {
//        	return false;
//		}
//    	for(Particle p : particles) {
//    		if(Double.compare(p.getPosition().getX(), 0) == 0
//					&& Double.compare(p.getPosition().getY(), 0) == 0) {
//    			continue;
//			}
//            if(Math.sqrt(Math.pow(p.getPosition().getX() - randomPositionX, 2) + Math.pow(p.getPosition().getY() - randomPositionY, 2))
//                    < (p.getRadius() + radius))
//                return false;
//        }
//        return true;
//    }

	private static void generateOvitoOutputFile() {
		File outputFile = new File(OUTPUT_FILE_NAME);
		outputFile.delete();
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create Ovito output file.");
			e.printStackTrace();
		}
	}
	
	public static void writeOvitoOutputFile(double time, List<Particle> particles) {
		File outputFile = new File(OUTPUT_FILE_NAME);
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			fw.write(PARTICLE_COUNT + "\n");
			fw.write("Lattice=\"" + WIDTH + " 0.0 0.0 0.0 " + DEPTH + " 0.0 0.0 0.0 " + HEIGHT
				+ "\" Properties=id:I:1:radius:R:1:pos:R:3:velo:R:3 Time=" + String.format(Locale.US, "%.2g", time) + "\n");
			for(Particle p : particles) {
				writeOvitoParticle(fw, p);
			}
		} catch (IOException e) {
			System.err.println("Failed to write Ovito output file.");
			e.printStackTrace();
		}
	}
	
	private static void writeOvitoParticle(FileWriter fw, Particle particle) throws IOException {
		fw.write(particle.getId() + " " + particle.getRadius() + " " 
				+ particle.getPosition().getX() + " " + particle.getPosition().getZ() + " " + particle.getPosition().getY() + " " 
				+ particle.getVelocity().getX() + " " + particle.getVelocity().getZ() + " " + particle.getVelocity().getY());
		fw.write('\n');
	}
	
	public static double getTimeLimit() {
		return timeLimit;
	}

//	public static double getTimeStep() {
//		return timeStep;
//	}
}
