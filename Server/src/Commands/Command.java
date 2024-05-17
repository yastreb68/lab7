package Commands;

import Collection.Movie;
import Utilities.Response;
import Utilities.Server;

import java.util.ArrayList;

public interface Command {
    Response execute(String args, Movie objArgs, int id);
    String getName();
    ArrayList<Movie> movies = Server.getMovies();
    
    default Response executeInScript(String userArgs, Movie objArgs, int id) {
        return null;
    }
}
