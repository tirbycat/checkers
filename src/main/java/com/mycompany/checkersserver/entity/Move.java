package com.mycompany.checkersserver.entity;

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

/**
 *
 * @author pzigel
 */
@Entity
@Table(name="moves")
public class Move implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;
    
    @ManyToOne(targetEntity=Game.class)
    @JoinColumn(name="game_id", nullable=false)
    private Game game;
    
    @Column(name="move_date", columnDefinition = "timestamp without time zone default now()")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date moveDate;
    
    private Integer fromRow;
    private Integer fromCol;
    private Integer ToRow;
    private Integer ToCol;

    public Move() {
    }

    public Move(Game game, Integer fromRow, Integer fromCol, Integer ToRow, Integer ToCol) {
        this.game = game;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.ToRow = ToRow;
        this.ToCol = ToCol;
        moveDate = new Date();
    }
}
