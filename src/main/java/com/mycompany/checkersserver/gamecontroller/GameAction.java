package com.mycompany.checkersserver.gamecontroller;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tirbycat
 */
public interface GameAction {
    public JSONObject run(Long uId, JSONObject param) throws ParseException;
}
