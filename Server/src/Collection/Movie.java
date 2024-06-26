package Collection;


import Utilities.Server;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Movie implements Comparable<Movie>, Serializable {
    @Serial
    private static final long serialVersionUID = 525252L;
    private int id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private int oscarsCount; //Значение поля должно быть больше 0
    private double budget; //Значение поля должно быть больше 0
    private Long length; //Поле не может быть null, Значение поля должно быть больше 0
    private MpaaRating mpaaRating; //Поле не может быть null
    private Person operator; //Поле не может быть null
    private String ownerLogin;
    public static int generateUniqueId() {
        ArrayList<Movie> movies = Server.getMovies();
        Set<Integer> ids = new HashSet<>();
        for (Movie movie: movies) {
            ids.add(movie.getId());
        }
        movies.stream().forEach(m -> ids.add(m.getId()));
        int maxId = 1;
        for (Integer id: ids) {
            if (id > maxId) maxId = id;
        }
        return ++maxId;
    }

    public Movie(String name, Coordinates coordinates, int oscarsCount, double budget, Long length, MpaaRating mpaaRating, Person operator, String ownerLogin) {
        this.name = name;
        this.coordinates = coordinates;
        this.operator = operator;
        this.oscarsCount = oscarsCount;
        this.budget = budget;
        this.length = length;
        this.mpaaRating = mpaaRating;
        this.id = generateUniqueId();
        this.ownerLogin = ownerLogin;

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        this.creationDate = LocalDateTime.parse(date, formatter);


    }
    public Movie(int id, String name, Coordinates coordinates, LocalDateTime creationDate, int oscarsCount, double budget, Long length, String mpaaRating, Person operator, String ownerLogin) {
        this.name = name;
        this.coordinates = coordinates;
        this.mpaaRating = MpaaRating.valueOf(mpaaRating);
        this.id = id;
        this.creationDate = creationDate;
        this.operator = operator;
        this.oscarsCount = oscarsCount;
        this.budget = budget;
        this.length = length;
        this.ownerLogin = ownerLogin;
    }
    public boolean checkMovie() {
        return (id > 0) && (name != null) && !name.isEmpty() && (coordinates.getX() != null) && (coordinates.getX() > -625) && (!(coordinates.getY() >= 209)) && (creationDate != null) && (oscarsCount > 0) && (!(budget <= 0)) && (length != null) && (length > 0) && (mpaaRating != null) && (operator.getHairColor() != null) && (operator.getName() != null) && (operator.getPassportID() != null);
    }


    @Override
    public int compareTo(Movie o) {
        return this.name.compareTo(o.getName());
    }

    public int getOscarsCount() {
        return oscarsCount;
    }

    public Person getOperator() {
        return operator;
    }

    public Long getLength() {
        return length;
    }

    public double getBudget() {
        return budget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id && oscarsCount == movie.oscarsCount && Double.compare(budget, movie.budget) == 0 && Objects.equals(name, movie.name) && Objects.equals(coordinates, movie.coordinates) && Objects.equals(creationDate, movie.creationDate) && Objects.equals(length, movie.length) && mpaaRating == movie.mpaaRating && Objects.equals(operator, movie.operator);
    }

    @Override
    public String toString() {
        return "Movie{" + "\n" +
                "id=" + id + "\n" +
                ", name='" + name + '\'' + "\n" +
                ", coordinates=" + coordinates + "\n" +
                ", creationDate=" + creationDate + "\n" +
                ", oscarsCount=" + oscarsCount + "\n" +
                ", budget=" + budget + "\n" +
                ", length=" + length + "\n" +
                ", mpaaRating=" + mpaaRating + "\n" +
                ", operator=" + operator + "\n" +
                ", ownerLogin='" + ownerLogin + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, oscarsCount, budget, length, mpaaRating, operator);
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

}
