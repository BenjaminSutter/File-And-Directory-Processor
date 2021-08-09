/*
 * File: Homework6.java
 * Author: Ben Sutter
 * Date: April 19th, 2021
 * Purpose: Perform various file system methods when given a file directory to work with.
 * Capable of encrypting and decrypting files using XOR.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Homework6 {

    Path absolutePath;//Saves the absolute path from option 1 for use in options 2-7
    Scanner scan = new Scanner(System.in);//Scanner is used in multiple methods so declare here

    //Stores the menu options in a method for easy access.
    public void displayMenu() {
        System.out.println("\nPlease select an option from the menu below"
                + "\n1. Select directory"
                + "\n2. List directory content (first level)"
                + "\n3. List directory content (all levels)"
                + "\n4. Delete file"
                + "\n5. Display file (hexadecimal view)"
                + "\n6. Encrypt file (XOR with password)"
                + "\n7. Decrypt file (XOR with password)"
                + "\nQ. Quit program");
    }

    private void selectDirectory() {
        System.out.println("\nPlease enter the directory:");
        String inputPath = scan.nextLine();
        while (true) {
            if (Files.isDirectory(Paths.get(inputPath))) {
                absolutePath = Paths.get(inputPath);
                System.out.println("\nValid path: Directory is now " + absolutePath.toString());
                break;
            } else if (new File(inputPath).isFile()) {
                System.out.println('"' + inputPath + '"' + " is a file and not a valid directory. Please enter a valid one:");
            } else {
                System.out.println('"' + inputPath + '"' + " is not a valid file path. Please enter a valid one:");
            }
            inputPath = scan.nextLine();
        }
    }

    private void listDirectoryContent() {
        //Creates a string array based on all current files in given directory
        String[] path = new File(absolutePath.toString()).list();
        //Iterates through the array to display the files (along with the directory path)
        System.out.println("The files in the first level of : " + '"' + absolutePath + '"' + " are:\n");
        for (String file : path) {
            System.out.println(file);
        }
    }

    private void listAllDirectoryContent() {
        try {
            System.out.println("The files in all levels of : " + '"' + absolutePath + '"' + " are:\n");
            //Recursive method from: https://stackabuse.com/java-list-files-in-a-directory/
            Files.walk(absolutePath).filter(Files::isRegularFile).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile() {
        System.out.println("\nWhat is the name of the file you wish to delete?");
        String fileName = scan.nextLine();
        File file = new File(absolutePath + "\\" + fileName);
        if (file.delete()) {
            System.out.println('"' + fileName + '"' + " deleted successfully");
        } else {
            System.out.println("Failed to delete " + '"' + fileName + '"' + ", file does not exist in given directory");
        }
    }

    public void displayHexadecimalFile() {
        System.out.println("\nWhat is the name of the file you wish to see in hexadecimal view?");
        String fileName = scan.nextLine();
        File file = new File(absolutePath + "\\" + fileName);

        //Code in try block from: https://kodejava.org/how-do-i-display-file-contents-in-hexadecimal/
        try (FileInputStream fis = new FileInputStream(file)) {

            int i = 0;// A variable to hold a single byte of the file data
            int count = 0;// A counter to print a new line every 16 bytes read.
            // Read till the end of the file and print the byte in hexadecimal values.
            while ((i = fis.read()) != -1) {
                System.out.printf("%02X ", i);
                count++;

                if (count == 16) {
                    System.out.println("");
                    count = 0;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println('"' + fileName + '"' + " was not found in the directory");
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("");//Blank line for some formatting
    }//End displayHexadecimal

    //Uses XOR to encrypt/decrypt the given file. Choice parameter determines different options displayed
    public void encryptOrDecryptFile(String choice) {

        //Saves different choice options based on whether or not the parameter = Encypt
        String[] choices = new String[3];
        if (choice == "Encrypt") {
            choices = new String[]{"Please enter a password to encrypt the file:",
                "Please choose a name for the encrypted file", "File encrypted successfully"};
        } else {
            choices = new String[]{"Please enter a password to decrypt the file:",
                "Please choose a name for the decrypted file",
                "No errors in decrypting attempt, if password was valid the file will open properly"};
        }
        String fileName = scan.nextLine();
        boolean writingProblem = false;//Used to keep track of where the IOException error is coming from

        try {
            //Try to create byte array based on the file. That way if file doesn't exist user need not to enter password.
            File file = new File(absolutePath + "\\" + fileName);
            byte[] fileByteArray = Files.readAllBytes(Paths.get(file.toString()));
            writingProblem = true;//If file was read sucessfully, then if there is an error it must be during the write

            System.out.println(choices[0]);//Ask user for password
            String password = scan.nextLine();
            while (password.getBytes().length > 256 || password.isBlank()) {
                System.out.println("Password was too long or left blank, please try again:");
                password = scan.nextLine();
            }
            byte[] passwordByteArray = password.getBytes();

            //Ecryption/decryption loop from: https://github.com/Jyasu/CMSC412-HW5/blob/master/Directory.java
            int j = 0;//Keeps track of the max index of the passwordByteArray
            for (int i = 0; i < fileByteArray.length; i++) {

                //When j becomes larrger than the size of the array, reset it to index 0
                if (j > passwordByteArray.length - 1) {
                    j = 0;
                }
                //Use Java's bitwise XOR operator to encrypt/decrypt the file
                //It will only copy the bit if one is 0 and the one is 1.
                fileByteArray[i] = (byte) (fileByteArray[i] ^ passwordByteArray[j]);
                j++;//Increment after each byte is added

            }

            System.out.println(choices[1]);//Ask for name of outgoing file
            String newFileName = scan.nextLine();
            //Create the new file based on name passed by user and the XOR encrpytion/decrpytion bytes
            File outgoingFile = new File(absolutePath + "\\" + newFileName);
            FileOutputStream stream = new FileOutputStream(outgoingFile);

            stream.write(fileByteArray);
            stream.close();

            System.out.println(choices[2]);//Success message of file createio
        } catch (IOException e) {
            //If the exception was thrown from writing the file, display it. Otherwise, exception was from reading.
            if (writingProblem) {
                System.out.println("Invalid name for outgoing file. Unable to write it.");
            } else {
                System.out.println('"' + fileName + '"' + " was not found in the current directory");
            }
        }
    }//End Encrypt/Decrypt
    
    public void initializeMenu() {
        displayMenu(); //Display menu choices
        String userInput = scan.nextLine();//Grab user input
        //While user input isn't equal to Q (quit) then loop
        while (!userInput.trim().equalsIgnoreCase("q")) {
            switch (userInput) {
                case "1"://Select directory
                    selectDirectory();
                    break;
                case "2"://List directory content (first level)
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        listDirectoryContent();
                    }
                    break;
                case "3"://List directory content (all levels)
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        listAllDirectoryContent();
                    }
                    break;
                case "4"://Delete file
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        deleteFile();
                    }
                    break;
                case "5"://Display file (hexadecimal view)
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        displayHexadecimalFile();
                    }
                    break;
                case "6"://Encrypt file (XOR with password)
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        System.out.println("\nWhat is the name of the file you wish to encrypt?");
                        encryptOrDecryptFile("Encrypt");//Determines what will be printed (different for encrypt)
                    }
                    break;
                case "7"://Decrypt file (XOR with password)
                    if (absolutePath == null) {
                        System.out.println("A directory has not been selected, please select one (option 1) before continuing");
                    } else {
                        System.out.println("\nWhat is the name of the file you wish to decrypt?");
                        //Determines what options will be printed (this can be anything except Encrpyt, passed Decrypt for clarity
                        encryptOrDecryptFile("Decrypt");
                    }
                    break;
                default:
                    System.out.println("That was not a vailid selection, please try again");
                    break;
            }//End switch
            //Display menu again and allow the user to input again
            displayMenu();
            userInput = scan.nextLine();
        }//End while
        System.out.println("Exiting program");
    }//End initializeMenu

    public static void main(String[] args) {
        Homework6 test = new Homework6();
        test.initializeMenu();
    }

}//End Homework6.java
