package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
class PassMove  implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) throws ParseException {
        Long id = (Long)param.get("gameid");
        
        Game game = GameCache.getGameById(id);
        if(game == null){
            throw new ParseException(0);
        }
        JSONObject gameJson = game.passMove();
        
        return gameJson;
    }
}
