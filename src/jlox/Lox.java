package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
	static boolean hadError = false;	// ensures code with known error is not executed
	
	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);	// wrong number of arguments
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}
	
	/**
	 * Reads a Lox file from the given path and executes it.
	 * Loads the file contents into memory, converts the raw bytes
	 * to a String using the system default charset, then passes it
	 * to the interpreter to run.
	 * 
	 * @param path			the path to the Lox file to execute
	 * @throws IOException	if the file cannot be found or read 
	 */
	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path)); // loads file into RAM
		run(new String(bytes, Charset.defaultCharset()));
		
		if (hadError) System.exit(65);	// invalid input
	}
	
	/**
	 * Starts an interactive REPL session.
	 * Continuously prompts the user for input, executes each line
	 * as a statement, and loops until the user exits.
	 * 
	 * @throws IOException	if an error occurs while reading user input
	 */
	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in); // converts raw bytes from user input into characters
		BufferedReader reader = new BufferedReader(input); // wraps input to allow reading characters as a full line
		
		for (;;) {
			System.out.print("> ");
			String line = reader.readLine(); // collects characters from reader into a full string
			if (line == null) break;
			run(line);
			hadError = false;
		}
	}
	
	/**
	 * NOT FINISHED YET
	 * 
	 * @param source	the Lox source code to scan and execute
	 */
	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		
		for (Token token : tokens) {
			System.out.println(token);
		}
	}
	
	/**
	 * Reports an error at the given line number.
	 * 
	 * @param line 		the line number where the error occurred
	 * @param message 	a description of the error
	 */
	static void error(int line, String message) {
		report(line, "", message);
	}
	
	/**
	 * Helper function that formats and prints an error message to the
	 *  standard error stream and flags that an error has occurred.
	 * 
	 * @param line		the line number where the error occurred
	 * @param where		the location in the source code where the error occurred
	 * @param message	a description of the error
	 */
	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + ": " + message);
		hadError = true;
	}
}
