package com.sofa.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.sofa.motifs.Motif;

public class Launcher {
	
	//public static RESTPadManager restpadManager;
	public static Long execTime = 0L;
	public static Map<String,String> abbrv = new HashMap<String, String>();
	/**
	 * Main restpad method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// initializations
		
		initRestpadManager();
		
		System.out.println("Reading dictionary...");
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File("../restpad/abbrv.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (fileScanner.hasNextLine()){
			String[] tmp = fileScanner.nextLine().split("\t");
			//System.out.println(tmp[0].toLowerCase() + " " + tmp[1].toLowerCase());
			abbrv.put(tmp[0].toLowerCase(), tmp[1].toLowerCase());
		}
		System.out.println("Dictionary read...");
		
		// choses a composite name
		String choice = Launcher.choseComposite(getComposites());
		
	}
	
	/**
	 * Initializes the restpad manager and its pattern to detect
	 */
	private static void initRestpadManager() {
		
		//restpadManager = new RESTPadManager();
		
		//Patterns
		for (Motif m : Motif.patterns()) {
			//restpadManager.addMotifToDetection(m);
		}
				
		//Anti-Patterns
		for (Motif m : Motif.antiPatterns()) {
			//restpadManager.addMotifToDetection(m);
		}
	}
	
	/**
	 * @return : A list wich contains all available composites
	 */
	private static List<String> getComposites() {
		File f = new File("repository/rsc/composites");
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
		ArrayList<String> namesCorrect = new ArrayList<String>();
		for (String name : names)
			namesCorrect.add(name.replace(".composite", ""));
		
		return namesCorrect;
	}
	
	/**
	 * List of APIs which dosent work
	 * @return
	 */
	private static ArrayList<String> faultyAPIs() {
		ArrayList<String> results = new ArrayList<String>();
		results.add("flickr");
		//results.add("externalip");
		//results.add("facebook-rest");
		//results.add("zappos");
		return results;
	}
	
	/**
	 * Asks the user wich composite to execute
	 * @param composites
	 * @return
	 */
	private static String choseComposite(List<String> composites) {
		
		// faulty APIs
		ArrayList<String> faulties = faultyAPIs();
		
		// composite selection
		System.out.println("Choose composite:");
		int number = 0;
		for (String composite : composites) {
			number++;
			String line = new String();
			if (faulties.contains(composite)) 
				line += "X";
			else{
				line += number;
				
			}
			line += " - " + composite;
			System.out.println(line);
		}
		
		// chose
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		int value = 0;
		String composite = new String();
		while (value == 0) {
			value = in.nextInt();
			if (!(value > 0 && value <= number)) {
				// access to the tool menu
				if (value == number + 1){
					return "tool-menu";
				} else {
					value = 0;
					System.out.println("Value must be between 0 and " + (number + 1) + " (excluded)");
				}
			
			} else {
				composite = composites.get(value - 1);
				if (faulties.contains(composite)) {
					System.out.println("This composite is faulty - continue ? (y/n)");
					String buffer = in.next();
					if (!(buffer.equals("y") || buffer.equals("Y"))) {
						value = 0;
					}
				}
			}
		}
		return composite;
	}
	
	private static void toolMenu() {
		
		boolean loop = true;
		
		while(loop) {
			System.out.println("=== Tool Menu ===");
			System.out.println("1 - Delete results");
			System.out.println("2 - Exit");
			System.out.println("=== Choice ?");
			
			@SuppressWarnings("resource")
			Scanner in = new Scanner(System.in);
			int choice = 0;
			while (choice <= 0 || choice > 1) {
				choice = in.nextInt();
			}
			
			switch(choice) {
			// delete results
			case 1 :
				OutputFormatter.getInstance().deleteResults();
				break;
				
			case 2 :
				loop = false;
				break;
			}
		}
		
	}
}