package com.mycompany.checkersserver.entity;

import com.mycompany.checkersserver.HibernateSessionManager;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author pzigel
 */
@Entity
@Table(name="users")
public class User implements Serializable{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="login", nullable=false, unique=true)
    private String login;
    
    @Column(name="reg_date")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date regTime;
    
    @Column(name="password")
    private String password;
    
    @OneToMany(targetEntity=Game.class, cascade=CascadeType.ALL, mappedBy="user1", fetch=FetchType.EAGER)
    @MapKey(name="id")
    Map<Long, Game> games;
    
    @OneToMany(targetEntity=Game.class, cascade=CascadeType.ALL, mappedBy="user2", fetch=FetchType.EAGER)
    @MapKey(name="id")
    Map<Long, Game> gamesJoined;
    
    public User() {
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public static User getById(Long id){
        Session s = HibernateSessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        User u = (User)s.get(User.class, id);
        s.getTransaction().commit();
        return u;
    }
    
    public static User getByLogin(String id){
        Session s = HibernateSessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        User u = (User)s.createCriteria(User.class)
                .add(Restrictions.eq("login", id)).uniqueResult();
        s.getTransaction().commit();
        return u;
    }
    
    public static User create(String login, String password){
        Session s = HibernateSessionManager.getSessionFactory().openSession();
        User u = new User(login, password);
        
        s.save(u);
        s.flush();
        s.close();
        return u;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Date getReg_time() {
        return regTime;
    }

    public void setReg_time(Date reg_time) {
        this.regTime = reg_time;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<Long, Game> getGames() {
        return games;
    }
    
    public JSONObject getGameListJSON(){
        JSONObject result = new JSONObject();
        JSONArray myGames = new JSONArray();
        if(games != null){
            for(Long id: games.keySet()){
                myGames.add(id);
            }
        }
        
        JSONArray joinedGames = new JSONArray();
        if(gamesJoined != null){
            for(Long id: gamesJoined.keySet()){
                joinedGames.add(id);
            }
        }
        
        result.put("mygames", myGames);
        result.put("joinedgames", joinedGames);
        return result;
    }

    public Game createGame() {        
        Game game = null;
        Session s = HibernateSessionManager.getSessionFactory().openSession();
        synchronized(this){
            if(games == null){
                games = new HashMap<Long, Game>();
            }

            try{
                s.beginTransaction();

                game = new Game(this);

                s.save(game);

                s.flush();
                s.getTransaction().commit();
                games.put(game.getId(), game);
            }catch(Exception e){
                s.getTransaction().rollback();
                game = null;
            }
        }
        return game;
    }

    public void joinToGame(Game game) {
        synchronized(this){
            game.joinUser(this);
            gamesJoined.put(game.getId(), game);
        }
    }
}
