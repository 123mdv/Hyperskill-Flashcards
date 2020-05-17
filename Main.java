package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        var flashCardDeck = new FlashCardDeck();
        Scanner scanner = new Scanner(System.in);
        boolean exitFlag = false;
        String s; // used for message texts in console and log
        String exportFile = "";

        // interpret command line args
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-import":
                    flashCardDeck.importCardsFromFile(args[i + 1]);
                    break;
                case "-export":
                    exportFile = args[i + 1];
                    break;
                default:
                    // do nothing
            }
        }

        // main menu
        while (!exitFlag) {
            s = "\nInput the action (add, remove, import, " +
                    "export, ask, exit, log, hardest card, reset stats):";
            System.out.println(s);
            flashCardDeck.writeToLog(s);
            String action = scanner.nextLine();
            flashCardDeck.writeToLog(action);
            switch (action) {
                case "add":
                    flashCardDeck.addCardFromConsole();
                    break;
                case "remove":
                    flashCardDeck.removeCardFromConsole();
                    break;
                case "import":
                    flashCardDeck.importCardsFromFileFromConsole();
                    break;
                case "export":
                    flashCardDeck.exportCardsToFileFromConsole();
                    break;
                case "ask":
                    flashCardDeck.askSomeCards();
                    break;
                case "exit":
                    System.out.println("Bye bye!");
                    if (!"".equals(exportFile)) {
                        flashCardDeck.exportCardsToFile(exportFile);
                    }
                    exitFlag = true;
                    break;
                case "log":
                    flashCardDeck.saveLogToFile();
                    break;
                case "hardest card":
                    flashCardDeck.printHardestCard();
                    break;
                case "reset stats":
                    flashCardDeck.resetErrorStats();
                    break;
                default:
                    s = "Invalid option. Try again...";
                    System.out.println(s);
                    flashCardDeck.writeToLog(s);
            }
        }
    }
}

class FlashCardDeck {
    private LinkedHashMap<FlashCard, Integer> map = new LinkedHashMap<FlashCard, Integer>();
    private ArrayList<String> sessionLog = new ArrayList<String>();
    private Scanner scanner = new Scanner(System.in);

