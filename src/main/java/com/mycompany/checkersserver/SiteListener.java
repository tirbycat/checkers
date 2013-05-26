package com.mycompany.checkersserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tirbycat
 */
public class SiteListener  implements ServletContextListener
{
    private static final Logger log = LoggerFactory.getLogger(SiteListener.class);
    
    public static String appName = "checkers";

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        log.info(appName + " initialisation begin...");
        HibernateSessionManager.init();
        
        log.info(appName + " started");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        log.info(appName + " shutting down...");
    }
}
