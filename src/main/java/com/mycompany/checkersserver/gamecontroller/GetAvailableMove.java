package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
class GetAvailableMove implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) throws ParseException {
        Long id = (Long)param.get("gameid");
        int row = (int)(long)(Long)param.get("row");
        int col = (int)(long)(Long)param.get("col");
        Game game = GameCache.getGameById(id);
        if(game == null){
            throw new ParseException(0);
        }
        JSONObject gameJson = game.getAvailableMove(row, col);
        
        return gameJson;
    }
}