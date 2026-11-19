/** INHERITANCE: four kinds of player, each extending Player. */
class Striker extends Player {
    Striker(String team, String name, int jersey, int goals) { super(team, name, jersey, goals); }
    @Override public String getPosition() { return "Striker"; }
}

class Midfielder extends Player {
    Midfielder(String team, String name, int jersey, int goals) { super(team, name, jersey, goals); }
    @Override public String getPosition() { return "Midfielder"; }
}

class Defender extends Player {
    Defender(String team, String name, int jersey, int goals) { super(team, name, jersey, goals); }
    @Override public String getPosition() { return "Defender"; }
}

class Goalkeeper extends Player {
    Goalkeeper(String team, String name, int jersey, int goals) { super(team, name, jersey, goals); }
    @Override public String getPosition() { return "Goalkeeper"; }

    /** METHOD OVERRIDING: keepers get a custom description. */
    @Override public String describe() { return super.describe() + " [Keeper]"; }
}
