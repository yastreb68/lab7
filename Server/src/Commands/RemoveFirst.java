package Commands;

import Collection.Movie;
import Utilities.DBManager;
import Utilities.Response;

public class RemoveFirst implements Command{
    public Response execute(String args, Movie objArgs, int id) {

        if (!movies.isEmpty()) {
            int minUserMovieID = DBManager.getMinUserMovieID(id);
            if (DBManager.removeUserMovieByID(id, minUserMovieID)) {
                movies.removeIf(movie -> movie.getId() == minUserMovieID);
                return new Response("Ваш первый элемент коллекции успешно удалён");
            }
        }
        return new Response("Невозможно удалить первый элемент коллекции, так как она пустая или в ней нет ваших movie");
    }

    @Override
    public String getName() {
        return "remove_first";
    }
}
