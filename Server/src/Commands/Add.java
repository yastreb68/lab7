package Commands;



import Collection.*;
import Utilities.DBManager;
import Utilities.Response;

import java.lang.reflect.Field;

import static Utilities.Server.scriptScanner;


public class Add implements Command {
    public Response execute(String args, Movie objArgs, int id) {
        if (DBManager.addMovieToDB(objArgs, id)) {
            Field idField = null;
            Field ownerLoginField = null;
            try {
                idField = objArgs.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(objArgs, Movie.generateUniqueId());
                ownerLoginField = objArgs.getClass().getDeclaredField("ownerLogin");
                ownerLoginField.setAccessible(true);
                ownerLoginField.set(objArgs, DBManager.getUserLoginByID(id));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            movies.add(objArgs);
            return new Response("Movie успешно добавлен");
        }
        return new Response("Ошибка при добавлении movie");

    }

    public Response executeInScript(String args, Movie objArgs, int id) {
        String name = null;
        Float x = null;
        float y = 210;
        int oscarsCount = -1;
        double budget = -1;
        Long length = null;
        MpaaRating mpaaRating = null;
        String personName = null;
        String passportID = null;
        Color hairColor = null;
        try {
            name = scriptScanner.nextLine();
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации имени");
        }
        try {
            x = Float.parseFloat(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации X");
        }
        try {
            y = Float.parseFloat(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации Y");
        }
        try {
            oscarsCount = Integer.parseInt(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации количества оскаров");
        }
        try {
            budget = Double.parseDouble(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации бюджета");
        }
        try {
            length = Long.valueOf(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации длины");
        }
        try {
            mpaaRating = MpaaRating.valueOf(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации mpaa rating");
        }
        try {
            personName = scriptScanner.nextLine();
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации имени оператора");
        }
        try {
            passportID = scriptScanner.nextLine();
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации паспорта");
        }
        try {
            hairColor = Color.valueOf(scriptScanner.nextLine());
        } catch (RuntimeException e) {
            return new Response("Ошибка при инициализации цвета волос");
        }

        if (name == null) {
            return new Response("Имя не может быть пустым");
        }
        if (x <= -625) {
            return new Response("X должен быть больше -625");
        }
        if (y >= 209) {
            return new Response("Y должен быть меньше 209");
        }
        if (oscarsCount <= 0) {
            return new Response("Количество оскаров должен быть больше 0");
        }
        if (budget <= 0) {
            return new Response("Бюджет должен быть больше 0");
        }
        if (length <= 0) {
            return new Response("Длина должна быть больше 0");
        }
        if (personName == null) {
            return new Response("Имя оператора не может быть пустым");
        }
        if (passportID == null) {
            return new Response("ИД паспорта не может быть пустым");
        }
        Movie movie = new Movie(name, new Coordinates(x, y), oscarsCount, budget, length, mpaaRating, new Person(personName, passportID, hairColor), DBManager.getUserLoginByID(id));
        if (DBManager.addMovieToDB(movie, id)) {
            movies.add(movie);
            return new Response("Movie успешно добавлен");
        }
        return new Response("Команда выполнена с ошибками");
    }
    @Override
    public String getName() {
        return "add";
    }

}