    public void addCardFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String term = "";
        String s = "The card:";
//        while ("".equals(term)) {
            System.out.println(s);
            writeToLog(s);
            term = scanner.nextLine();
            writeToLog(term);
//        }
        if (containsTerm(term)) {
            s = "The card \"" + term + "\" already exists.";
            System.out.println(s);
            writeToLog(s);
            return;
        }
        s = "The definition of the card:";
        System.out.println(s);
        writeToLog(s);
        String definition = scanner.nextLine();
        writeToLog(definition);
        if (containsDefinition(definition)) {
            s = "The definition \"" + definition + "\" already exists.";
            System.out.println(s);
            writeToLog(s);
            return;
        }
        FlashCard flashCard = new FlashCard(term, definition);
        map.put(flashCard, 0);
        s = "The pair (\"" + term + "\":\"" + definition + "\") has been added.";
        System.out.println(s);
        writeToLog(s);
    }

    public void removeCardFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String s = "The card:";
        System.out.println(s);
        writeToLog(s);
        String term = scanner.nextLine();
        writeToLog(term);
        if (containsTerm(term)) {
            for (FlashCard f : map.keySet()) {
                if (term.equals(f.getTerm())) {
                    map.remove(f);
                    s = "The card has been removed.";
                    System.out.println(s);
                    writeToLog(s);
                    return;
                }
            }
        } else {
            s = "Can't remove \"" + term + "\": there is no such card.";
            System.out.println(s);
            writeToLog(s);
            return;
        }
    }

    public void importCardsFromFileFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String s = "File name:";
        System.out.println(s);
        writeToLog(s);
        s = scanner.nextLine();
        writeToLog(s);
        importCardsFromFile(s);
    }

    public void importCardsFromFile(String fileName) {
        File file = new File(fileName);
        String term, definition, errors, s;
        int counter = 0;
        try (Scanner scanner2 = new Scanner(file)) {
            while (scanner2.hasNext()) {
                term = scanner2.nextLine();
                definition = scanner2.nextLine();
                errors = scanner2.nextLine();
                for (FlashCard f : map.keySet()) {
                    if (term.equals(f.getTerm())) {
                        map.remove(f);
                        break;
                    }
                }
                FlashCard flashCard = new FlashCard(term, definition);
                map.put(flashCard, Integer.parseInt(errors));
                counter++;
            }
            s = counter + " cards have been loaded.";
            System.out.println(s);
            writeToLog(s);
        } catch (FileNotFoundException e) {
            s = "File not found.";
            System.out.println(s);
            writeToLog(s);
        }
    }

    public void exportCardsToFileFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String s = "File name:";
        System.out.println(s);
        writeToLog(s);
        s = scanner.nextLine();
        writeToLog(s);
        exportCardsToFile(s);
    }

    public void exportCardsToFile(String s) {
        File file = new File(s);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (FlashCard flashCard : map.keySet()) {
                printWriter.println(flashCard.getTerm());
                printWriter.println(flashCard.getDefinition());
                printWriter.println(map.get(flashCard));
            }
            s = map.size() + " cards have been saved.";
            System.out.println(s);
            writeToLog(s);
        } catch (IOException e) {
            s = "An exception occurred: " + e.getMessage();
            System.out.println(s);
            writeToLog(s);
        }
    }

    public void askSomeCards() {
        Scanner scanner = new Scanner(System.in);
        String s = "How many times to ask?";
        System.out.println(s);
        writeToLog(s);
        int n = scanner.nextInt();
        writeToLog("" + n);
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int randomNumber = random.nextInt(map.size());
            int mapIndex = 0;
            for (FlashCard flashCard : map.keySet()) {
                if (mapIndex == randomNumber) {
                    s = "Print the definition of \"" + flashCard.getTerm() + "\":";
                    System.out.println(s);
                    writeToLog(s);
                    Scanner scanner2 = new Scanner(System.in);
                    String userAnswer = scanner2.nextLine();
                    writeToLog(userAnswer);
                    if (userAnswer.equals(flashCard.getDefinition())) {
                        s = "Correct answer.";
                        System.out.println(s);
                        writeToLog(s);
                    } else if (containsDefinition(userAnswer)) {
                        s = "Wrong answer. The correct one is \"" + getDefinition(flashCard.getTerm()) +
                                "\", you've just written the definition of \"" + getTerm(userAnswer) + "\".";
                        System.out.println(s);
                        writeToLog(s);
                        setErrors(flashCard, getErrors(flashCard) + 1);
                    } else {
                        s = "Wrong answer. The correct one is \"" + getDefinition(flashCard.getTerm()) + "\".";
                        System.out.println(s);
                        writeToLog(s);
                        setErrors(flashCard, getErrors(flashCard) + 1);
                    }
                    break;
                } else {
                    mapIndex++;
                }
            }
        }
    }

    public void saveLogToFile() {
        Scanner scanner = new Scanner(System.in);
        String s = "File name:";
        System.out.println(s);
        writeToLog(s);
        s = scanner.nextLine();
        writeToLog(s);
        File file = new File(s);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println("SESSION LOG");
            printWriter.println("-----------");
            for (String logEntry : sessionLog) {
                printWriter.println(logEntry);
            }
            printWriter.println("\n----------");
            printWriter.println("END OF LOG");
            printWriter.close();
            s = "The log has been saved.";
            System.out.println(s);
            writeToLog(s);
        } catch (IOException e) {
            s = "An exception occurred: " + e.getMessage();
            System.out.println(s);
            writeToLog(s);
        }
    }

    public void printHardestCard() {
        // find the hardest card, or cards
        ArrayList<String> hardestCards = new ArrayList<String>();
        int max = 1;
        for (FlashCard f : map.keySet()) {
            Integer v = map.get(f);
            if (v > max) {
                max = v;
                hardestCards.clear();
                hardestCards.add(f.getTerm());
            } else if (v == max) {
                hardestCards.add(f.getTerm());
            }
        }
        // format and print the output
        switch (hardestCards.size()) {
            case 0:
                String s = "There are no cards with errors.";
                System.out.println(s);
                writeToLog(s);
                break;
            case 1:
                s = "The hardest card is \"" + hardestCards.get(0) + "\". You have " + max + " errors answering it.";
                System.out.println(s);
                writeToLog(s);
                break;
            default:
                String cards ="";
                for (int i = 0; i < hardestCards.size(); i++) {
                    if (i < hardestCards.size() - 1) {
                        cards += "\"" + hardestCards.get(i) + "\", ";
                    } else {
                        cards += "\"" + hardestCards.get(i) + "\". ";
                    }
                }
                s = "The hardest cards are " + cards + "You have " + max + " errors answering them.";
                System.out.println(s);
                writeToLog(s);
                }
        }

    public void resetErrorStats() {
        for (FlashCard f : map.keySet()) {
            map.put(f, 0);
        }
        String s = "Card statistics has been reset.";
        System.out.println(s);
        writeToLog(s);
    }

    public void writeToLog(String s) {
        sessionLog.add(s);
    }

    public boolean containsTerm(String term) {
        for (FlashCard flashCard : map.keySet()) {
            if (term.equals(flashCard.getTerm())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsDefinition(String definition) {
        for (FlashCard flashCard : map.keySet()) {
            if (definition.equals(flashCard.getDefinition())) {
                return true;
            }
        }
        return false;
    }

    public String getDefinition(String term) {
        for (FlashCard flashCard : map.keySet()) {
            if (term.equals(flashCard.getTerm())) {
                return flashCard.getDefinition();
            }
        }
        return "Internal error - Term not found!";
    }

    public String getTerm(String definition) {
        for (FlashCard flashCard : map.keySet()) {
            if (definition.equals(flashCard.getDefinition())) {
                return flashCard.getTerm();
            }
        }
        return "Internal error - Definition not found!";
    }

    public Integer getErrors(FlashCard f) {
        return map.get(f);
    }

    public Integer setErrors(FlashCard f, Integer i) {
        return map.put(f, i);
    }
}


class FlashCard {
    private String term;
    private String definition;

    public FlashCard(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }
}

# test