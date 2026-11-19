import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** MODEL: a team, its squad and its league statistics (stats are recalculated from match results). */
public class Team implements Storable {
    private final String name;
    private final List<Player> players = new ArrayList<>();
    private int played, won, drawn, lost, goalsFor, goalsAgainst;

    public Team(String name) { this.name = name; }

    public void addPlayer(Player p) { players.add(p); }
    public void removePlayer(Player p) { players.remove(p); }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }

    public void resetStats() { played = won = drawn = lost = goalsFor = goalsAgainst = 0; }

    public void record(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) won++;
        else if (scored == conceded) drawn++;
        else lost++;
    }

    public String getName() { return name; }
    public int getPlayed() { return played; }
    public int getWon() { return won; }
    public int getDrawn() { return drawn; }
    public int getLost() { return lost; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalDiff() { return goalsFor - goalsAgainst; }
    public int getPoints() { return 3 * won + drawn; }

    @Override public String toLine() { return "T|" + name; }
    @Override public String toString() { return name; }
}
