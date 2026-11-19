/** CUSTOM EXCEPTION: thrown when a tournament rule is broken (shown to the user in an Alert). */
public class TournamentException extends RuntimeException {
    public TournamentException(String message) {
        super(message);
    }
}
