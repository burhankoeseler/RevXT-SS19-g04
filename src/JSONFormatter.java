/*
  Author: Daniel Kucko, OTH Regensburg
  This is an utility class to use with the automated testing functionality for ReversiXT clients.
  Note that this implementation is very rudimentary as it does not use any external libraries but rather constructs
  the JSON strings via StringBuilders and String formatting. The use of external libraries to "optimize" this code
  is extremely discouraged and should be talked about beforehand with Professor Kern.
  ---
  Please do not attempt to change this code. The test log parser requires a very specific formatting of the output,
  which this class provides. Any Changes made to this class, resulting in failed test results, are not the fault of the
  original author. If the parameter types are conflicting with your internal storage, you can adjust the methods,
  provided that the output stays the same, as specified in the method comments. Please note that JSON does not care
  about line breaks.
  ---
  Usage:
  The methods of this class are all static. You do not have to create an object of the class in order to use them.
  Whenever needed (as explained in the methods) call the appropriate method to store certain information about the
  current action (also explained in the methods). Before shutting down your client, call the buildFinalString() method
  and print the resulting string using your default loggMode method.
  !ATTENTION!
  Since this class will hold all information in memory until they are printed before shutting down your client,
  you should only use this class and its' special loggMode when explicitly asked to (if the client is started
  with the -t/--test argument)
  !ATTENTION!
 */

import java.util.ArrayList;
import java.util.Arrays;

public class JSONFormatter {

    private static String initialMap;
    private static ArrayList<String> moveRequests = new ArrayList<>();
    private static ArrayList<String> bombMoveRequests = new ArrayList<>();
    private static ArrayList<String> moves = new ArrayList<>();
    private static ArrayList<String> bombMoves = new ArrayList<>();
    private static ArrayList<String> ServerMessages = new ArrayList<>();

    /**
     * Call this method whenever you received a move request in the first phase,
     * but only AFTER calculating the valid moves.
     * This method will produce a string of the form
     * "{
     *     "Overrides": <Number of overrides>,
     *     "Player": "<Your player number (colour)>",
     *     "Board": [[<First line of the map>], ...],
     *     "ValidMoves": [<List of valid moves>]
     * }" and append it to the class level list moveRequests.
     * @param board: 2d array of type char, where every char represents a single field of the board.
     * @param overrides: Number of overrides you have at the moment of receiving the move request.
     * @param player: single char containing your "colour" (number from 1-8).
     * @param validMoves: Array of type String containing the valid moves you calculated,
     *                  where every valid move is of the form "x y"
     */
    //In CLient case 5 phase 1
    public static void addMoveRequest(char[][] board, int overrides, char player, String[] validMoves) {
        String moveRequest = String.format("{" +
                "\"Overrides\": %d," +
                "\"Player\": \"%s\"," +
                "\"Board\": %s," +
                "\"ValidMoves\": %s" +
                "}", overrides, player, build2dArrayString(board), build1dArrayString(validMoves));
        moveRequests.add(moveRequest);
    }

    /**
     * Call this method whenever you received a move request in the second phase,
     * but only AFTER calculating the valid moves.
     * This method will produce a string of the form
     * "{
     *     "Player": "<Your player number (colour)>",
     *     "Board": [[<First line of the map>], ...],
     *     "ValidMoves": [<List of valid moves>]
     * }" and append it to the class level list bombMoveRequests.
     * @param board: 2d array of type char, where every char represents a single field of the board.
     * @param player: single char containing your "colour" (number from 1-8).
     * @param validMoves: Array of type String containing the valid moves you calculated,
     *                  where every valid move is of the form "x y"
     */
    //In client case 5 phase 2
    public static void addBombMoveRequest(char[][] board, char player, String[] validMoves) {
        String moveRequest = String.format("{" +
                "\"Player\": \"%s\"," +
                "\"Board\": %s," +
                "\"ValidMoves\": %s" +
                "}", player, build2dArrayString(board), build1dArrayString(validMoves));
        bombMoveRequests.add(moveRequest);
    }

