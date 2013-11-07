/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.task;

import cn.yeahstar.dao.impl.CommonDao;
import cn.yeahstar.dao.impl.DaoFactory;
import cn.yeahstar.net.NetHelper;
import cn.yeahstar.util.ConfigUtils;
import cn.yeahstar.util.Contants;
import cn.yeahstar.util.JavaAppUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

/**
 *
 * @author ganqing
 */
public class UploadDataTask extends TimerTask {

    private static final Log logger = LogFactory.getLog(UploadDataTask.class);
    private NetHelper netHelper;
    private CommonDao dao;
    private Pattern pattern;

    public UploadDataTask() {
        this.netHelper = NetHelper.getInstance();
        dao = DaoFactory.getInstance().getCommonDao();
        pattern = Pattern.compile("[0-9]+");
    }

    @Override
    public void run() {
        logger.debug("UploadDataTask run");
        JsonNode json = netHelper.ping();
        if (null == json) {
            logger.debug("net ping failed");
            return;
        }

        // 读取需要提交的数据 并且压缩zip
        Long current = System.currentTimeMillis() / 1000;
        Map<String, Object> result = dao.query("select value from ulian_env_settings where key=?", Contants.LAST_UPLOAD_TIME);

        List quizzes;
        List points;
        List log;
        List tests;
       
        if (MapUtils.isEmpty(result)) {
            logger.debug("first time upload data");
            // 第一次上报数据
            quizzes = dao.list("select * from ulian_favorite_quizzes where timestamp<=?", current);
            points = dao.list("select * from ulian_user_points where timestamp<=?", current);
            log = dao.list("select * from ulian_log where timestamp<=?", current);
            tests = dao.list("select * from ulian_tests where timestamp<=?", current);
        } else {
            Long time = (Long) result.get("value");
            logger.debug("last upload data time is " + time);
            quizzes = dao.list("select * from ulian_favorite_quizzes where timestamp>? and timestamp<=?", time, current);
            points = dao.list("select * from ulian_user_points where timestamp>? and timestamp<=?", time, current);
            log = dao.list("select * from ulian_log where timestamp>? and timestamp<=?", time, current);
            tests = dao.list("select * from ulian_tests where timestamp>? and timestamp<=?", time, current);
        }

        StringBuilder sb = new StringBuilder("{");
        sb.append(createJsonData("ulian_favorite_quizzes", quizzes, false));
        sb.append(",");
        sb.append(createJsonData("ulian_user_points", points, false));
        sb.append(",");
        sb.append(createJsonData("ulian_log", log, false));
        sb.append(",");
        sb.append(createTestData(tests));
        sb.append("}");

        // 上报 上报成功后env 设置上报的时间点
        String data = sb.toString();
        logger.debug(data);
        if (StringUtils.isNotBlank(data)) {
            File temp = new File(ConfigUtils.getTempDir() +  File.separator + "data" + current);
            File tempZip = new File(ConfigUtils.getTempDir() + File.separator + "dataZip" + current + ".zip");
            boolean success = true;
            try {
                FileUtils.writeStringToFile(temp, data, Contants.UTF8);
                zip(tempZip, temp);
            } catch (Exception ex) {
                success = false;
            }
            if (success) {
                boolean res = netHelper.uploadFile(tempZip);
                if (res == true) {
                    logger.debug("upload data success");
                    // 成功了
                    if (MapUtils.isEmpty(result)) {
                        dao.update("insert into ulian_env_settings (key, value) values(?, ?)", Contants.LAST_UPLOAD_TIME, current.toString());
                    } else {
                        dao.update("update ulian_env_settings set value=? where key=?", current.toString(), Contants.LAST_UPLOAD_TIME);
                    }
                }
            }
            try {
                FileUtils.forceDeleteOnExit(temp);
                FileUtils.forceDeleteOnExit(tempZip);
            } catch (IOException ex) {
            }
        }
    }

