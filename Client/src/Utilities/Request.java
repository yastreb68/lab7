package Utilities;

import java.io.Serial;
import java.io.Serializable;

public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 5252L;
    

    private String commandName;
    private String commandStrArg;
    private String login;
    private String pass;
    private boolean reg;
    private Serializable commandObjArg;

    public Request(String commandName, String commandStrArg, Serializable commandObjArg, String login, String pass) {
        this.commandName = commandName;
        this.commandStrArg = commandStrArg;
        this.commandObjArg = commandObjArg;
        this.login = login;
        this.pass = pass;
    }

    public Request(String commandName, String commandStrArg, String login, String pass) {
        this.commandName = commandName;
        this.commandStrArg = commandStrArg;
        this.login = login;
        this.pass = pass;
    }
    public Request(String login, String pass, boolean reg) {

        this.login = login;
        this.pass = pass;
        this.reg = reg;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getCommandStrArg() {
        return commandStrArg;
    }

    public Serializable getCommandObjArg() {
        return commandObjArg;
    }

    @Override
    public String toString() {
        return "Request{" +
                "commandName='" + commandName + '\'' +
                ", commandStrArg='" + commandStrArg + '\'' +
                ", login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                ", commandObjArg=" + commandObjArg +
                '}';
    }

    public boolean isReg() {
        return reg;
    }
}
