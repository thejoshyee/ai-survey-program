import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class Main {
    // Array of political parties
    private static final String[] PARTIES = {"Democratic", "Republican", "Libertarian", "Green"};

    // List to store our survey questions
    private static List<Question> questions = new ArrayList<>();

    // Nested map to store party data
    private static Map<String, Map<String, Integer>> partyData = new HashMap<>();

    public static void main(String[] args) {
        // initialize questions and party size
        initializeQuestions();
        initializePartyData();

        Scanner scanner = new Scanner(System.in);
        Map<String, Integer> userResponses = new HashMap<>();

        // Present each question to the user
        for (Question question : questions) {
            boolean validInput = false;
            while (!validInput) {
                try {
                    System.out.println(question.getText());
                    for (int i = 0; i < question.getOptions().length; i++) {
                        System.out.println((i + 1) + ". " + question.getOptions()[i]);
                    }

                    System.out.print("Enter your choice (1-" + question.getOptions().length + "): ");
                    int response = scanner.nextInt();

                    if (response < 1 || response > question.getOptions().length) {
                        System.out.println("Invalid option. Please choose a number between 1 and " + question.getOptions().length);
                        continue;
                    }

                    userResponses.put(question.getId(), response - 1);
                    validInput = true;

                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                }
            }
        }

        // try to guess the party after each response
        String guessedParty = guessParty(userResponses);

        if (guessedParty != null) {
            System.out.println("Based on your responses, I guess you might be affiliated with the " + guessedParty + " party.");
        } else {
            System.out.println("Based on your responses, I couldn't make a confident guess about your party affiliation.");
        }

        // ask for actual party affiliation
        System.out.println("What is your actual party affiliation?");
        boolean validInput = false;
        int actualParty = -1;
        while (!validInput) {
            try {
                for (int i = 0; i < PARTIES.length; i++) {
                    System.out.println((i + 1) + ". " + PARTIES[i]);
                }
                System.out.print("Enter your choice (1-" + PARTIES.length + "): ");
                int input = scanner.nextInt();

                if (input < 1 || input > PARTIES.length) {
                    System.out.println("Invalid option. Please choose a number between 1 and " + PARTIES.length);
                    continue;
                }

                actualParty = input - 1;
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }

        // After updating party data
        updatePartyData(PARTIES[actualParty], userResponses);

        // Save the updated data
        savePartyData();

        System.out.println("Thank you for participating in the survey!");
    }

    // Method to initialize questions
    private static void initializeQuestions() {
        // add questions to the list
        questions.add(new Question("Q1", "What should the government do to help the poor?",
                new String[]{"Make it easier to apply for assistance",
                        "Allow parents to use education funds for charter schools",
                        "Create welfare to work programs",
                        "Nothing"}));

        questions.add(new Question("Q2", "What is your stance on gun control?",
                new String[]{"Stricter gun laws are needed",
                        "Current laws are sufficient",
                        "Some gun laws should be repealed",
                        "The Second Amendment should not be restricted at all"}));

        questions.add(new Question("Q3", "What is your view on healthcare?",
                new String[]{"The government should provide universal healthcare",
                        "A mix of private and public options should be available",
                        "Healthcare should be privatized",
                        "The current system works well"}));

        questions.add(new Question("Q4", "What is your stance on climate change?",
                new String[]{"It's a critical threat requiring immediate action",
                        "It's a concern but economic growth is more important",
                        "It's exaggerated and not a significant threat",
                        "It's not real or not caused by human activity"}));

        questions.add(new Question("Q5", "What is your view on taxation?",
                new String[]{"Increase taxes on the wealthy to fund social programs",
                        "Keep tax rates roughly where they are",
                        "Lower taxes across the board",
                        "Implement a flat tax rate for all"}));

        questions.add(new Question("Q6", "What is your stance on immigration?",
                new String[]{"Create a path to citizenship for undocumented immigrants",
                        "Allow more skilled workers to immigrate legally",
                        "Reduce overall immigration levels",
                        "Strictly enforce current immigration laws"}));

        questions.add(new Question("Q7", "What is your view on abortion?",
                new String[]{"Should be legal in all or most cases",
                        "Should be legal only in certain cases",
                        "Should be illegal except in rare cases",
                        "Should be illegal in all cases"}));

        questions.add(new Question("Q8", "What is your stance on minimum wage?",
                new String[]{"Significantly increase the federal minimum wage",
                        "Slightly increase the minimum wage",
                        "Keep the minimum wage where it is",
                        "Abolish the minimum wage"}));

        questions.add(new Question("Q9", "What is your view on the role of government?",
                new String[]{"Government should do more to solve problems",
                        "Government is doing too many things better left to businesses and individuals",
                        "Government should only handle essential functions like national defense",
                        "The less government, the better"}));

        questions.add(new Question("Q10", "What is your stance on education policy?",
                new String[]{"Increase funding for public schools and make college free",
                        "Focus on improving the current public education system",
                        "Promote school choice and charter schools",
                        "Privatize education and use a voucher system"}));


    }

    // Method to initialize party data
    private static void initializePartyData() {
        Map<String, Map<String, Integer>> loadedData = loadPartyData();
        if (loadedData != null) {
            partyData = loadedData;
            return;
        }

        // If no saved data, initialize all values to 0
        for (String party : PARTIES) {
            Map<String, Integer> partyQuestions = new HashMap<>();
            for (Question question : questions) {
                // Initialize with 0 (neutral stance)
                partyQuestions.put(question.getId(), 0);
            }
            partyData.put(party, partyQuestions);
        }

        // Save the initialized data
        savePartyData();
    }

    // Load party data from JSON file
    private static Map<String, Map<String, Integer>> loadPartyData() {
        // Check if file exists
        File file = new File("party_data.json");
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            // Define the Map type for Gson deserialization
            Type type = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
            Map<String, Map<String, Integer>> loadedData = new Gson().fromJson(reader, type);

            // Validate loaded data
            if (loadedData == null || loadedData.isEmpty()) {
                return null;
            }

            return loadedData;
        } catch (IOException e) {
            return null;
        }
    }

    // Save party data to JSON file
    private static void savePartyData() {
        try (Writer writer = new FileWriter("party_data.json")) {
            // Create Gson instance with pretty printing
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(partyData, writer);
            System.out.println("Party data saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save party data: " + e.getMessage());
        }
    }

    // Guess user's party affiliation based on their responses
    private static String guessParty(Map<String, Integer> userResponses) {
        // Initialize scores for each party
        Map<String, Double> partyScores = new HashMap<>();
        for (String party : PARTIES) {
            partyScores.put(party, 0.0);
        }

        // Calculate scores based on user responses
        for (Map.Entry<String, Integer> entry : userResponses.entrySet()) {
            String questionId = entry.getKey();
            int userResponse = entry.getValue();

            for (String party : PARTIES) {
                Map<String, Integer> partyQuestionData = partyData.get(party);
                if (partyQuestionData.containsKey(questionId)) {
                    int partyResponse = partyQuestionData.get(questionId);
                    double score = 1.0 - (Math.abs(userResponse - partyResponse) / 3.0);
                    partyScores.put(party, partyScores.get(party) + score);
                }
            }
        }

        // Sort parties by score in descending order
        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(partyScores.entrySet());
        sortedScores.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        // Find parties with the highest score (within a small threshold)
        double maxScore = sortedScores.get(0).getValue();
        List<String> topParties = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedScores) {
            if (Math.abs(entry.getValue() - maxScore) < 0.01) {
                topParties.add(entry.getKey());
            } else {
                break;
            }
        }

        // Return result based on number of top parties
        if (topParties.size() > 1) {
            return "Leaning towards " + String.join(" and ", topParties);
        } else {
            return topParties.get(0);
        }
    }

    // method to update party data
    private static void updatePartyData(String party, Map<String, Integer> userResponses) {
        Map<String, Integer> partyQuestionData = partyData.get(party);
        if (partyQuestionData == null) {
            partyQuestionData = new HashMap<>();
            partyData.put(party, partyQuestionData);
        }
        for (Map.Entry<String, Integer> entry : userResponses.entrySet()) {
            String questionId = entry.getKey();
            int responseIndex = entry.getValue();

            // averaging update
            int currentValue = partyQuestionData.getOrDefault(questionId, responseIndex);
            int newValue = (currentValue + responseIndex) / 2;
            partyQuestionData.put(questionId, newValue);
        }

    }

}

class Question {
    private String id;
    private String text;
    private String[] options;

    public Question(String id, String text, String[] options) {
        this.id = id;
        this.text = text;
        this.options = options;
    }

    public String getId() { return id;}
    public String getText() { return text; }
    public String[] getOptions() { return options; }
}