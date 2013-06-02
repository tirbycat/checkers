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

    private char winnerColor;
    
    private int resumeMoveRow;
    private int resumeMoveCol;
    
    public Game() {
    }

    public Game(User user1) {
        this.status = GAME_STARTED;
        this.startDate = new Date();
        this.user1 = user1;
        initializeGameField();
        creatorColor = WHITE;
        currentMove = WHITE;
        winnerColor = ' ';
        resumeMoveRow = -1;
        resumeMoveCol = -1;
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
            result.put("resumeMoveRow", resumeMoveRow);
            result.put("resumeMoveCol", resumeMoveCol);
            if(resumeMoveRow != -1){
                result.put("moves", availableJumpes(resumeMoveRow, resumeMoveCol, true));
            }
        }
        if(status == GAME_FINISHED){
            result.put("winnerColor", String.valueOf(winnerColor));
        }
        
        result.put("gameField", getgameField());
        
        return result;
    }

    public void joinUser(User user) {
        synchronized(this){
            this.setUser2(user);
            this.setStatus(GAME_INPROCESS);
            Session s = HibernateSessionManager.getSessionFactory().openSession();
            s.beginTransaction();
            s.update(this);
            s.getTransaction().commit();
        }
    }

    private char getQueenCode(char color){
        char queen = BLACK_KING;
        if(color == WHITE){
            queen = WHITE_QUEEN;
        }
        return queen;
    }
    
    public JSONObject getAvailableMove(int row, int col) {
        JSONObject result = new JSONObject();
        JSONArray moves = new JSONArray();

        if(gameField[row][col] == currentMove ||
           gameField[row][col] == getQueenCode(currentMove)){
            if(resumeMoveRow == -1 || 
               resumeMoveRow == row && resumeMoveCol == col){
                moves = availableJumpes(row, col, false);
            }
            if(resumeMoveRow == -1){
                if(moves.size() == 0){
                    moves = availableMoves(row, col);
                }
            }
        }
        
        result.put("moves", moves);
        return result;
    }
    
    private char getNextGamerColor(char color){
        char result = WHITE;
        if(color == WHITE){
            result = BLACK;
        }
        return result;
    }
    
    private boolean checkJump(int fRow, int fCol, int rOver, int cOver, int rTo, int cTo) {
        if (rTo < 0 || rTo > 7 || cTo < 0 || cTo > 7){
            return false;
        }
        
        if(Math.abs(fRow-rTo) > 2){
            if(gameField[fRow][fCol] != getQueenCode(currentMove)){
                return false;
            }
            int fieldsR = Math.abs(fRow-rOver);
            int fieldsC = Math.abs(fCol-cOver);
            int signR = -(fRow-rOver)/fieldsR;
            int signC = -(fCol-cOver)/fieldsC;
            for(int i=1;i<fieldsR;i++){
                if (gameField[fRow + signR*i][fCol + signC*i] != EMPTY){
                    return false;
                }
            }
        }

        if (gameField[rTo][cTo] != EMPTY){
            return false;
        }

        char oppColor = getNextGamerColor(currentMove);
        if (gameField[rOver][cOver] != oppColor && gameField[rOver][cOver] != getQueenCode(oppColor)){
           return false;
        }
        return true;
    }
    
    private boolean checkMove(int rowFrom, int colFrom, int rowTo, int colTo){
        if (rowTo < 0 || rowTo > 7 || colTo < 0 || colTo > 7){
            return false;
        }
        
        if(Math.abs(rowFrom-rowTo) > 1){
            if(gameField[rowFrom][colFrom] != getQueenCode(currentMove)){
                return false;
            }
            int fieldsR = Math.abs(rowFrom-rowTo);
            int fieldsC = Math.abs(colFrom-colTo);
            int signR = -(rowFrom-rowTo)/fieldsR;
            int signC = -(colFrom-colTo)/fieldsC;
            for(int i=1;i<fieldsR;i++){
                if (gameField[rowFrom + signR*i][colFrom + signC*i] != EMPTY){
                    return false;
                }
            }
        }
        
        if (gameField[rowTo][colTo] != EMPTY){
            return false;
        }

        if (gameField[rowFrom][colFrom] == WHITE) {
            if (rowTo-rowFrom != 1 || Math.abs(colTo-colFrom) != 1){
               return false;
            }
        }else if(gameField[rowFrom][colFrom] == BLACK){
            if (rowFrom-rowTo != 1 || Math.abs(colFrom-colTo) != 1){
               return false;
            }
        }
        return true;
    }
    
    private JSONArray availableJumpes(int row, int col, boolean resume){
        JSONArray result = new JSONArray();
        
        for(int i=2;i<8;i++){
            if(checkJump(row, col, row-(i-1), col-(i-1), row-i, col-i)){
                JSONObject o = new JSONObject();
                o.put("row", row-i);
                o.put("col", col-i);
                result.add(o);
                break;
            }
            if(resume){
                break;
            }
        }
        for(int i=2;i<8;i++){
            if(checkJump(row, col, row-(i-1), col+(i-1), row-i, col+i)){
                JSONObject o = new JSONObject();
                o.put("row", row-i);
                o.put("col", col+i);
                result.add(o);
            }
            if(resume){
                break;
            }
        }
        for(int i=2;i<8;i++){
            if(checkJump(row, col, row+(i-1), col-(i-1), row+i, col-i)){
                JSONObject o = new JSONObject();
                o.put("row", row+i);
                o.put("col", col-i);
                result.add(o);
            }
            if(resume){
                break;
            }
        }
        for(int i=2;i<8;i++){
            if(checkJump(row, col, row+(i-1), col+(i-1), row+i, col+i)){
                JSONObject o = new JSONObject();
                o.put("row", row+i);
                o.put("col", col+i);
                result.add(o);
            }
            if(resume){
                break;
            }
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
            case BLACK_KING:
                for(int i=1;i<8;i++){
                    if(checkMove(row, col, row-i, col-i)){
                        JSONObject o = new JSONObject();
                        o.put("row", row-i);
                        o.put("col", col-i);
                        result.add(o);
                    }else{
                        break;
                    }
                }
                for(int i=1;i<8;i++){
                    if(checkMove(row, col, row-i, col+i)){
                        JSONObject o = new JSONObject();
                        o.put("row", row-i);
                        o.put("col", col+i);
                        result.add(o);
                    }else{
                        break;
                    }
                }
                for(int i=1;i<8;i++){
                    if(checkMove(row, col, row+i, col-i)){
                        JSONObject o = new JSONObject();
                        o.put("row", row+i);
                        o.put("col", col-i);
                        result.add(o);
                    }else{
                        break;
                    }
                }
                for(int i=1;i<8;i++){
                    if(checkMove(row, col, row+i, col+i)){
                        JSONObject o = new JSONObject();
                        o.put("row", row+i);
                        o.put("col", col+i);
                        result.add(o);
                    }else{
                        break;
                    }
                }
                break;
        }
        
        return result;
    }
    
    private boolean checkGameOver(char color){
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                if(gameField[row][col] == color ||
                   gameField[row][col] == getQueenCode(color)){
                    if(availableJumpes(row,col,false).size() > 0){
                        return false;
                    }
                    if(availableMoves(row,col).size() > 0){
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean isJump(int rowFrom, int colFrom, int rowTo, int colTo){
        if(Math.abs(rowFrom-rowTo) > 1){
            int signR = (rowFrom-rowTo)/Math.abs(rowFrom-rowTo);
            int signC = (colFrom-colTo)/Math.abs(colFrom-colTo);

            int rowOver = rowTo + signR;
            int colOver = colTo + signC;
            if(gameField[rowOver][colOver] == getNextGamerColor(currentMove) ||
               gameField[rowOver][colOver] == getQueenCode(getNextGamerColor(currentMove))){
                return true;
            }
        }
        
        return false;
    }

    public JSONObject doMove(int rowFrom, int colFrom, int rowTo, int colTo) {
        JSONObject result = new JSONObject();

        synchronized(this){
            if(currentMove == gameField[rowFrom][colFrom] ||
               getQueenCode(currentMove) == gameField[rowFrom][colFrom]){
                boolean valid = false;
                JSONArray resumeJumps = null;
                if( isJump(rowFrom, colFrom, rowTo, colTo) &&
                   (resumeMoveRow == -1 || 
                    resumeMoveRow == rowFrom && resumeMoveCol == colFrom)){
                    
                    int signR = (rowFrom-rowTo)/Math.abs(rowFrom-rowTo);
                    int signC = (colFrom-colTo)/Math.abs(colFrom-colTo);
                    
                    int rowOver = rowTo + signR;
                    int colOver = colTo + signC;
                    if(checkJump(rowFrom, colFrom, rowOver, colOver, rowTo, colTo)){
                        valid = true;
                        gameField[rowTo][colTo] = gameField[rowFrom][colFrom];
                        gameField[rowOver][colOver] = EMPTY;
                        gameField[rowFrom][colFrom] = EMPTY;
                        
                        resumeJumps = availableJumpes(rowTo, colTo, true);
                    }
                }else{
                    if(resumeMoveRow == -1 && 
                       checkMove(rowFrom, colFrom, rowTo, colTo)){
                        valid = true;
                        gameField[rowTo][colTo] = gameField[rowFrom][colFrom];
                        gameField[rowFrom][colFrom] = EMPTY;
                    }
                }
                if(valid){
                    if(gameField[rowTo][colTo] == WHITE && rowTo == 7 ||
                        gameField[rowTo][colTo] == BLACK && rowTo == 0){
                         gameField[rowTo][colTo] = getQueenCode(gameField[rowTo][colTo]);
                    }
                    
                    if(resumeJumps != null && resumeJumps.size() != 0){
                        resumeMoveRow = rowTo;
                        resumeMoveCol = colTo;
                        result.put("moves", resumeJumps);
                    }else{
                        resumeMoveRow = -1;
                        resumeMoveCol = -1;
                        if(checkGameOver(getNextGamerColor(currentMove))){
                            winnerColor = currentMove;
                            status = GAME_FINISHED;
                        }else{
                            currentMove = getNextGamerColor(currentMove);
                        }
                    }
                    
                    Session s = HibernateSessionManager.getSessionFactory().openSession();
                    s.beginTransaction();
                    s.update(this);
                    
                    Move move = new Move(this, rowFrom, colFrom, rowTo, colTo);
                    s.save(move);
                    
                    s.getTransaction().commit();
                }
            }
        }
        result.put("resumeMoveRow", resumeMoveRow);
        result.put("resumeMoveCol", resumeMoveCol);
        result.put("winnerColor", String.valueOf(winnerColor));
        result.put("gameStatus", status);
        result.put("currentMove", String.valueOf(currentMove));
        result.put("gameField", getgameField());
        return result;
    }
    
    public JSONObject passMove(){
        synchronized(this){
            resumeMoveRow = -1;
            resumeMoveCol = -1;
            if(checkGameOver(getNextGamerColor(currentMove))){
                winnerColor = currentMove;
                status = GAME_FINISHED;
            }else{
                currentMove = getNextGamerColor(currentMove);
            }
            
            Session s = HibernateSessionManager.getSessionFactory().openSession();
            s.beginTransaction();
            s.update(this);        
                   
            s.getTransaction().commit();
        }

        return toJSONString();
    }
}