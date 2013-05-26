package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;

/**
 *
 * @author tirbycat
 */
public class CreateNewGame implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) {
        JSONObject result = new JSONObject();
        Game game = GameCache.createNewGame(uId);
        
        result.put("gameid", game.getId());
        return result;
    }
}
