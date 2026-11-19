/** MODEL: one fixture between two teams. */
public class Match implements Storable {
    private final Team home, away;
    private int homeGoals, awayGoals;
    private boolean played;

    public Match(Team home, Team away) {
        this.home = home;
        this.away = away;
    }

    public void setResult(int homeGoals, int awayGoals) {
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.played = true;
    }

    public boolean involves(Team t) { return home == t || away == t; }
    public Team getHome() { return home; }
    public Team getAway() { return away; }
    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }
    public boolean isPlayed() { return played; }
    public String getScoreText() { return played ? homeGoals + " - " + awayGoals : "vs"; }

    @Override
    public String toLine() {
        return "M|" + home.getName() + "|" + away.getName() + "|" + homeGoals + "|" + awayGoals + "|" + played;
    }
}
