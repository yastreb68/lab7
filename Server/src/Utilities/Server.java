package Utilities;
import Collection.Movie;
import Commands.Command;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static Scanner scriptScanner = null;
    private static ArrayList<Movie> movies;
    private static final LocalDateTime initializationDate = LocalDateTime.now();

    private int port;
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    ExecutorService executor = Executors.newFixedThreadPool(5);


    public static ArrayList<Movie> getMovies() {
        return movies;
    }

    public static LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public Server() throws IOException {
        movies = DBManager.getMoviesFromDB();
        Invoker.invoke();
    }

    public static final Logger logger = Logger.getLogger("Server");

    public void run() throws IOException {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Введите порт: ");
            port = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Такого порта не существует. Повторите ввод");
            new Server().run();
        }
        new Thread(() -> {
            Scanner inputThreadScanner = new Scanner(System.in);
            while (true) {
                String input = inputThreadScanner.nextLine().trim();
                if (input.equals("exit")) System.exit(0);
            }
        }).start();
        Map<String, Command> commands = Invoker.getCommands();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.log(Level.INFO, "Сервер запущен на порте " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.execute(() -> {
                        logger.log(Level.INFO, "Подключился новый клиент: " + clientSocket.getInetAddress());
                        try {
                            ObjectInputStream objectInputStream= new ObjectInputStream(clientSocket.getInputStream());
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            while (true) {
                                Future<Request> requestFuture = forkJoinPool.submit(() -> (Request) objectInputStream.readObject());
                                while(!requestFuture.isDone());
                                Request request = requestFuture.get();
                                logger.log(Level.INFO, "Получен запрос от клиента: " + request);
                                String commandName = request.getCommandName();
                                String commandStrArg = request.getCommandStrArg();
                                Movie commandObjArg = (Movie) request.getCommandObjArg();
                                String login = request.getLogin();
                                String pass = request.getPass();
                                boolean reg = request.isReg();

                                CountDownLatch latch = new CountDownLatch(1);
                                Response[] response = new Response[1];
                                new Thread(() -> {
                                    response[0] = handleRequest(reg, login, pass, commandName, commandStrArg, commandObjArg, commands);
                                    latch.countDown();
                                }).start();
                                latch.await();
                                forkJoinPool.submit(() -> {
                                    try {
                                        objectOutputStream.writeObject(response[0]);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).join();

                                logger.log(Level.INFO, "Отправлен ответ клиенту: " + response[0].getMessage());
                                if (commandName!= null && commandName.equals("exit")) {
                                    logger.log(Level.INFO, "Клиент отключился");
                                    break;
                                }
                            }
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            logger.log(Level.SEVERE, "Ошибка: " + e.getMessage());
                        }
                    });

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка: " + e.getMessage());
                }
            }
        } catch (BindException e) {
            logger.log(Level.SEVERE, "Невозможно запустить сервер на данном порту, так как он занят: " + e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка: " + e.getMessage());
        }
    }
    private static Response handleRequest(boolean reg, String login, String pass, String commandName, String commandStrArg, Movie commandObjArg, Map<String, Command> commands) {
        int id;
        Response response;
        if (reg) {
            id = DBManager.regUser(login, pass);
            if (id == 0) response = new Response("Пользователь с данным логином уже существует");
            else response = new Response("Вы успешно зарегистрировались.", id);
        } else {
            id = DBManager.authUser(login, pass);
            if (id == 0) response = new Response("Неверный логин или пароль");
            else response = new Response("Вы успешно авторизовались.", id);
        }
        if (commandName != null) {
            if (commands.containsKey(commandName) && !commandStrArg.contains("Ошибка")) {
                response = commands.get(commandName).execute(commandStrArg, commandObjArg, id);
            } else if (commandStrArg.contains("Ошибка")) {
                response = new Response(commandStrArg);
            } else {
                response = new Response("Команды " + commandName + " не существует.");
            }
        }
        return response;
    }
}