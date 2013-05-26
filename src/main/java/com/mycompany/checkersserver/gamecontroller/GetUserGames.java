package com.mycompany.checkersserver.gamecontroller;

import com.mycompany.checkersserver.GameCache;
import com.mycompany.checkersserver.entity.User;
import org.json.simple.JSONObject;

/**
 *
 * @author tirbycat
 */
class GetUserGames implements GameAction{
    @Override
    public JSONObject run(Long uId, JSONObject param) {
        Long userId = uId;
        if(param.containsKey("userid")){
            userId = (Long)param.get("userid");
        }
        User user = GameCache.getUserById(userId);        

        return user.getGameListJSON();
    }
}