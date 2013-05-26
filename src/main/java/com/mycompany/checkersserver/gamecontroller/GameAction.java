package com.mycompany.checkersserver.gamecontroller;

import org.json.simple.JSONObject;

/**
 *
 * @author tirbycat
 */
public interface GameAction {
    public JSONObject run(Long uId, JSONObject param);
}
