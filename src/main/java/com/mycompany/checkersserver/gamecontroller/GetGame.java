package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.Game;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
class GetGame implements GameAction{

    @Override
    public JSONObject run(Long uId, JSONObject param) throws ParseException {
        Long id = (Long)param.get("gameid");
        Game game = GameCache.getGameById(id);
        if(game == null){
            throw new ParseException(0);
        }
        JSONObject gameJson = game.toJSONString();
        if(game.getUser2() != null){
            if(game.getUser2().getId().equals(uId)){
                gameJson.put("opponent", game.getUser1().getLogin());
            }else{
                gameJson.put("opponent", game.getUser2().getLogin());
            }
        }
        return gameJson;
    }
}
