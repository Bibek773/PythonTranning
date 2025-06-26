package main.controller;
import main.java.app.LoginManager;

public abstract class BaseController {
    protected LoginManager loginManager;
    public void setLoginManager(LoginManager manager) {
        this.loginManager = manager;
    }
}
