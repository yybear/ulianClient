/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.jdbc;

import com.mchange.v2.c3p0.DataSources;
import cn.yeahstar.ex.DBException;
import cn.yeahstar.util.ConfigUtils;
import cn.yeahstar.util.Contants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author ganqing
 */
public class DBManager {

    private final static Log LOGGER = LogFactory.getLog(DBManager.class);
    private static DataSource unpooled;
    private static DataSource pooled;
    private static final String DATABASE_DRIVER = "org.sqlite.JDBC";

    static {
        try {
            Class.forName(DATABASE_DRIVER);
            SQLiteConfig config = new SQLiteConfig();
            //config.setEncoding(SQLiteConfig.Encoding.UTF8);
            //unpooled = DataSources.unpooledDataSource(ConfigUtils.get("db.url"), config.toProperties());
            String url = String.format(ConfigUtils.get("db.url"), ConfigUtils.getRootDir());
            LOGGER.debug("sqlite database url is " + url);
            unpooled = DataSources.unpooledDataSource(url);
            pooled = DataSources.pooledDataSource(unpooled);
        } catch (ClassNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static void initDB() {
        try {
            InputStream input = DBManager.class.getResourceAsStream("/sql/ulian.sql");
            /*String sqls = IOUtils.toString(input, "UTF-8");

             String[] scripts = StringUtils.split(sqls, ";");*/
            importSQL(input);
            /*Statement stmt = connection.createStatement();
             try {
             for (String script : scripts) {
             LOGGER.debug(script);
             stmt.execute(script);
             }
             } catch (SQLException ex) {
             LOGGER.error(ex.getMessage(), ex);
             } finally {
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(connection);
             }*/
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 更新一个目录下所有的sql脚本
     *
     * @param dir
     */
    public static void updateSQLs(String dir) {
        LOGGER.debug("exec sql file, dir is: " + dir);
        File sqlDir = new File(dir);
        if (sqlDir.exists() && sqlDir.isDirectory()) {
            Iterator it = FileUtils.iterateFiles(sqlDir, new String[]{"sql"}, false);
            while (it.hasNext()) {
                File sqlFile = (File) it.next();
                updateSQL(sqlFile);
            }
        }
    }

    /**
     * 更新单个sql脚本
     * @param file 
     */
    public static void updateSQL(File file) {
        try {
            if(file.exists()) {
                InputStream in = new FileInputStream(file);
                importSQL(in);
            }
        } catch (FileNotFoundException | SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private static void importSQL(InputStream in) throws SQLException {
        Scanner s = new Scanner(in, Contants.UTF8);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        Connection conn = getConnection();
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if ((line.startsWith("/*!") || line.startsWith("/*")) && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                } else if (line.startsWith("--")) {
                    continue;
                }
                LOGGER.debug("sql is " + line);
                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } finally {
            DbUtils.closeQuietly(st);
            DbUtils.closeQuietly(conn);
        }
    }

    public static Connection getConnection() {
        try {
            return pooled.getConnection();
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DBException("get connection error!");
        }
    }

    public static QueryRunner getQueryRunner() {
        return new QueryRunner(pooled);
    }
}
