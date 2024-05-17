package Commands;


import Collection.Movie;
import Utilities.DBManager;
import Utilities.Response;

import java.util.List;

public class Clear implements Command{
    public Response execute(String args, Movie objArgs, int id) {
        List<Integer> movieIDList = DBManager.clearUserMovies(id);
        if (movieIDList != null) {
            movieIDList.stream().forEach(movieID -> movies.removeIf(movie -> movie.getId() == movieID));
            return new Response("Все ваши элементы из коллекции удалены");
        }
        return new Response("У вас нет movie, которые можно удалить");
    }

    @Override
    public String getName() {
        return "clear";
    }
}
