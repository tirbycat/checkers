package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;

/**
 *
 * @author tirbycat
 */
class GetGame implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) {
        Long id = (Long)param.get("gameid");
        Game game = GameCache.getGameById(id);
                
        return game.toJSONString();
    }
}
