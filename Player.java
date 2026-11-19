/** ABSTRACT BASE CLASS for every player. Subclasses decide their own position. */
public abstract class Player implements Storable {
    private final String teamName, name;
    private final int jersey;
    private int goals;

    protected Player(String teamName, String name, int jersey, int goals) {
        this.teamName = teamName;
        this.name = name;
        this.jersey = jersey;
        this.goals = goals;
    }

    /** Each subclass must provide its own position (polymorphism). */
    public abstract String getPosition();

    public String describe() {
        return "#" + jersey + " " + name + " (" + getPosition() + ")";
    }

    public void addGoal() { goals++; }
    public String getName() { return name; }
    public String getTeamName() { return teamName; }
    public int getJersey() { return jersey; }
    public int getGoals() { return goals; }

    @Override
    public String toLine() {
        return String.join("|", "P", getPosition(), teamName, name.replace("|", "/"),
                String.valueOf(jersey), String.valueOf(goals));
    }

    @Override
    public String toString() { return describe(); }

    /** FACTORY METHOD: creates the correct subclass from a position name. */
    public static Player create(String position, String team, String name, int jersey, int goals) {
        switch (position) {
            case "Striker":    return new Striker(team, name, jersey, goals);
            case "Midfielder": return new Midfielder(team, name, jersey, goals);
            case "Defender":   return new Defender(team, name, jersey, goals);
            case "Goalkeeper": return new Goalkeeper(team, name, jersey, goals);
            default: throw new TournamentException("Unknown position: " + position);
        }
    }
}
