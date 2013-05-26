package com.mycompany.checkersserver.webpageclasses;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.User;
import org.apache.click.Page;
import org.apache.click.control.ActionLink;
import org.apache.click.control.Form;
import org.apache.click.control.Submit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tirbycat
 */
public class IndexPage  extends Page{
    private static final Logger log = LoggerFactory.getLogger(IndexPage.class);
    private ActionLink logOutLnk = new ActionLink("logoutLnk", this, "onLogoutClick");

    public IndexPage() {
        if(!getContext().hasSessionAttribute("userid")){
            Form form = new Form("loginForm");
            form.addStyleClass("form-signin");
            
            Submit loginBtn = new Submit("loginButton", "Sign in", this, "onLoginClicked");
            loginBtn.addStyleClass("btn btn-large btn-primary");
            form.add(loginBtn);
            addControl(form);
        }else{
            logOutLnk.setLabel("logout");
            addControl(logOutLnk);
            
            User u = GameCache.getUserById((Long)getContext().getSessionAttribute("userid"));
            
            getModel().put("userid", u.getId());
        }
    }
    
    public boolean onLoginClicked() {
        String login = getContext().getRequestParameter("login");
        String password = getContext().getRequestParameter("password");
        
        User u = User.getByLogin(login);
        if(u == null){
            u = GameCache.createNewUser(login, password);
        }else if(!u.getPassword().equals(password)){
            getModel().put("login", login);
            getContext().setFlashAttribute("message", "invalid password");
            return true;
        }
        
        getContext().setSessionAttribute("userid", u.getId());
        setRedirect(IndexPage.class);
        return false;
    }
    
    public boolean onLogoutClick() {
        getContext().removeSessionAttribute("userid");
        setRedirect(IndexPage.class);
        return true;
    }
}
