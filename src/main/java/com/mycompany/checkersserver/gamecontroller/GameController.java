package com.mycompany.checkersserver.gamecontroller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
public class GameController {
    
    private static final Map<String,GameAction> factoryMap =
    Collections.unmodifiableMap(new HashMap<String,GameAction>() {{
        put("getusergames", new GetUserGames());
        put("getgame", new GetGame());
        put("newgame", new CreateNewGame());
        put("jointhegame", new JoinTheGame());
        put("getavailablemove", new GetAvailableMove());
        put("domove", new DoMove());
        put("passmove", new PassMove());
    }});

    
    public static JSONObject runGameAction(Long userId, String actionType, JSONObject params) throws ParseException {
        GameAction action = factoryMap.get(actionType);
        if (action == null) {
            throw new ParseException(0);
        }
        return action.run(userId, params);
    }
}
