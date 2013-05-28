package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import com.mycompany.checkersserver.entity.User;
import org.json.simple.JSONObject;

/**
 *
 * @author tirbycat
 */
class JoinTheGame implements GameAction{
    @Override
    public JSONObject run(Long uId, JSONObject param) {
        Long id = (Long)param.get("gameid");
        Game game = GameCache.getGameById(id);
        User user = GameCache.getUserById(uId);
        
        synchronized(this){
            if(game.getUser2() == null){
                user.joinToGame(game);
            }
        }
        return game.toJSONString();
    }
}
