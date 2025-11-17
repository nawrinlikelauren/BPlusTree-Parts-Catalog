import java.io.*;
import java.util.*;
import java.util.Scanner;

public class Main {

    static BPlusTree bPlusTree = new BPlusTree();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Main mainInstance = new Main();
        mainInstance.loadData();

        while (true) {
            boolean changesMade = false; // Reset flag at the start of each loop

            System.out.println("Select an option:");
            System.out.println("1. Query a Record");
            System.out.println("2. Add New Record");
            System.out.println("3. Change Record");
            System.out.println("4. Delete Record");
            System.out.println("5. Display Next 10 Records");
            System.out.println("6. Search for Stats");
            System.out.println("7. Exit Program");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    System.out.println("Enter Part ID:");
                    String searchPartID = scanner.nextLine().trim();
                    String searchDescription = bPlusTree.searchNode(searchPartID);
                    System.out.println("Description of part " + searchPartID + " is: " + searchDescription);
                    break;

                case 2:
                    System.out.println("Enter Part ID for New Record:");
                    String partID = scanner.nextLine().trim();

                    // Check if the Part ID already exists
                    if (bPlusTree.searchNode(partID) != null) {
                        System.out.println("Part ID " + partID + " already exists. Insertion aborted. Enter a different Part ID.");
                    } else {
                        System.out.println("Enter Part Description for New Record:");
                        String description = scanner.nextLine().trim();

                        // Insert new record
                        bPlusTree.insertNode(partID, description);
                        System.out.println("Part ID " + partID + " added successfully.");
                        changesMade = true; // Mark changes
                    }
                    break;

                case 3:
                    System.out.println("Enter Part ID to Update:");
                    String updatePartID = scanner.nextLine().trim();
                    System.out.println("Enter Updated Part Description:");
                    String newDescription = scanner.nextLine().trim();

                    // Update record
                    bPlusTree.updateNode(updatePartID, newDescription);
                    changesMade = true; // Mark changes
                    break;

                case 4:
                    System.out.println("Enter Part ID to Delete:");
                    String deletePartId = scanner.nextLine().trim();

                    // Check if the Part ID exists
                    String existingDescription = bPlusTree.searchNode(deletePartId);
                    if (existingDescription == null) {
                        System.out.println("Part ID " + deletePartId + " does not exist. Deletion aborted.");
                    } else {
                        // Proceed with deletion
                        bPlusTree.deleteNode(deletePartId);
                        System.out.println("Part ID " + deletePartId + " deleted successfully.");
                        changesMade = true; // Mark changes
                    }
                    break;

                case 5:
                    System.out.println("Enter Part ID to print next 10:");
                    String partId = scanner.nextLine().trim();
                    ArrayList<Structure> structureList = bPlusTree.getNextTenNode(partId);

                    if (structureList == null || structureList.isEmpty()) {
                        System.out.println("Invalid Part ID or no next parts available.");
                    } else {
                        for (Structure structure : structureList) {
                            System.out.println("PartId: " + structure.getId() + " Description: " + structure.getDescription());
                        }
                    }
                    break;

                case 6:
                    // Display statistics
                    bPlusTree.printStaistic();
                    break;

                case 7:
                    // Save changes to file and exit
                    if (changesMade) {
                        mainInstance.saveData();
                    }
                    System.out.println("Exiting program.");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Option does not exist. Please enter a number between 1 and 7.");
            }

            // Ask the user if changes should be saved only when changes were made
            if (changesMade) {
                System.out.println("Confirm changes? (Y/N)");
                String saveChoice = scanner.nextLine().trim().toLowerCase();
                if (saveChoice.equals("y")) {
                    mainInstance.saveData();
                    System.out.println("Changes saved to file successfully.");
                }
            }
        }
    }

    private void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("partfile.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String partId = line.substring(0, 7).trim();
                int lengthOfDescription = 15 + line.substring(15).length();
                String description = line.substring(15, lengthOfDescription).trim();  // Adjust the substring length based on your file format
                bPlusTree.insertNode(partId, description);
            }
        } catch (IOException e) {
            System.out.println("File not fount.");
        }
        System.out.println("File uploaded.");
    }

    private void saveData() {
        try {
            PrintWriter out = new PrintWriter("partfile.txt");
            for (Structure Structure : bPlusTree.getAllNode()) {
                String formattedLine = String.format("%-7s        %s", Structure.getId(), Structure.getDescription());
                out.println(formattedLine);
            }
            out.close();
            System.out.println("Data saved.");
        } catch (FileNotFoundException e) {
            System.out.println("Unable to save.");
        }
    }
}