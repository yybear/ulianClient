/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.net;

import cn.yeahstar.util.ConfigUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author ganqing
 */
public class NetHelper {

    private static final Log logger = LogFactory.getLog(NetHelper.class);
    public static NetHelper helper = new NetHelper();

    private NetHelper() {
    }

    public static NetHelper getInstance() {
        return helper;
    }

    public JsonNode ping(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("accept", "application/json");
        httpGet.setHeader("Content-Type", "application/json;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            Integer statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return parseResponse(response);
            }
        } catch (IOException | IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return null;
    }

    public JsonNode ping() {
        return ping(ConfigUtils.get("server.time.url"));
    }

    private JsonNode parseResponse(HttpResponse response) throws UnsupportedEncodingException, IOException {
        HttpEntity entityRsp = response.getEntity();
        StringBuilder result = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entityRsp.getContent(), HTTP.UTF_8));
        String tempLine = rd.readLine();
        while (tempLine != null) {
            result.append(tempLine);
            tempLine = rd.readLine();
        }
        String resStr = result.toString();
        logger.debug("response is " + resStr);
        
        ObjectMapper m = new ObjectMapper();
        
        return m.readTree(resStr);
    }

    public boolean uploadFile(File file) {
        HttpClient httpClient = new DefaultHttpClient();
        boolean res = false;
        HttpPost post = new HttpPost(ConfigUtils.get("server.upload.url"));
        try {
            // 取用户的信息 以便验证
            MultipartEntity entity = new MultipartEntity();
            entity.addPart("file", new FileBody(file));
            entity.addPart("user", new StringBody("xx"));
            entity.addPart("pw", new StringBody("xx"));
            entity.addPart("code", new StringBody("xx"));
            post.setEntity(entity);

            post.setHeader("accept", "application/json");
            post.setHeader("Content-Type", entity.getContentType().getValue());

            HttpResponse httpResponse = httpClient.execute(post);

            int httpCode = httpResponse.getStatusLine().getStatusCode();
            if (httpCode == HttpStatus.SC_OK) {
                JsonNode json = parseResponse(httpResponse);
                if(json.get("code")!= null && "0".equals(json.get("code").asText())) {
                    res = true;
                }
            }
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            httpClient.getConnectionManager().shutdown();// 最后关掉链接。
        }

        return res;
    }

    public JsonNode post(List<NameValuePair> params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(ConfigUtils.get("server.login.url"));
        post.setHeader("accept", "application/json");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse httpResponse = httpClient.execute(post);

            int httpCode = httpResponse.getStatusLine().getStatusCode();
            if (httpCode == HttpStatus.SC_OK) {
                JsonNode json = parseResponse(httpResponse);
                return json;
            }
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            httpClient.getConnectionManager().shutdown();// 最后关掉链接。
        }
        return null;
    }
    
//    public boolean uploadData(String data) {
//        HttpClient httpClient = new DefaultHttpClient();
//        boolean res = false;
//        HttpPost post = new HttpPost(ConfigUtils.get("server.upload.url"));
//        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        // 取用户的信息 以便验证
//        params.add(new BasicNameValuePair("user", "xx"));
//        params.add(new BasicNameValuePair("pw", "xx"));
//        params.add(new BasicNameValuePair("code", "xx"));
//        StringEntity se;
//        try {
//            se = new StringEntity("data: " + data);
//            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            post.setEntity(se);
//            //post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//            HttpResponse httpResponse = httpClient.execute(post);
//
//            int httpCode = httpResponse.getStatusLine().getStatusCode();
//            if (httpCode == HttpStatus.SC_OK) {
//                JsonNode json = parseResponse(httpResponse);
//                if(json.get("code")!= null && "0".equals(json.get("code").asText())) {
//                    res = true;
//                }
//            }
//        } catch (UnsupportedEncodingException ex) {
//            logger.error(ex.getMessage(), ex);
//        } catch (IOException ex) {
//            logger.error(ex.getMessage(), ex);
//        } finally {
//            httpClient.getConnectionManager().shutdown();// 最后关掉链接。
//        }
//        return res;
//    }
}
