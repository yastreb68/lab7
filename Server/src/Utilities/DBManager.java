package Utilities;

import Collection.Coordinates;
import Collection.Movie;
import Collection.Person;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import static Utilities.Server.logger;


public class DBManager {
    private static final Semaphore semaphore = new Semaphore(1);

    private static Connection connectToDB() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("resources/properties.txt"));
            String url = properties.getProperty("url");
            String login = properties.getProperty("login");
            String password = properties.getProperty("password");
            return DriverManager.getConnection(url, login, password);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<Movie> getMoviesFromDB() {
        ArrayList<Movie> movies = new ArrayList<>();
        try {
            Connection connection = connectToDB();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Movie");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Coordinates coordinates = getCoordinatesFromDB(resultSet.getInt("coordinates_id"));
                LocalDateTime creationDate = resultSet.getTimestamp("creationDate").toLocalDateTime();
                int oscarsCount = resultSet.getInt("oscarsCount");
                double budget = resultSet.getDouble("budget");
                Long length = resultSet.getLong("length");
                String mpaaRating = resultSet.getString("mpaaRating");
                Person operator = getPersonFromDB(resultSet.getInt("operator_id"));
                int ownerID = resultSet.getInt("owner_id");

                Movie movie = new Movie(id, name, coordinates, creationDate, oscarsCount, budget, length, mpaaRating, operator, getUserLoginByID(ownerID));
                movies.add(movie);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println();
        }
        return movies;
    }

    private static Coordinates getCoordinatesFromDB(int coordinatesId) {
        Coordinates coordinates = null;
        try {
            Connection connection = connectToDB();
            String query = "SELECT * FROM Coordinates WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, coordinatesId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Float x = resultSet.getFloat("x");
                float y = resultSet.getFloat("y");
                coordinates = new Coordinates(x, y);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка: " + e.getMessage());
        }

        return coordinates;
    }

    private static Person getPersonFromDB(int personId) {
        Person person = null;
        try {
            Connection connection = connectToDB();
            String query = "SELECT * FROM Person WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, personId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String passportID = resultSet.getString("passportID");
                String hairColor = resultSet.getString("hairColor");
                person = new Person(name, passportID, hairColor);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка: " + e.getMessage());
        }

        return person;
    }
    public static boolean addMovieToDB(Movie movie, int id) {
        try (Connection connection = connectToDB()) {
            semaphore.acquire();
            String coordinatesQuery = "INSERT INTO Coordinates (x, y) VALUES (?, ?) RETURNING id";
            PreparedStatement coordinatesStatement = connection.prepareStatement(coordinatesQuery);
            coordinatesStatement.setFloat(1, movie.getCoordinates().getX());
            coordinatesStatement.setFloat(2, movie.getCoordinates().getY());
            ResultSet coordinatesResult = coordinatesStatement.executeQuery();
            int coordinatesID = -1;
            if (coordinatesResult.next()) {
                coordinatesID = coordinatesResult.getInt(1);
            }

            String personQuery = "INSERT INTO Person (name, passportID, hairColor) VALUES (?, ?, CAST(? AS Color)) RETURNING id";
            PreparedStatement personStatement = connection.prepareStatement(personQuery);
            Person operator = movie.getOperator();
            personStatement.setString(1, operator.getName());
            personStatement.setString(2, operator.getPassportID());
            personStatement.setString(3, operator.getHairColor().toString());
            ResultSet personResult = personStatement.executeQuery();
            int operatorID = 0;
            if (personResult.next()) {
                operatorID = personResult.getInt(1);
            }

            String movieQuery = "INSERT INTO Movie (name, coordinates_id, creationDate, oscarsCount, budget, length, MpaaRating, operator_id, owner_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS MpaaRating), ?, ?)";
            PreparedStatement movieStatement = connection.prepareStatement(movieQuery);
            movieStatement.setString(1, movie.getName());
            movieStatement.setInt(2, coordinatesID);
            movieStatement.setTimestamp(3, Timestamp.valueOf(movie.getCreationDate()));
            movieStatement.setInt(4, movie.getOscarsCount());
            movieStatement.setDouble(5, movie.getBudget());
            movieStatement.setLong(6, movie.getLength());
            movieStatement.setString(7, movie.getMpaaRating().toString());
            movieStatement.setInt(8, operatorID);
            movieStatement.setInt(9, id);
            semaphore.release();
            int counter = movieStatement.executeUpdate();
            return counter > 0;
        } catch (SQLException | InterruptedException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении Movie в базу данных: " + e.getMessage());
            return false;
        }
    }
    public static boolean updateMovieInDB(int id, Movie movie) throws SQLException {
        try (Connection connection = connectToDB()) {
            semaphore.acquire();
            String updateCoordinatesQuery = "UPDATE Coordinates SET x = ?, y = ? WHERE id = (SELECT coordinates_id FROM Movie WHERE id = ?) RETURNING id";
            PreparedStatement updateCoordinatesStatement = connection.prepareStatement(updateCoordinatesQuery);
            updateCoordinatesStatement.setFloat(1, movie.getCoordinates().getX());
            updateCoordinatesStatement.setFloat(2, movie.getCoordinates().getY());
            updateCoordinatesStatement.setInt(3, id);
            ResultSet updateCoordinatesResult = updateCoordinatesStatement.executeQuery();
            if (!updateCoordinatesResult.next()) return false;
            int coordinatesID = updateCoordinatesResult.getInt(1);


            String updatePersonQuery = "UPDATE Person SET name = ?, passportID = ?, hairColor = CAST(? AS Color) WHERE id = (SELECT coordinates_id FROM Movie WHERE id = ?) RETURNING id";
            PreparedStatement updatePersonStatement = connection.prepareStatement(updatePersonQuery);
            Person operator = movie.getOperator();
            updatePersonStatement.setString(1, operator.getName());
            updatePersonStatement.setString(2, operator.getPassportID());
            updatePersonStatement.setString(3, operator.getHairColor().toString());
            updatePersonStatement.setInt(4, id);
            ResultSet updatePersonResult = updatePersonStatement.executeQuery();
            if (!updatePersonResult.next()) return false;
            int operatorID = updatePersonResult.getInt(1);

            String updateMovieQuery = "UPDATE Movie SET name = ?, coordinates_id = ?, creationDate = ?, oscarsCount = ?, budget = ?, length = ?, MpaaRating = CAST(? AS MpaaRating), operator_id = ? WHERE id = ?";
            PreparedStatement updateMovieStatement = connection.prepareStatement(updateMovieQuery);
            updateMovieStatement.setString(1, movie.getName());
            updateMovieStatement.setInt(2, coordinatesID);
            updateMovieStatement.setTimestamp(3, Timestamp.valueOf(movie.getCreationDate()));
            updateMovieStatement.setInt(4, movie.getOscarsCount());
            updateMovieStatement.setDouble(5, movie.getBudget());
            updateMovieStatement.setLong(6, movie.getLength());
            updateMovieStatement.setString(7, movie.getMpaaRating().toString());
            updateMovieStatement.setInt(8, operatorID);
            updateMovieStatement.setInt(9, id);
            semaphore.release();
            int counter = updateMovieStatement.executeUpdate();
            return counter > 0;
        } catch (SQLException | InterruptedException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении Movie в базе данных: " + e.getMessage());
            return false;
        }
    }
    public static List<Integer> clearUserMovies(int id) {
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT id, coordinates_id, operator_id FROM Movie WHERE owner_id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, id);
            ResultSet result = selectStatement.executeQuery();
            List<Integer> coordinatesIDList= new ArrayList<>();
            List<Integer> operatorIDList= new ArrayList<>();
            List<Integer> movieIDList= new ArrayList<>();
            while (result.next()) {
                movieIDList.add(result.getInt("id"));
                coordinatesIDList.add(result.getInt("coordinates_id"));
                operatorIDList.add(result.getInt("operator_id"));
            }
            String deleteMoviesQuery = "DELETE FROM Movie WHERE owner_id = ?";
            PreparedStatement deleteMoviesStatement = connection.prepareStatement(deleteMoviesQuery);
            deleteMoviesStatement.setInt(1, id);
            deleteMoviesStatement.executeUpdate();

            String deleteCoordinatesQuery = "DELETE FROM Coordinates WHERE id = ?";
            PreparedStatement deleteCoordinatesStatement = connection.prepareStatement(deleteCoordinatesQuery);
            for (int coordinatesID: coordinatesIDList) {
                deleteCoordinatesStatement.setInt(1, coordinatesID);
                deleteCoordinatesStatement.executeUpdate();
            }
            String deletePersonQuery = "DELETE FROM Person WHERE id = ?";
            PreparedStatement deletePersonStatement = connection.prepareStatement(deletePersonQuery);
            for (int operatorID: operatorIDList) {
                deletePersonStatement.setInt(1, operatorID);
                deletePersonStatement.executeUpdate();
            }

            return movieIDList;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении Movie из базы данных: " + e.getMessage());
            return null;
        }
    }

    public static boolean removeUserMovieByID(int id, int movieID) {
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT coordinates_id, operator_id FROM Movie WHERE owner_id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, id);
            ResultSet result = selectStatement.executeQuery();
            if (!result.next()) return false;
            int coordinatesID = result.getInt("coordinates_id");
            int operatorID = result.getInt("operator_id");

            String deleteMoviesQuery = "DELETE FROM Movie WHERE id = ? AND owner_id = ?";
            PreparedStatement deleteMoviesStatement = connection.prepareStatement(deleteMoviesQuery);
            deleteMoviesStatement.setInt(1, movieID);
            deleteMoviesStatement.setInt(2, id);
            int counter = deleteMoviesStatement.executeUpdate();
            if (counter <= 0) return false;

            String deleteCoordinatesQuery = "DELETE FROM Coordinates WHERE id = ?";
            PreparedStatement deleteCoordinatesStatement = connection.prepareStatement(deleteCoordinatesQuery);
            deleteCoordinatesStatement.setInt(1, coordinatesID);
            deleteCoordinatesStatement.executeUpdate();

            String deletePersonQuery = "DELETE FROM Person WHERE id = ?";
            PreparedStatement deletePersonStatement = connection.prepareStatement(deletePersonQuery);
            deletePersonStatement.setInt(1, operatorID);
            deletePersonStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении Movie из базы данных: " + e.getMessage());
            return false;
        }
    }
    public static int getMinUserMovieID(int id) {
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT MIN(id) FROM Movie WHERE owner_id =?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, id);
            ResultSet result = selectStatement.executeQuery();
            if (!result.next()) return 0;
            return result.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Таблица Movie пустая: " + e.getMessage());
            return 0;
        }
    }
    public static boolean isLoginInUsersTable(String login) {
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT id FROM Users WHERE login = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, login);
            ResultSet result = selectStatement.executeQuery();
            if (!result.next()) return false;
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Данного user не существует: " + e.getMessage());
            return false;
        }
    }
    public static int regUser(String login, String pass) {
        if (isLoginInUsersTable(login)) return 0;
        try (Connection connection = connectToDB()) {
            semaphore.acquire();
            String insertQuery = "INSERT INTO Users (login, pass_hash, pass_salt) VALUES (?, ?, ?) RETURNING id";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            String pass_salt = PasswordManager.generateSalt();
            String pass_hash = PasswordManager.hashPassword(pass, pass_salt);
            insertStatement.setString(1, login);
            insertStatement.setString(2, pass_hash);
            insertStatement.setString(3, pass_salt);
            ResultSet result = insertStatement.executeQuery();
            semaphore.release();
            if (!result.next()) return 0;
            return result.getInt(1);
        } catch (SQLException | InterruptedException e) {
            logger.log(Level.SEVERE, "Не удалось добавить user: " + e.getMessage());
            return 0;
        }
    }
    public static int authUser(String login, String pass) {
        if (!isLoginInUsersTable(login)) return 0;
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT id, pass_hash, pass_salt FROM Users WHERE login = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, login);
            ResultSet result = selectStatement.executeQuery();
            if (!result.next()) return 0;
            String pass_hash = result.getString("pass_hash");
            String pass_salt = result.getString("pass_salt");
            if (!pass_hash.equals(PasswordManager.hashPassword(pass, pass_salt))) return 0;
            return result.getInt("id");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Данного user не существует: " + e.getMessage());
            return 0;
        }
    }
    public static String getUserLoginByID(int id) {
        try (Connection connection = connectToDB()) {
            String selectQuery = "SELECT login FROM Users WHERE id = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, id);
            ResultSet result = selectStatement.executeQuery();
            if (!result.next()) return null;
            return result.getString(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Данного user не существует: " + e.getMessage());
            return null;
        }
    }



}