    /**
     * Call this method whenever you received a move in the first phase,
     * but only AFTER calculating the new map.
     * This method will produce a string of the form
     * "{
     *     "Overrides": [<Number of overrides for player one>, ...],
     *     "Bombs": [<Number of bombs for player one>, ...]
     *     "Player": "<The colour of the player who made the move>",
     *     "Board": [[<First line of the map>], ...],
     *     "ReceivedMove": "<The move received from the server>",
     * }" and append it to the class level list moves.
     * @param board: 2d array of type char, where every char represents a single field of the board. Note that
     *             you need to pass the NEW board.
     * @param overrides: Array of overrides per player after executing the received move.
     * @param player: single char containing your "colour" (number from 1-8).
     * @param bombs: Array of bombs per player after executing the received move.
     * @param receivedMove: The move received from the server. Pass as String of the form "x y bonus".
     */
    //In Client case 6 phase 1
    public static void addMove(String receivedMove, char player, char[][] board, int[] overrides, int[] bombs) {
        String move = String.format("{" +
                "\"ReceivedMove\": \"%s\"," +
                "\"Player\": \"%s\"," +
                "\"Board\": %s," +
                "\"Overrides\": %s," +
                "\"Bombs\": %s" +
                "}", receivedMove, player, build2dArrayString(board), Arrays.toString(overrides), Arrays.toString(bombs));
        moves.add(move);
    }

    /**
     * Call this method whenever you received a move in the second phase,
     * but only AFTER calculating the new map.
     * This method will produce a string of the form
     * "{
     *     "Bombs": [<Number of bombs for player one>, ...]
     *     "Player": "<The colour of the player who made the move>",
     *     "Board": [[<First line of the map>], ...],
     *     "ReceivedMove": "<The move received from the server>",
     * }" and append it to the class level list bombMoves
     * @param board: 2d array of type char, where every char represents a single field of the board. Note that
     *             you need to pass the NEW board.
     * @param player: single char containing your "colour" (number from 1-8).
     * @param bombs: Array of bombs per player after executing the received move.
     * @param receivedMove: The move received from the server. Pass as String of the form "x y bonus".
     */
    //In Client case 6 phase 2
    public static void addBombMove(String receivedMove, char player, char[][] board, int[] bombs) {
        String move = String.format("{" +
                "\"ReceivedMove\": \"%s\"," +
                "\"Player\": \"%s\"," +
                "\"Board\": %s," +
                "\"Bombs\": %s" +
                "}", receivedMove, player, build2dArrayString(board), Arrays.toString(bombs));
        bombMoves.add(move);
    }

    /**
     * Call this method ONCE after receiving the initial Map specification from the server.
     * This method will produce a string of the form
     * "{
     *      "Players": <Number of players on this map>,
     *      "Overrides": <Number of overrides each player has at the start of the game>,
     *      "Bombs": <Number of bombs each player has at the start of the game>,
     *      "BombStrength": <The strength of the bombs>,
     *      "Height": <Number of lines of the map>,
     *      "Width": <Number of columns of the map>,
     *      "Board": [[<First line of the map>], ...],
     *      "Transitions": [{"origin": {"x": <x>, "y": <y>, "direction": <direction>}, "destination": {...} ...]
     * }" and store it in the class level variable initialMap.
     * @param players: number of players.
     * @param overrides: number of overrides each player gets.
     * @param bombs: number of bombs each player gets.
     * @param bombStrength: the strength of the bombs.
     * @param height: the number of lines of the map.
     * @param width: the number of columns of the map.
     * @param board: 2d array of type char, where every char represent a single field of the board.
     * @param transitions: array of type String, where every transition is a String of the form
     *                   "x y direction <-> x y direction"
     */
    //In Spielfeld
    public static void addInitialMap(int players, int overrides, int bombs, int bombStrength, int height, int width, char[][] board, String[] transitions) {
        String initialMapString = String.format("{" +
                "\"Players\": %d," +
                "\"Overrides\": %d," +
                "\"Bombs\": %d," +
                "\"BombStrength\": %d," +
                "\"Height\": %d," +
                "\"Width\": %d," +
                "\"Board\": %s," +
                "\"Transitions\": %s" +
                "}", players, overrides, bombs, bombStrength, height, width, build2dArrayString(board), buildTransitionArray(transitions));
        initialMap = initialMapString;
    }

    /**
     * This method is not needed to be called at this moment.
     * @param message: message received from the server.
     */
    public static void addServerMessage(String message) {
        ServerMessages.add(message);
    }