    private void zip(File zipFile, File inputFile) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFile));
        BufferedOutputStream bo = new BufferedOutputStream(out);
        try {
            zip(out, inputFile, inputFile.getName(), bo);
        } catch (IOException ex) {
            throw new Exception(ex);
        } finally {
            try {
                bo.close();
                out.close();
            } catch (IOException ex) {
            }
        }
    }

    private void zip(ZipOutputStream out, File f, String base,
            BufferedOutputStream bo) throws Exception {
        FileInputStream in = new FileInputStream(f);
        BufferedInputStream bi = new BufferedInputStream(in);
        int b;
        try {
            out.putNextEntry(new ZipEntry(base)); // 创建zip压缩进入点base  
            while ((b = bi.read()) != -1) {
                bo.write(b); // 将字节流写入当前zip目录  
            }
        } catch (IOException ex) {
            throw new Exception(ex);
        } finally {
            try {
                bi.close();
                in.close(); // 输入流关闭  
            } catch (IOException ex) {
            }
        }

    }

    private String createTestData(List tests) {
        int size = tests.size();
        if(logger.isDebugEnabled())
            logger.debug(String.format("table ulian_tests, data size is %s", size));
        StringBuilder sb = new StringBuilder(getField("ulian_tests")).append(":[");

        for (int i = 0; i < size; i++) {
            sb.append("{");
            if (i > 0) {
                sb.append(",");
            }
            Map<String, Object> map = (Map<String, Object>) tests.get(i);
            int j = 0;
            for (String key : map.keySet()) {
                if ("id".equals(key)) {
                    continue;
                }

                String value = map.get(key).toString();
                if (StringUtils.isNotBlank(value)) {
                    if (j > 0) {
                        sb.append(",");
                    }
                    sb.append(getField(key)).append(":").append(getField(value));
                    j++;
                }
            }
            Long id = Long.valueOf(map.get("id").toString());
            // 与知识点的关系
            List knowledgeTests = dao.list("select knowledge_id from ulian_knowledge_tests where test_id=?", id);
            sb.append(",").append(createJsonData("ulian_knowledge_tests", knowledgeTests, true));

            // 测试详情与知识点的关系
            List details = dao.list("select * from ulian_test_details where test_id=?", id);
            sb.append(",").append(createJsonData("ulian_test_details", details, false));

            // 测试详情与知识点的关系
            List answers = dao.list("select * from ulian_test_answers where test_id=?", id);
            sb.append(",").append(createJsonData("ulian_test_answers", answers, false));
            
            sb.append("}");
        }
        sb.append("]");
        String str = sb.toString();
        logger.debug("data json is " + str);
        return str;
    }

    private String createJsonData(String objectName, List datas, boolean justValue) {
        int size = datas.size();
        StringBuilder sb = new StringBuilder();
        sb.append(getField(objectName)).append(":[");
        if(logger.isDebugEnabled())
            logger.debug(String.format("table %s, data size is %s", objectName, size));
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (!justValue)
                sb.append("{");
            Map<String, Object> map = (Map<String, Object>) datas.get(i);
            
            int j = 0;
            for (String key : map.keySet()) {
                if ("id".equals(key) || "test_id".equals(key)) {
                    continue;
                }

                String value = "";
                if(map.get(key) != null)
                    value = map.get(key).toString();
                if (StringUtils.isNotBlank(value)) {
                    if (j > 0) {
                        sb.append(",");
                    }
                    if (justValue) {
                        sb.append(getField(value));
                    } else {
                        sb.append(getField(key)).append(":").append(getField(value));
                    }
                    j++;
                }
            }
            if (!justValue)
                sb.append("}");
        }
        sb.append("]");
        String str = sb.toString();
        logger.debug("data json is " + str);
        return str;
    }

    private String getField(String name) {
        Matcher match = pattern.matcher(name);
        if (match.matches()) {
            return name;
        }
        return "'" + name + "'";
    }
}
