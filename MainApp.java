import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.function.Function;

/** FRONTEND: one window with four tabs (Teams, Fixtures, Standings, Top Scorers). */
public class MainApp extends Application {

    private final TournamentService service = new TournamentService();

    private final ListView<Team> teamList = new ListView<>();
    private final TableView<Player> playerTable = new TableView<>();
    private final TableView<Match> fixtureTable = new TableView<>();
    private final TableView<Team> standingsTable = new TableView<>();
    private final TableView<Player> scorerTable = new TableView<>();
    private final CategoryAxis chartX = new CategoryAxis();
    private final NumberAxis chartY = new NumberAxis();
    private final BarChart<String, Number> scorerChart = new BarChart<>(chartX, chartY);
    private final Label teamTitle = new Label("Select a team to see its squad");
    private final Label status = new Label("Welcome to the Football Tournament Manager");
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Label banner = new Label("FOOTBALL TOURNAMENT MANAGER");
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setStyle("-fx-background-color:#1b5e20;-fx-text-fill:white;"
                + "-fx-font-size:20px;-fx-font-weight:bold;-fx-padding:12;");

        TabPane tabs = new TabPane(teamsTab(), fixturesTab(), standingsTab(), scorersTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        HBox bar = new HBox(status);
        bar.setPadding(new Insets(5, 10, 5, 10));

        BorderPane root = new BorderPane(tabs);
        root.setTop(new VBox(menuBar(), banner));
        root.setBottom(bar);

        refreshAll();
        stage.setTitle("Football Tournament Manager");
        stage.setScene(new Scene(root, 1000, 650));
        stage.show();
    }