    /**
     * Call this method only at the end of the match / before shutting down your client.
     * This method will produce the final String, which you need to output using your default loggMode tool.
     * The final string will be of the form:
     * "//JSON Start
     * {
     *      "InitialMap": <Result of addInitialMap>,
     *      "MoveRequests": [<First recorded moveRequest>, ...],
     *      "BombMoveRequests": [<First recorded bombMoveRequest>, ...],
     *      "Moves": [<First recorded Move>, ...],
     *      "BombMoves": [<First recorded bombMove>, ...],
     *      "ServerMessages": [<Probably an empty array>]
     * }
     * //JSON End"
     * @return The final JSON Object as a String, delimited by two line comments JSON Start and JSON End.
     */
    //In Client case 9;
    public static String buildFinalString() {
        StringBuilder finalString = new StringBuilder();
        finalString.append("//JSON Start\n");
        finalString.append('{');
        finalString.append(String.format("\"InitialMap\": %s,", initialMap));
        finalString.append("\"MoveRequests\": [");
        for (int i = 0; i < moveRequests.size(); i++) {
            finalString.append(moveRequests.get(i));
            if (!(i == moveRequests.size() - 1)) {
                finalString.append(',');
            }
        }
        finalString.append("],");
        finalString.append("\"BombMoveRequests\": [");
        for (int i = 0; i < bombMoveRequests.size(); i++) {
            finalString.append(bombMoveRequests.get(i));
            if (!(i == bombMoveRequests.size() - 1)) {
                finalString.append(',');
            }
        }
        finalString.append("],");
        finalString.append("\"Moves\": [");
        for (int i = 0; i < moves.size(); i++) {
            finalString.append(moves.get(i));
            if (!(i == moves.size() - 1)) {
                finalString.append(',');
            }
        }
        finalString.append("],");
        finalString.append("\"BombMoves\": [");
        for (int i = 0; i < bombMoves.size(); i++) {
            finalString.append(bombMoves.get(i));
            if (!(i == bombMoves.size() - 1)) {
                finalString.append(',');
            }
        }
        finalString.append("],");
        finalString.append("\"ServerMessages\": [");
        for (int i = 0; i < ServerMessages.size(); i++) {
            finalString.append(ServerMessages.get(i));
            if (!(i == ServerMessages.size() - 1)) {
                finalString.append(',');
            }
        }
        finalString.append("]");
        finalString.append('}');
        finalString.append("\n//JSON End");
        return finalString.toString();
    }

    /**
     * Helper method, that will produce a JSON conform String representation of an array. Only used for the boards.
     * @param array: the array to transform.
     * @return String representation of an 2d array in correct JSON form.
     */
    private static String build2dArrayString(char[][] array) {
        StringBuilder arrayString = new StringBuilder();
        arrayString.append('[');
        for (int y = 0; y < array.length; y++) {
            arrayString.append('[');
            for (int x = 0; x < array[y].length; x++) {
                arrayString.append(String.format("\"%s\"", array[y][x]));
                if (!(x == array[y].length - 1)) {
                    arrayString.append(',');
                }
            }
            arrayString.append(']');
            if (!(y == array.length - 1)) {
                arrayString.append(',');
            }
        }
        arrayString.append(']');
        return arrayString.toString();
    }

    /**
     * Helper method, that will produce a JSON conform String representation of an array. Only used for the valid Moves.
     * @param array: the array to transform.
     * @return String representation of an 1d array in correct JSON form.
     */
    private static String build1dArrayString(String[] array) {
        StringBuilder arrayString = new StringBuilder();
        arrayString.append('[');
        for (int i = 0; i < array.length; i++) {
            arrayString.append(String.format("\"%s\"", array[i]));
            if (!(i == array.length - 1)) {
                arrayString.append(',');
            }
        }
        arrayString.append(']');
        return arrayString.toString();
    }

    /**
     * Helper method, that will produce a JSON conform String representation of an array. Only used for the transitions.
     * Transforms the transitions from the form "x y direction <-> x y direction" to the form:
     * {
     *     "origin": {"x": <x>, "y": <y>, "direction": <direction>},
     *     "destination": {"x": <x>, "y": <y>, "direction": <direction>}
     * }
     * @param transitions: the array to transform.
     * @return String representation of an 1d array in correct JSON form.
     */
    private static String buildTransitionArray(String[] transitions) {
        StringBuilder arrayString = new StringBuilder();
        arrayString.append('[');
        for (int i = 0; i < transitions.length; i++) {
            String[] parts = transitions[i].split(" <-> ");
            String[] origin = parts[0].split(" ");
            String[] destination = parts[1].split(" ");
            arrayString.append(String.format("{" +
                            "\"origin\": {\"x\": %d, \"y\": %d, \"direction\": %d}," +
                            "\"destination\": {\"x\": %d, \"y\": %d, \"direction\": %d}", Integer.parseInt(origin[0]), Integer.parseInt(origin[1]),
                    Integer.parseInt(origin[2]), Integer.parseInt(destination[0]), Integer.parseInt(destination[1]), Integer.parseInt(destination[2])));
            arrayString.append('}');
            if (!(i == transitions.length - 1)) {
                arrayString.append(',');
            }
        }
        arrayString.append(']');
        return arrayString.toString();
    }

}
