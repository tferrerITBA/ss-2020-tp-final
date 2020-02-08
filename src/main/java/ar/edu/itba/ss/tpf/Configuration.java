package ar.edu.itba.ss.tpf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Configuration {

	private static String INPUT_FILE_NAME = "config.txt";
	private static String OUTPUT_FILE_NAME = "ovito_output.xyz";
	public static final double WIDTH = 200.0; // m
	public static final double HEIGHT = 100.0; // m
	public static final double DEPTH = 100.0; // m
	
	public static final double REBEL_SHIP_RADIUS = 1.5; // m
	//public static final double REBEL_SHIP_MASS = 1.0; // m CAMBIAR
	public static final Point REBEL_SHIP_INIT_POSITION = new Point(WIDTH / 8, HEIGHT / 2, DEPTH / 2);
	
	public static final double DEATH_STAR_RADIUS = 20.0; // m
	//public static final double DEATH_STAR_MASS = 10.0; // m CAMBIAR
	public static Point DEATH_STAR_POSITION = new Point(WIDTH - DEATH_STAR_RADIUS - (DEPTH - 2 * DEATH_STAR_RADIUS) / 2, HEIGHT / 2, DEPTH / 2);
	
	private static List<Turret> turrets = new ArrayList<Turret>();
	public static final double TURRET_RADIUS = 0.5; // m
	public static final double TURRET_FIRE_RATE = 5; // s
	public static final int PROJECTILE_PARTICLE_COUNT = 3;
	public static final double TURRET_PROJECTILE_RADIUS = 0.3; // m
	public static final double TURRET_PROJECTILE_SPEED = 25; // m/s
	public static final int TURRET_COUNT = 5;
	
	private static List<Drone> drones = new ArrayList<Drone>();
	public static final double DRONE_RADIUS = 1.0; // m
	public static final double DRONE_DESIRED_VEL = 5.0;
	public static final double DRONE_MAX_VEL = DRONE_DESIRED_VEL;
	public static final int DRONE_COUNT = 10;
	
	public static final double COLLISION_PREDICTION_TIME_LIMIT = 1.5;
	public static final int COLLISION_AWARENESS_COUNT = 30;
	
	public static final double REBEL_SHIP_TO_PROJECTILE_PERSONAL_SPACE = REBEL_SHIP_RADIUS + 0.5;
	public static final double DRONE_TO_PROJECTILE_PERSONAL_SPACE = DRONE_RADIUS + 0.5;
	public static final double DRONE_TO_DRONE_PERSONAL_SPACE = 5.0;
	public static final double DRONE_TO_REBEL_SHIP_PERSONAL_SPACE = 15.0;
	public static final double REBEL_SHIP_TO_DRONE_PERSONAL_SPACE = 15.0;
	public static final double REBEL_SHIP_TO_TURRET_PERSONAL_SPACE = 15.0;
	public static final double WALL_SAFE_DISTANCE = 10.0;
	public static final double DEATH_STAR_SAFE_DISTANCE = 1.0;
	public static final int K_CONSTANT = 2;
	
	public static final double TIME_STEP = 0.0001;// * Math.sqrt(PARTICLE_MASS / K_NORM); // s
	public static final double DESIRED_VEL = 5.0; // m/s
	public static final double REBEL_SHIP_MAX_VEL = 3 * DESIRED_VEL;
	public static final double TAU = 0.5; // s
	
	private static double timeLimit;
	private static boolean readFromFile;
	private static String fileName = "";
	///////
//	public static final double A_CONSTANT = 2000; // N
//	public static final double B_CONSTANT = 0.08; // m
//	public static final double MIN_PARTICLE_RADIUS = 0.25; // m
//	public static final double MAX_PARTICLE_RADIUS = 0.35; // m
//	public static final double K_NORM = 1.2e5; // kg/s^2
//	public static final double K_TANG = 2 * K_NORM; // kg/(m s)
	//public static final double PARTICLE_MASS = 80; // kg
	//private static double timeStep = 0.1 * Math.sqrt(PARTICLE_MASS / K_NORM);
//	private static final int INVALID_POSITION_LIMIT = 500;
	
	public static void requestParameters() {
		Scanner scanner = new Scanner(System.in);
	    
	    System.out.println("Enter Time Limit [Press Enter for goal limit]:");
    	Double selectedTimeLimit = null;
    	boolean hasTimeLimitInput = false;
	    while(!hasTimeLimitInput || (selectedTimeLimit != null && selectedTimeLimit <= 0)) {
	    	selectedTimeLimit = stringToDouble(scanner.nextLine());
	    	if(!hasTimeLimitInput)
	    		hasTimeLimitInput = true;
	    }
	    if(selectedTimeLimit == null)
	    	timeLimit = -1;
	    else
	    	timeLimit = selectedTimeLimit;
	    
	    System.out.println("Read data from input file? [y/n]");
	    String readFile = "";
	    while(!readFile.equals("y") && !readFile.equals("n")) {
	    	readFile = scanner.nextLine();
		}
	    readFromFile = readFile.equals("y");
	    
	    System.out.println("Enter Filename:");
	    while(fileName == "") {
	    	fileName = scanner.nextLine();
		}
		INPUT_FILE_NAME = fileName + "-input.txt";
		OUTPUT_FILE_NAME = fileName + ".xyz";
	    
	    scanner.close();
	}
	
	public static List<Particle> initializeParticles() {
		if(readFromFile) {
			return parseInputFile();
		} else {
			return generateRandomInputFiles();
		}
	}
	
	private static List<Particle> parseInputFile() {
		List<Particle> particles = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
			String line = br.readLine();
			int index = 0;
			while(line != null) {
				String[] attributes = line.split(" ");
				attributes = removeSpaces(attributes);
				particles.add(parseParticle(attributes, index));
				
				line = br.readLine();
				index++;
			}
		} catch(Exception e) {
			System.err.println("Failed to read input file.");
			e.printStackTrace();
		}
		
		return particles;
	}

	/* Parameters must have already been requested */
	public static List<Particle> generateRandomInputFiles() {
		List<Particle> particles = generateParticles();
		generateInputFile(particles);
		generateOvitoOutputFile();
		return particles;
	}

	private static void generateInputFile(List<Particle> particles) {
		File inputFile = new File(INPUT_FILE_NAME);
		inputFile.delete();

		try(FileWriter fw = new FileWriter(inputFile)) {
			inputFile.createNewFile();
			//fw.write("0\n");

			for(Particle p : particles) {
				fw.write(p.getId() + " " + p.getRadius() + " " 
						+ p.getPosition().getX() + " " + p.getPosition().getY() + " " + p.getPosition().getZ() + " " 
						+ p.getVelocity().getX() + " " + p.getVelocity().getY() + " " + p.getVelocity().getX() + "\n");
			}
		} catch (IOException e) {
			System.err.println("Failed to create input file.");
			e.printStackTrace();
		}
	}
	
	private static Integer stringToInt(String s) {
		Integer i = null;
		try {
			i = Integer.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return i;
	}
	
	private static Double stringToDouble(String s) {
		Double d = null;
		try {
			d = Double.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return d;
	}
	
	private static String[] removeSpaces(String[] array) {
		List<String> list = new ArrayList<>(Arrays.asList(array));
		List<String> filteredList = list.stream().filter(s -> !s.equals("") && !s.equals(" ")).collect(Collectors.toList());
		String[] newArray = new String[filteredList.size()];
		return filteredList.toArray(newArray);
	}
	
	private static Particle parseParticle(String[] attributes, int index) {
		Integer id = null;
		Double radius = null, x = null, y = null, z = null, vx = null, vy = null, vz = null;
		if(attributes.length != 8
			|| (id = stringToInt(attributes[0])) == null || id < 0
			|| (radius = stringToDouble(attributes[1])) == null || radius < 0
			|| (x = stringToDouble(attributes[2])) == null || radius < 0
			|| (y = stringToDouble(attributes[3])) == null || radius < 0
			|| (z = stringToDouble(attributes[4])) == null || radius < 0
			|| (vx = stringToDouble(attributes[5])) == null || radius < 0
			|| (vy = stringToDouble(attributes[6])) == null || radius < 0
			|| (vz = stringToDouble(attributes[7])) == null || radius < 0) {
				failWithMessage(attributes + " is an invalid Particle.");
			}
		
		if(index == 0) {
			/* Rebel Ship */
			return new RebelShip(id, radius, x, y, z, vx, vy, vz);
		} else if(index == 1) {
			/* Death Star */
			return new DeathStar(id, radius, x, y, z, vx, vy, vz);
		} else if(index > 1 && index <= 1 + 3 * TURRET_COUNT) {
			/* Turret */
			Turret turret = new Turret(id, radius, x, y, z, vx, vy, vz);
			turrets.add(turret);
			return turret;
		} else {
			/* Drone */
			Drone drone = new Drone(id, radius, x, y, z, vx, vy, vz);
			drones.add(drone);
			return drone;
		}
	}
	
	private static void failWithMessage(String message) {
		System.err.println(message);
		System.exit(1);
	}
	
	/* Time (0) */
    private static List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>();
		
		particles.add(createRebelShip());
		particles.add(createDeathStar());
		createTurrets();
		particles.addAll(turrets);
		createDrones();
		particles.addAll(drones);

		return particles;
    }

	private static Particle createRebelShip() {
    	return new RebelShip(REBEL_SHIP_RADIUS, REBEL_SHIP_INIT_POSITION.getX(), 
				REBEL_SHIP_INIT_POSITION.getY(), REBEL_SHIP_INIT_POSITION.getZ());
	}
    
    private static Particle createDeathStar() {
    	return new DeathStar(DEATH_STAR_RADIUS, DEATH_STAR_POSITION.getX(), DEATH_STAR_POSITION.getY(),
    			DEATH_STAR_POSITION.getZ());
	}
    
    @SuppressWarnings("unused")
	private static void createTurrets() {
    	if(TURRET_COUNT == 0) {
    		return;
    	}
    	
    	createTurretLayer(DEATH_STAR_POSITION.getX() - DEATH_STAR_RADIUS, DEATH_STAR_POSITION.getY());
    	createTurretLayer(DEATH_STAR_POSITION.getX() - 0.866 * DEATH_STAR_RADIUS, DEATH_STAR_POSITION.getY() + DEATH_STAR_RADIUS / 2);
    	createTurretLayer(DEATH_STAR_POSITION.getX() - 0.866 * DEATH_STAR_RADIUS, DEATH_STAR_POSITION.getY() - DEATH_STAR_RADIUS / 2);
    	
    	return;
	}
    
    private static void createTurretLayer(double x, double y) {
    	double z = DEATH_STAR_POSITION.getZ();
    	/* Turrets are evenly spaced */
    	turrets.add(new Turret(x, y, z));
    	
    	Point diffVector = (new Point(x, y, z)).getDiffVector(DEATH_STAR_POSITION);
    	double deltaAngle = 2 * Math.PI / TURRET_COUNT;
    	for(int i = 1; i < TURRET_COUNT; i++) {
    		
    		x = diffVector.getX() * Math.cos(deltaAngle) - diffVector.getZ() * Math.sin(deltaAngle);
    		z = diffVector.getX() * Math.sin(deltaAngle) + diffVector.getZ() * Math.cos(deltaAngle);
    		
    		diffVector.setX(x);
    		diffVector.setZ(z);
    		
    		Point newTurretPosition = DEATH_STAR_POSITION.getSumVector(diffVector);
    		turrets.add(new Turret(newTurretPosition.getX(), newTurretPosition.getY(), newTurretPosition.getZ()));
    	}
    }
    
    private static void createDrones() {
    	Random r = new Random();
    	double maxX = DEATH_STAR_POSITION.getX() - DEATH_STAR_RADIUS - DRONE_RADIUS;
    	double minX = (maxX - REBEL_SHIP_INIT_POSITION.getX()) * 3.0 / 4.0 + REBEL_SHIP_INIT_POSITION.getX();
		for(int i = 0; i < DRONE_COUNT; i++) {
			boolean isValidPosition = false;
			double x = 0, y = 0, z = 0;
			while(!isValidPosition) {
				x = r.nextDouble() * (maxX - minX) + minX;
				y = r.nextDouble() * (HEIGHT - 2 * DRONE_RADIUS) + DRONE_RADIUS;
				z = r.nextDouble() * (DEPTH - 2 * DRONE_RADIUS) + DRONE_RADIUS;
				isValidPosition = validateDronePosition(x, y, z);
			}
			drones.add(new Drone(x, y, z));
		}
	}

	public static boolean validateDronePosition(final double x, final double y, final double z) {
		/* Only drones may overlap initially */
    	for(Drone drone : drones) {
            if(Math.sqrt(Math.pow(drone.getPosition().getX() - x, 2) + Math.pow(drone.getPosition().getY() - y, 2)
            		+ Math.pow(drone.getPosition().getZ() - z, 2)) < 2 * DRONE_RADIUS)
                return false;
        }
        return true;
    }

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
			fw.write(particles.size() + "\n");
			fw.write("Lattice=\"" + WIDTH + " 0.0 0.0 0.0 " + DEPTH + " 0.0 0.0 0.0 " + HEIGHT
				+ "\" Properties=id:I:1:radius:R:1:pos:R:3:velo:R:3 Time=" + String.format(Locale.US, "%.3g", time) + "\n");
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
	
	public static List<Turret> getTurrets() {
		return turrets;
	}
	
	public static List<Drone> getDrones() {
		return drones;
	}
	
	public static double getTimeLimit() {
		return timeLimit;
	}
	
	public static boolean isGoalTimeLimit() {
		return Double.compare(timeLimit, -1.0) == 0;
	}

//	public static double getTimeStep() {
//		return timeStep;
//	}
}
