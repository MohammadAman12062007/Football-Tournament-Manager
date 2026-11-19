import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** BACKEND: all tournament rules, calculations and file storage (tournament.txt). */
public class TournamentService {
    private static final Path FILE = Paths.get("tournament.txt");
    private static final int MAX_SQUAD = 15;
    private final List<Team> teams = new ArrayList<>();
    private final List<Match> matches = new ArrayList<>();

    public TournamentService() {
        load();
        if (teams.isEmpty()) seed();
        recalc();
    }

    public List<Team> getTeams() { return new ArrayList<>(teams); }
    public List<Match> getMatches() { return new ArrayList<>(matches); }

    // ---------- Teams and players ----------
    public void addTeam(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) throw new TournamentException("Team name cannot be empty.");
        if (name.length() > 25) throw new TournamentException("Team name can have at most 25 characters.");
        if (name.contains("|")) throw new TournamentException("Team name cannot contain the | character.");
        if (teams.stream().anyMatch(t -> t.getName().equalsIgnoreCase(name)))
            throw new TournamentException("A team named \"" + name + "\" already exists.");
        teams.add(new Team(name));
        save();
    }

    public void removeTeam(Team t) {
        teams.remove(t);
        matches.removeIf(m -> m.involves(t));
        recalc();
        save();
    }

    public void addPlayer(Team t, String position, String rawName, int jersey) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) throw new TournamentException("Player name cannot be empty.");
        if (jersey < 1 || jersey > 99) throw new TournamentException("Jersey number must be between 1 and 99.");
        if (t.getPlayers().size() >= MAX_SQUAD)
            throw new TournamentException("Squad is full (maximum " + MAX_SQUAD + " players).");
        if (t.getPlayers().stream().anyMatch(p -> p.getJersey() == jersey))
            throw new TournamentException("Jersey number " + jersey + " is already taken in " + t.getName() + ".");
        t.addPlayer(Player.create(position, t.getName(), name, jersey, 0));
        save();
    }

    public void removePlayer(Team t, Player p) {
        t.removePlayer(p);
        save();
    }

    public void addGoal(Player p) {
        p.addGoal();
        save();
    }

    // ---------- Fixtures and results ----------
    /** Round robin: every team plays every other team once. Replaces any existing fixtures. */
    public void generateFixtures() {
        if (teams.size() < 2) throw new TournamentException("Add at least 2 teams to generate fixtures.");
        matches.clear();
        for (int i = 0; i < teams.size(); i++)
            for (int j = i + 1; j < teams.size(); j++)
                matches.add(new Match(teams.get(i), teams.get(j)));
        recalc();
        save();
    }

    public void recordResult(Match m, int homeGoals, int awayGoals) {
        if (homeGoals < 0 || awayGoals < 0) throw new TournamentException("Goals cannot be negative.");
        m.setResult(homeGoals, awayGoals);
        recalc();
        save();
    }

    /** Standings are always rebuilt from the played matches, so they can never go out of sync. */
    private void recalc() {
        teams.forEach(Team::resetStats);
        for (Match m : matches) {
            if (!m.isPlayed()) continue;
            m.getHome().record(m.getHomeGoals(), m.getAwayGoals());
            m.getAway().record(m.getAwayGoals(), m.getHomeGoals());
        }
    }

    // ---------- Queries (Streams) ----------
    public List<Team> getStandings() {
        return teams.stream()
                .sorted(Comparator.comparingInt(Team::getPoints)
                        .thenComparingInt(Team::getGoalDiff)
                        .thenComparingInt(Team::getGoalsFor)
                        .reversed()
                        .thenComparing(Team::getName))
                .collect(Collectors.toList());
    }

    public List<Player> getTopScorers(int limit) {
        return teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .sorted(Comparator.comparingInt(Player::getGoals).reversed().thenComparing(Player::getName))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void reset() {
        teams.clear();
        matches.clear();
        seed();
        recalc();
    }

    // ---------- File storage ----------
    private Team findTeam(String name) {
        return teams.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow(() -> new TournamentException("Unknown team: " + name));
    }

    private void load() {
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE)) {
                if (line.isBlank()) continue;
                try {
                    String[] f = line.split("\\|", -1);
                    switch (f[0]) {
                        case "T":
                            teams.add(new Team(f[1]));
                            break;
                        case "P":
                            findTeam(f[2]).addPlayer(Player.create(f[1], f[2], f[3],
                                    Integer.parseInt(f[4]), Integer.parseInt(f[5])));
                            break;
                        case "M":
                            Match m = new Match(findTeam(f[1]), findTeam(f[2]));
                            if (Boolean.parseBoolean(f[5]))
                                m.setResult(Integer.parseInt(f[3]), Integer.parseInt(f[4]));
                            matches.add(m);
                            break;
                        default:
                            break;
                    }
                } catch (RuntimeException ex) {
                    System.err.println("Skipping bad line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Team t : teams) lines.add(t.toLine());
        for (Team t : teams) for (Player p : t.getPlayers()) lines.add(p.toLine());
        for (Match m : matches) lines.add(m.toLine());
        try {
            Files.write(FILE, lines);
        } catch (IOException e) {
            System.err.println("Could not save file: " + e.getMessage());
        }
    }

    private void seed() {
        String[] names = {"Mangalore FC", "Bengaluru United", "Mysore Royals", "Udupi Strikers"};
        String[][] squads = {
                {"Arjun Shetty", "Rohan Rai", "Naveen Poojary"},
                {"Vikram Rao", "Sameer Khan", "Pranav Gowda"},
                {"Karthik Iyer", "Dev Nair", "Manoj Kumar"},
                {"Aditya Bhat", "Faisal Ahmed", "Sachin Hegde"}};
        for (int i = 0; i < names.length; i++) {
            Team t = new Team(names[i]);
            t.addPlayer(Player.create("Striker", names[i], squads[i][0], 9, 0));
            t.addPlayer(Player.create("Midfielder", names[i], squads[i][1], 8, 0));
            t.addPlayer(Player.create("Goalkeeper", names[i], squads[i][2], 1, 0));
            teams.add(t);
        }
        save();
    }
}
