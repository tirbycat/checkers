package com.mycompany.checkersserver.restservices;

import com.mycompany.checkersserver.exceptions.AuthException;
import com.mycompany.checkersserver.gamecontroller.GameController;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tirbycat
 */
public class CheckersRESTService extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(CheckersRESTService.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        JSONParser jsonParser = new JSONParser();
        
        PrintWriter out = response.getWriter();
        try {
            if(session.getAttribute("userid") == null){
                throw new AuthException("");
            }
            JSONObject jsonRequest = (JSONObject) jsonParser.parse(request.getParameter("json"));
            String action = (String)jsonRequest.get("action");
            
            JSONObject parameters = (JSONObject)jsonRequest.get("parameters");
            JSONObject req = GameController.runGameAction((Long)session.getAttribute("userid"), action, parameters);
            
            out.println(req.toJSONString());
        }catch(ParseException ex){
            response.setStatus(400);
            log.error("bad api request", ex);
        }catch(AuthException ex){
            response.setStatus(401);
            log.error("non authirized request", ex);
        }catch(Exception ex){
            response.setStatus(500);
            log.error("server error", ex);
        } finally {            
            out.close();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Checkers REST API Server";
    }
}
