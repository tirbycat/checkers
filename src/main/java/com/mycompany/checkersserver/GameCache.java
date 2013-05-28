package com.mycompany.checkersserver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mycompany.checkersserver.entity.Game;
import com.mycompany.checkersserver.entity.User;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tirbycat
 */
public class GameCache {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GameCache.class);
    
    private static LoadingCache<Long, Game> activeGames = CacheBuilder.newBuilder()
    .concurrencyLevel(4)
    .weakKeys()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .removalListener(new RemovalListener<Long, Game>(){
        @Override
        public void onRemoval(RemovalNotification<Long, Game> rn) {
            //rn.getValue().closeSession();
        }
    })
    .build(
        new CacheLoader<Long, Game>() {
            @Override
          public Game load(Long key){
            return Game.getById(key);
          }
        });

    private static LoadingCache<Long, User> activeUsers = CacheBuilder.newBuilder()
    .concurrencyLevel(4)
    .weakKeys()
    .maximumSize(10000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .removalListener(new RemovalListener<Long, User>(){
        @Override
        public void onRemoval(RemovalNotification<Long, User> rn) {
            //rn.getValue().closeSession();
        }
    })
    .build(
        new CacheLoader<Long, User>() {
            @Override
          public User load(Long key){
            return User.getById(key);
          }
        });
    

    public static Game createNewGame(Long userid){
        User user = activeUsers.getIfPresent(userid);
        Game g = user.createGame();

        activeGames.put(g.getId(), g);
        return g;
    }
    
    public static Game getGameById(Long id){
        try{
            return activeGames.get(id);
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }
    
    public static User createNewUser(String login, String password){
        User u = User.create(login, password);
        activeUsers.put(u.getId(), u);
        return u;
    }
    
    public static User getUserById(Long id){
        try {
            return activeUsers.get(id);
        } catch (ExecutionException ex) {
            log.error("", ex);
            return null;
        }
    }
}