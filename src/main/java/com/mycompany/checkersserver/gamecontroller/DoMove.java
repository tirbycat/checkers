package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
class DoMove implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) throws ParseException {
        Long id = (Long)param.get("gameid");
        int rowFrom = (int)(long)(Long)param.get("rowFrom");
        int colFrom = (int)(long)(Long)param.get("colFrom");
        int rowTo = (int)(long)(Long)param.get("rowTo");
        int colTo = (int)(long)(Long)param.get("colTo");
        
        Game game = GameCache.getGameById(id);
        if(game == null){
            throw new ParseException(0);
        }
        JSONObject gameJson = game.doMove(rowFrom, colFrom, rowTo, colTo);
        
        return gameJson;
    }
}