    private MenuBar menuBar() {
        MenuItem reset = new MenuItem("Reset Tournament");
        reset.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "This deletes all teams, fixtures and results and restores the sample data. Continue?");
            if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                service.reset();
                status.setText("Tournament reset");
                refreshAll();
            }
        });
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> stage.close());
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> alert(Alert.AlertType.INFORMATION, "About",
                "Football Tournament Manager v1.0\nA JavaFX mini project.\nData is saved in tournament.txt"));
        return new MenuBar(new Menu("File", null, reset, exit), new Menu("Help", null, about));
    }

    // ---------- Tab 1: Teams and players ----------
    private Tab teamsTab() {
        // left side: team list
        TextField teamField = new TextField();
        teamField.setPromptText("New team name");
        Button addTeam = new Button("Add Team");
        addTeam.setOnAction(e -> act(() -> {
            service.addTeam(teamField.getText());
            teamField.clear();
        }, "Team added"));
        Button delTeam = new Button("Delete Team");
        delTeam.setOnAction(e -> {
            Team t = teamList.getSelectionModel().getSelectedItem();
            if (t == null) { alert(Alert.AlertType.WARNING, "No team selected", "Select a team first."); return; }
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + t.getName() + " and all its matches?");
            if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                act(() -> service.removeTeam(t), "Deleted " + t.getName());
        });
        teamList.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> refreshPlayers());
        VBox left = new VBox(10, new Label("Teams"), teamList, teamField, new HBox(10, addTeam, delTeam));
        left.setPrefWidth(260);
        VBox.setVgrow(teamList, Priority.ALWAYS);

        // right side: squad
        teamTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        addCol(playerTable, "No.", p -> String.valueOf(p.getJersey()));
        addCol(playerTable, "Name", Player::getName);
        addCol(playerTable, "Position", Player::getPosition);
        addCol(playerTable, "Goals", p -> String.valueOf(p.getGoals()));
        playerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextField pName = new TextField();
        pName.setPromptText("Player name");
        Spinner<Integer> jersey = new Spinner<>(1, 99, 10);
        jersey.setPrefWidth(80);
        ComboBox<String> position = new ComboBox<>();
        position.getItems().addAll("Striker", "Midfielder", "Defender", "Goalkeeper");
        position.setValue("Striker");
        Button addPlayer = new Button("Add Player");
        addPlayer.setOnAction(e -> {
            Team t = teamList.getSelectionModel().getSelectedItem();
            if (t == null) { alert(Alert.AlertType.WARNING, "No team selected", "Select a team first."); return; }
            String name = pName.getText(), pos = position.getValue();
            int no = jersey.getValue();
            act(() -> {
                service.addPlayer(t, pos, name, no);
                pName.clear();
            }, "Player added to " + t.getName());
        });

        Button goal = new Button("+ Goal for Selected Player");
        goal.setOnAction(e -> {
            Player p = playerTable.getSelectionModel().getSelectedItem();
            if (p == null) { alert(Alert.AlertType.WARNING, "No player selected", "Select a player first."); return; }
            act(() -> service.addGoal(p), p.getName() + " scored!");
        });
        Button removePlayer = new Button("Remove Selected Player");
        removePlayer.setOnAction(e -> {
            Team t = teamList.getSelectionModel().getSelectedItem();
            Player p = playerTable.getSelectionModel().getSelectedItem();
            if (t == null || p == null) {
                alert(Alert.AlertType.WARNING, "No player selected", "Select a player first.");
                return;
            }
            act(() -> service.removePlayer(t, p), "Removed " + p.getName());
        });

        VBox right = new VBox(10, teamTitle, playerTable,
                new HBox(10, pName, new Label("No."), jersey, position, addPlayer),
                new HBox(10, goal, removePlayer));
        VBox.setVgrow(playerTable, Priority.ALWAYS);
        HBox.setHgrow(pName, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox box = new HBox(20, left, right);
        box.setPadding(new Insets(15));
        return new Tab("Teams & Players", box);
    }

    // ---------- Tab 2: Fixtures and results ----------
    private Tab fixturesTab() {
        addCol(fixtureTable, "#", m -> String.valueOf(fixtureTable.getItems().indexOf(m) + 1));
        addCol(fixtureTable, "Home", m -> m.getHome().getName());
        addCol(fixtureTable, "Score", Match::getScoreText);
        addCol(fixtureTable, "Away", m -> m.getAway().getName());
        addCol(fixtureTable, "Status", m -> m.isPlayed() ? "Played" : "Pending");
        fixtureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fixtureTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Match m, boolean empty) {
                super.updateItem(m, empty);
                setStyle(!empty && m != null && m.isPlayed() ? "-fx-background-color:#c8e6c9;" : "");
            }
        });

        Button generate = new Button("Generate Fixtures");
        generate.setOnAction(e -> {
            if (!service.getMatches().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                        "This replaces the current fixtures and erases all results. Continue?");
                if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            }
            act(service::generateFixtures, "Fixtures generated");
        });

        Label matchLabel = new Label("Select a match to enter its result");
        matchLabel.setStyle("-fx-font-weight:bold;");
        Spinner<Integer> homeGoals = new Spinner<>(0, 30, 0);
        Spinner<Integer> awayGoals = new Spinner<>(0, 30, 0);
        homeGoals.setPrefWidth(80);
        awayGoals.setPrefWidth(80);
        fixtureTable.getSelectionModel().selectedItemProperty().addListener((o, ov, m) -> {
            if (m == null) {
                matchLabel.setText("Select a match to enter its result");
                return;
            }
            matchLabel.setText(m.getHome().getName() + "  vs  " + m.getAway().getName());
            homeGoals.getValueFactory().setValue(m.isPlayed() ? m.getHomeGoals() : 0);
            awayGoals.getValueFactory().setValue(m.isPlayed() ? m.getAwayGoals() : 0);
        });

        Button save = new Button("Save Result");
        save.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;");
        save.setOnAction(e -> {
            Match m = fixtureTable.getSelectionModel().getSelectedItem();
            if (m == null) { alert(Alert.AlertType.WARNING, "No match selected", "Select a match first."); return; }
            int h = homeGoals.getValue(), a = awayGoals.getValue();
            act(() -> service.recordResult(m, h, a), "Result saved: " + m.getHome().getName()
                    + " " + h + " - " + a + " " + m.getAway().getName());
        });

        HBox entry = new HBox(10, matchLabel, new Label("Home goals:"), homeGoals,
                new Label("Away goals:"), awayGoals, save);
        VBox box = new VBox(10, generate, fixtureTable, entry);
        box.setPadding(new Insets(15));
        VBox.setVgrow(fixtureTable, Priority.ALWAYS);
        return new Tab("Fixtures & Results", box);
    }

    // ---------- Tab 3: Standings ----------
    private Tab standingsTab() {
        addCol(standingsTable, "Pos", t -> String.valueOf(standingsTable.getItems().indexOf(t) + 1));
        addCol(standingsTable, "Team", Team::getName);
        addCol(standingsTable, "P", t -> String.valueOf(t.getPlayed()));
        addCol(standingsTable, "W", t -> String.valueOf(t.getWon()));
        addCol(standingsTable, "D", t -> String.valueOf(t.getDrawn()));
        addCol(standingsTable, "L", t -> String.valueOf(t.getLost()));
        addCol(standingsTable, "GF", t -> String.valueOf(t.getGoalsFor()));
        addCol(standingsTable, "GA", t -> String.valueOf(t.getGoalsAgainst()));
        addCol(standingsTable, "GD", t -> String.valueOf(t.getGoalDiff()));
        addCol(standingsTable, "Pts", t -> String.valueOf(t.getPoints()));
        standingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        standingsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Team t, boolean empty) {
                super.updateItem(t, empty);
                boolean leader = !empty && t != null && getIndex() == 0 && t.getPlayed() > 0;
                setStyle(leader ? "-fx-background-color:#fff59d;" : "");
            }
        });
        VBox box = new VBox(10, new Label("League table (3 points for a win, 1 for a draw). The leader is highlighted."),
                standingsTable);
        box.setPadding(new Insets(15));
        VBox.setVgrow(standingsTable, Priority.ALWAYS);
        return new Tab("Standings", box);
    }

    // ---------- Tab 4: Top scorers ----------
    private Tab scorersTab() {
        addCol(scorerTable, "Rank", p -> String.valueOf(scorerTable.getItems().indexOf(p) + 1));
        addCol(scorerTable, "Player", Player::getName);
        addCol(scorerTable, "Team", Player::getTeamName);
        addCol(scorerTable, "Position", Player::getPosition);
        addCol(scorerTable, "Goals", p -> String.valueOf(p.getGoals()));
        scorerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        chartY.setLabel("Goals");
        scorerChart.setTitle("Top 5 Scorers");
        scorerChart.setLegendVisible(false);
        scorerChart.setAnimated(false);
        scorerChart.setPrefHeight(280);

        VBox box = new VBox(10, scorerTable, scorerChart);
        box.setPadding(new Insets(15));
        VBox.setVgrow(scorerTable, Priority.ALWAYS);
        return new Tab("Top Scorers", box);
    }

    // =============== HELPERS ===============
    /** Runs one backend action. A broken rule becomes an error Alert; success refreshes every tab. */
    private void act(Runnable action, String successMessage) {
        try {
            action.run();
            status.setText(successMessage);
            refreshAll();
        } catch (TournamentException ex) {
            alert(Alert.AlertType.ERROR, "Cannot do that", ex.getMessage());
        }
    }

    private <T> void addCol(TableView<T> table, String title, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d -> new SimpleStringProperty(value.apply(d.getValue())));
        table.getColumns().add(col);
    }

    private void refreshPlayers() {
        Team t = teamList.getSelectionModel().getSelectedItem();
        if (t == null) {
            teamTitle.setText("Select a team to see its squad");
            playerTable.setItems(FXCollections.observableArrayList());
            return;
        }
        teamTitle.setText(t.getName() + " (" + t.getPlayers().size() + " players)");
        playerTable.setItems(FXCollections.observableArrayList(t.getPlayers()));
    }

    /** One method keeps every tab in sync with the backend. */
    private void refreshAll() {
        Team selTeam = teamList.getSelectionModel().getSelectedItem();
        Player selPlayer = playerTable.getSelectionModel().getSelectedItem();
        teamList.setItems(FXCollections.observableArrayList(service.getTeams()));
        if (selTeam != null && service.getTeams().contains(selTeam))
            teamList.getSelectionModel().select(selTeam);
        refreshPlayers();
        if (selPlayer != null && playerTable.getItems().contains(selPlayer))
            playerTable.getSelectionModel().select(selPlayer);

        Match selMatch = fixtureTable.getSelectionModel().getSelectedItem();
        fixtureTable.setItems(FXCollections.observableArrayList(service.getMatches()));
        if (selMatch != null && service.getMatches().contains(selMatch))
            fixtureTable.getSelectionModel().select(selMatch);

        standingsTable.setItems(FXCollections.observableArrayList(service.getStandings()));
        scorerTable.setItems(FXCollections.observableArrayList(service.getTopScorers(10)));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Player p : service.getTopScorers(5)) {
            if (p.getGoals() > 0)
                series.getData().add(new XYChart.Data<>(
                        p.getName() + " (" + p.getTeamName() + " #" + p.getJersey() + ")", p.getGoals()));
        }
        scorerChart.getData().clear();
        scorerChart.getData().add(series);
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
