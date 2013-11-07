/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.ui;

import cn.yeahstar.dao.UlianUsersDao;
import cn.yeahstar.dao.impl.CommonDao;
import cn.yeahstar.dao.impl.DaoFactory;
import cn.yeahstar.ex.ErrorCode;
import cn.yeahstar.jdbc.DBManager;
import cn.yeahstar.jdbc.DBUtilsTransactionHelper;
import cn.yeahstar.model.Ulian_Users;
import cn.yeahstar.net.NetHelper;
import cn.yeahstar.util.ConfigUtils;
import static cn.yeahstar.util.Contants.*;
import cn.yeahstar.util.JavaAppUtils;
import javafx.scene.web.WebEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static cn.yeahstar.util.Contants.*;
import cn.yeahstar.util.DataUpdaterUtils;
import cn.yeahstar.util.MD5Utils;
import cn.yeahstar.util.MessageUtils;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;


/**
 *
 * @author ganqing
 */
public class JavaApp {
    private static final Log logger = LogFactory.getLog(JavaApp.class);
    private WebEngine webEngine;
    private CommonDao commonDao;
    private UlianUsersDao userDao;
    private NetHelper netHelper;
    
    public JavaApp(WebEngine webEngine) {
        this.webEngine = webEngine;
        commonDao = DaoFactory.getInstance().getCommonDao();
        userDao = DaoFactory.getInstance().getUlianUsersDao();
        netHelper = NetHelper.getInstance();
    }
    
    public void initDB() {
        DBManager.initDB();
    }
    
    public boolean needUpdateProgram() {
        String latestPVersion = ConfigUtils.getUlianConfig(P_VERSION);
        return needUpdate(latestPVersion, P_VERSION);
    }
    
    public boolean needUpdateData() {
        String latestDVersion = ConfigUtils.getUlianConfig(D_VERSION);
        return needUpdate(latestDVersion, D_VERSION);
    }
    
    private boolean needUpdate(String latestVersion, String key) {
        boolean result = false;
        try {
            DBUtilsTransactionHelper.startTransacion();
            Connection conn = DBUtilsTransactionHelper.getConnetion();
            String sql = "select value from ulian_env_settings where key=?";
            Map<String, Object> map = commonDao.query(conn, sql, key);
            if(MapUtils.isEmpty(map)) {
                logger.debug("ulian_env_settings doesn't have " + key);
                // 系统中为空则保存
                commonDao.update(conn, "insert into ulian_env_settings (key, value) values (?, ?)", key, latestVersion);
            } else {
                String value = map.get("value").toString();
                logger.debug(String.format("ulian_env_settings %s is %s, latest is %s", key, value, latestVersion));
                if(!latestVersion.equals(value)) {
                    result = true;
                }
            }
            DBUtilsTransactionHelper.commit();
        } catch(Exception e) {
            logger.error("exception roll back", e);
            DBUtilsTransactionHelper.rollback();
        } finally {
            DBUtilsTransactionHelper.close();
        }
        
        return result;
    }
    
    /**
     * 新版本第一次启动时需要更新数据结构和相关数据
     */
    public void updater() {
        if(needUpdateProgram()) {
            logger.debug("update sql");
            String version = ConfigUtils.getUlianConfig(P_VERSION);
            String sqlFile = ConfigUtils.getRootDir() + File.separator + "bin" 
                    + File.separator + ConfigUtils.getCurrentPVersionDir() + File.separator + ConfigUtils.get("updater.dir") 
                    + File.separator + ConfigUtils.get("updater.sql");
            DBManager.updateSQL(new File(sqlFile));
            commonDao.update("update ulian_env_settings set value=? where key=?", version, P_VERSION);
        }
        
        if(needUpdateData()) {
            logger.debug("update file");
            String version = ConfigUtils.getUlianConfig(D_VERSION);
            DataUpdaterUtils.update(version);
            commonDao.update("update ulian_env_settings set value=? where key=?", version, D_VERSION);
        }
        
        // 更新完跳转到
        String page = JavaAppUtils.getPageContent(ConfigUtils.get("home.page"));
        webEngine.loadContent(page);
    }
    
    /**
     * 查看客户端是否有账号了。
     * @return 
     */
    public boolean hasAccount() {
        int count = commonDao.count("select count(*) from ulian_users");
        if(count > 0)
            return true;
        else 
            return false;
    }
    
    /**
     * 用户登录
     * @param email
     * @param passwd
     */
    public String login(String email, String passwd) {
        if(netHelper.ping() != null) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", passwd));
            
            JsonNode json = netHelper.post(params);
            return json.toString();
        } else { // 客户端验证
            Map<String, Object> res = commonDao.query("select password from ulian_users where email=?", email);
            if(MapUtils.isEmpty(res)) {
                return jsonMessage(ErrorCode.USER_ERR_NOT_EXSIT);
            }
            if(!res.get("password").equals(MD5Utils.getMD5Str(passwd))) {
                return jsonMessage(ErrorCode.USER_ERR_PASSWD_WRONG);
            }
            return jsonMessage(ErrorCode.OK);
        }
    }

    private static final String jsonMessage = "{'res':'%s', 'msg':'%s'}";
    private String jsonMessage(int code) {
        if(code == ErrorCode.OK)
            return String.format(jsonMessage, code, "");
        else 
            return String.format(jsonMessage, code, MessageUtils.getErrorMessage(code));
    }
    
    public String insertTest() {
        userDao.update("update ulian_users set activation_time=?, activation_code=? where id=?", 1373354629, "234234", 1);
        //Ulian_Users user = userDao.get(1l);
        Ulian_Users user2 = commonDao.queryClass(null, "select id, email from ulian_users where id=?", Ulian_Users.class, 1);
        logger.debug("code is " + user2.getEmail());
        //JsonNode obj = netHelper.login("hhh@sdd.com", "123456");
        //logger.debug("here json " + obj.get("code"));
        return user2.getEmail();
    }
}