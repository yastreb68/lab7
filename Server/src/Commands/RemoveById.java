package Commands;

import Collection.Movie;
import Utilities.DBManager;
import Utilities.Response;


public class RemoveById implements Command{


    public Response execute(String args, Movie objArgs, int id) {
        if (args.isEmpty()) {
            return new Response("Команда remove_by_id требует аргументы");
        } else {
            int movieID = Integer.parseInt(args);
            if (DBManager.removeUserMovieByID(id, movieID)) {
                movies.remove(id-1);
                return new Response("Movie с таким id успешно удалён");
            } else  {
                return new Response("Movie с таким id не существует, или он принадлежит не вам");
            }
        }
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }
}
