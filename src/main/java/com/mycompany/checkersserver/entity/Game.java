package com.mycompany.checkersserver.entity;

import com.mycompany.checkersserver.HibernateSessionManager;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.hibernate.Session;
import org.json.simple.JSONObject;

/**
 *
 * @author pzigel
 */
@Entity
@Table(name="games")
public class Game implements Serializable {
    public static final int GAME_STARTED = 1;
    public static final int GAME_INPROCESS = 2;
    public static final int GAME_FINISHED = 3;
    
//    @TableGenerator(name = "gameid", table = "game", pkColumnName = "idGame", 
//            pkColumnValue = "idValue",allocationSize = 1)
//    @GeneratedValue (strategy = GenerationType.TABLE, generator = "id")
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(targetEntity=User.class)
    @JoinColumn(name="user1_id", nullable=false)
    private User user1;
    
    @ManyToOne(targetEntity=User.class)
    @JoinColumn(name="user2_id", nullable=true)
    private User user2;
    
    @Column(name="start_date", columnDefinition = "timestamp without time zone default now()")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date startDate;
    
    @Column(name="finish_date", nullable=true)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date finishDate;
    
    @Column(name="status")
    private int status;
    
    public Game() {
    }

    public Game(User user1) {
        this.status = GAME_STARTED;
        this.startDate = new Date();
        this.user1 = user1;
    }

    public static Game getById(Long id){
        Session s = HibernateSessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        Game o = (Game)s.get(Game.class, id);
        s.getTransaction().commit();
        return o;
    }

    public static Game create(User user){
        Session s = HibernateSessionManager.getSessionFactory().openSession();
        Game g = new Game(user);
        
        s.save(g);        
        s.flush();
        s.close();
        return g;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }
    
    public void setUser2(User user) {
        this.user2 = user;
    }

    public Date getAddDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public JSONObject toJSONString() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("startDate", startDate.toString());
        if(finishDate != null){
            result.put("finishDate", finishDate.toString());
        }
        result.put("user1Id", user1.getId());
        if(user2 != null){
            result.put("user2Id", user2.getId());
        }
        result.put("gameStatus", status);
        
        return result;
    }

    public void joinUser(User user) {
        this.setUser2(user);
        this.setStatus(GAME_INPROCESS);
        Session s = HibernateSessionManager.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(this);
        s.getTransaction().commit();
    }
}
