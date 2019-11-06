package co.com.bpmsconnector.service;

import co.com.bpmsconnector.entity.BonitaObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.util.List;
import java.util.Properties;

@Service
public class OrderService {

    @Autowired
    RestTemplate restTemplate;

    public HttpStatus instanceBPMS(){
        try {
            Properties prop = getPropertiesFile();
            BonitaObject b = getBPMSToken(prop);
            String idProcess = null;
            if (b != null){
                idProcess = getIdProcess(b);
            }
            instanceBPMSProcess(b, idProcess);
            return HttpStatus.ACCEPTED;
        } catch (Exception e) {
           return HttpStatus.BAD_REQUEST;
        }
    }

    public BonitaObject getBPMSToken(Properties prop){
        BonitaObject b = new BonitaObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            String url = prop.getProperty("HOST") + ":"+ prop.getProperty("PORT")+"/bonita/loginservice?username="+prop.getProperty("USERNAME")+"&password="+ prop.getProperty("PASSWORD")+"&redirect=false";
            List<String> cooks = restTemplate.exchange(url, HttpMethod.GET, entity,String.class).getHeaders().get("Set-Cookie");
            if (cooks != null && !cooks.isEmpty()) {
                for(String cook : cooks){
                    List<HttpCookie> httpCookies = HttpCookie.parse(cook);
                    for (HttpCookie httpCookie : httpCookies) {
                        if(httpCookie.getName().equalsIgnoreCase("X-Bonita-API-Token")){
                            b.setToken(httpCookie.getValue());
                        } else if (httpCookie.getName().equalsIgnoreCase("JSESSIONID")) {
                            b.setjSessionId(httpCookie.getValue());
                        }
                    }
                }
            }
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getIdProcess(BonitaObject b){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Bonita-API-Token",b.getToken());
            headers.set("Cookie","JSESSIONID=" + b.getjSessionId());
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<String> result = restTemplate.exchange("http://localhost:9471/bonita/API/bpm/process?s=Process", HttpMethod.GET, requestEntity, String.class);
            String responseStr = result.getBody();
            responseStr = responseStr.substring( responseStr.indexOf("{"), responseStr.lastIndexOf("}") + 1);
            JsonObject jsonObject = new JsonParser().parse(responseStr).getAsJsonObject();
            return jsonObject.get("id").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void instanceBPMSProcess(BonitaObject b, String idProcess) {
       try {
           HttpHeaders headers = new HttpHeaders();
           headers.set("X-Bonita-API-Token",b.getToken());
           headers.set("Cookie","JSESSIONID=" + b.getjSessionId());
           HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
           ResponseEntity<String> result = restTemplate.exchange("http://localhost:9471/bonita/API/bpm/process/" + idProcess + "/instantiation", HttpMethod.POST, requestEntity, String.class);
       } catch (Exception e) {
            e.printStackTrace();
       }
    }

    public Properties getPropertiesFile(){
        InputStream inputStream;
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            return prop;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
