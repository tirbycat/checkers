package com.mycompany.checkersserver.entity;

import com.mycompany.checkersserver.HibernateSessionManager;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author pzigel
 */
@Entity
@Table(name="games")
public class Game implements Serializable {
    public static final int GAME_STARTED = 1,
                            GAME_INPROCESS = 2,
                            GAME_FINISHED = 3;
    
    public static final char
                EMPTY = 'e',
                WHITE = 'w',
                WHITE_QUEEN = 'Q',
                BLACK = 'b',
                BLACK_KING = 'K';
    
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
    
    @Column(name="game_field",length=64)
    private char[][] gameField;
    
    private char creatorColor;
    
    private char currentMove;
    
    public Game() {
    }

    public Game(User user1) {
        this.status = GAME_STARTED;
        this.startDate = new Date();
        this.user1 = user1;
        initializeGameField();
        creatorColor = WHITE;
        currentMove = WHITE;
    }
    
    private boolean isGrayField(int row, int col){
        return (row+col)%2 == 0;
    }
    
    private void initializeGameField(){
        gameField = new char[8][8];
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                gameField[row][col] = EMPTY;
                if(isGrayField(row, col) && row<3){
                    gameField[row][col] = WHITE;
                }
                if(isGrayField(row,col) && row>4){
                    gameField[row][col] = BLACK;
                }
            }
        }
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

    public char getCreatorColor() {
        return creatorColor;
    }

    private JSONArray getgameField(){
        JSONArray cols = new JSONArray();
        for(int row=0;row<8;row++){
            JSONArray rows = new JSONArray();
            cols.add(rows);
            for(int col=0;col<8;col++){
                char c = gameField[row][col];
                rows.add(String.valueOf(c));
            }
        }
        return cols;
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
        if(status == GAME_INPROCESS){
            result.put("currentMove", String.valueOf(currentMove));
        }
        
        result.put("gameField", getgameField());
        
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

    public JSONObject getAvailableMove(int row, int col) {
        JSONObject result = new JSONObject();
        JSONArray moves = new JSONArray();
        
        char KING = BLACK_KING;
        if(currentMove == WHITE){
            KING = WHITE_QUEEN;
        }
        
        if(gameField[row][col] == currentMove ||
           gameField[row][col] == KING){
            moves = availableJumpes(row, col);
            if(moves.size() == 0){
                moves = availableMoves(row, col);
            }
        }
        
        result.put("moves", moves);
        return result;
    }
    
    private void setNextGamerMove(){
        if(currentMove == WHITE){
            currentMove = BLACK;
        }else{
            currentMove = WHITE;
        }
    }
    
    private boolean checkJump(int rOver, int cOver, int rTo, int cTo) {
        if (rTo < 0 || rTo > 7 || cTo < 0 || cTo > 7){
            return false;
        }

        if (gameField[rTo][cTo] != EMPTY){
            return false;
        }

        if (currentMove == WHITE) {
            if (gameField[rOver][cOver] != BLACK && gameField[rOver][cOver] != BLACK_KING){
               return false;
            }
            return true;
        }else {
            if (gameField[rOver][cOver] != WHITE && gameField[rOver][cOver] != WHITE_QUEEN){
               return false;
            }
            return true;
        }
    }
    
    private boolean checkMove(int rowFrom, int colFrom, int rowTo, int colTo){
        if (rowTo < 0 || rowTo > 7 || colTo < 0 || colTo > 7){
            return false;
        }
        
        if (gameField[rowTo][colTo] != EMPTY){
            return false;
        }

        if (currentMove == WHITE) {
            if (rowTo-rowFrom != 1 || Math.abs(colTo-colFrom) != 1){
               return false;
            }
            return true;
        }else {
            if (rowFrom-rowTo != 1 || Math.abs(colFrom-colTo) != 1){
               return false;
            }
            return true;
        }
    }
    
    private JSONArray availableJumpes(int row, int col){
        JSONArray result = new JSONArray();
        
        if(checkJump(row-1, col-1, row-2, col-2)){
            JSONObject o = new JSONObject();
            o.put("row", row-2);
            o.put("col", col-2);
            result.add(o);
        }
        if(checkJump(row-1, col+1, row-2, col+2)){
            JSONObject o = new JSONObject();
            o.put("row", row-2);
            o.put("col", col+2);
            result.add(o);
        }
        if(checkJump(row+1, col-1, row+2, col-2)){
            JSONObject o = new JSONObject();
            o.put("row", row+2);
            o.put("col", col-2);
            result.add(o);
        }
        if(checkJump(row+1, col+1, row+2, col+2)){
            JSONObject o = new JSONObject();
            o.put("row", row+2);
            o.put("col", col+2);
            result.add(o);
        }
        
        return result;
    }
    
    private JSONArray availableMoves(int row, int col){
        JSONArray result = new JSONArray();
        
        switch(gameField[row][col]){
            case WHITE:
                if(row<7 && col<7 && gameField[row+1][col+1] == EMPTY){
                    JSONObject o = new JSONObject();
                    o.put("row", row+1);
                    o.put("col", col+1);
                    result.add(o);
                }
                if(row<7 && col>0 && gameField[row+1][col-1] == EMPTY){
                    JSONObject o = new JSONObject();
                    o.put("row", row+1);
                    o.put("col", col-1);
                    result.add(o);
                }
                break;
                
            case BLACK:
                if(row>0 && col>0 && gameField[row-1][col-1] == EMPTY){
                    JSONObject o = new JSONObject();
                    o.put("row", row-1);
                    o.put("col", col-1);
                    result.add(o);
                }
                if(row>0 && col<7 && gameField[row-1][col+1] == EMPTY){
                    JSONObject o = new JSONObject();
                    o.put("row", row-1);
                    o.put("col", col+1);
                    result.add(o);
                }
                break;
            case WHITE_QUEEN:
                break;
            case BLACK_KING:
                break;
        }
        
        return result;
    }

    public JSONObject doMove(int rowFrom, int colFrom, int rowTo, int colTo) {
        JSONObject result = new JSONObject();

        if(currentMove == gameField[rowFrom][colFrom]){
            if(Math.abs(rowFrom-rowTo) > 1){
                int rowOver = (rowFrom+rowTo)/2;
                int colOver = (colFrom+colTo)/2;
                if(checkJump(rowOver, colOver, rowTo, colTo)){
                    gameField[rowTo][colTo] = gameField[rowFrom][colFrom];
                    gameField[rowOver][colOver] = EMPTY;
                    gameField[rowFrom][colFrom] = EMPTY;
                    setNextGamerMove();
                }
            }else{
                if(checkMove(rowFrom, colFrom, rowTo, colTo)){
                    gameField[rowTo][colTo] = gameField[rowFrom][colFrom];
                    gameField[rowFrom][colFrom] = EMPTY;
                    setNextGamerMove();
                }
            }
        }
        result.put("gameStatus", status);
        result.put("currentMove", String.valueOf(currentMove));
        result.put("gameField", getgameField());
        return result;
    }
}