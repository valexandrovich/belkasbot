package modules.bankrupts;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankruptsRequest {

    /* Данный класс возвращает объект BankruptResponse с ответом от сервера */

    private final String link= "https://ovsb.ics.gov.ua/view/vgsu/bank_content.php";
    private Map<String, String> requestBody = new HashMap<String, String>();
    private String iDisplayStart = "0";
    private String iDisplayLength = "10";
    private String pdate="~";
    private String code = "";


    public void setiDisplayStart(String iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public String generateBody(){
        String requestBodysString = "sEcho=4&iColumns=9&sColumns=&iDisplayStart=0&iDisplayLength=10&mDataProp_0=0&mDataProp_1=1&mDataProp_2=2&mDataProp_3=3&mDataProp_4=4&mDataProp_5=5&mDataProp_6=6&mDataProp_7=7&mDataProp_8=8&sSearch=&bRegex=false&sSearch_0=&bRegex_0=false&bSearchable_0=true&sSearch_1=&bRegex_1=false&bSearchable_1=true&sSearch_2=&bRegex_2=false&bSearchable_2=true&sSearch_3=&bRegex_3=false&bSearchable_3=true&sSearch_4=&bRegex_4=false&bSearchable_4=true&sSearch_5=&bRegex_5=false&bSearchable_5=true&sSearch_6=&bRegex_6=false&bSearchable_6=true&sSearch_7=&bRegex_7=false&bSearchable_7=false&sSearch_8=&bRegex_8=false&bSearchable_8=false&regdate=~&sSearch_3=&sSearch_4=&q_ver=arbitr&aucdate=~&aucplace=&ptype=0&pkind=0&price=~&ckind=";
        requestBody.put("iDisplayStart", iDisplayStart);
        requestBody.put("iDisplayLength", iDisplayLength);
        requestBody.put("pdate", pdate);
        requestBody.put("code", code);
        for (Map.Entry<String, String> entry : requestBody.entrySet()){
            requestBodysString+="&"+entry.getKey()+"="+entry.getValue();
        }
        return requestBodysString;
    }

    public BankruptsResponse findByCode(String code) throws IOException {
        this.code = code;

        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setRequestProperty("Referer", "https//ovsb.ics.gov.ua/view/vgsu/bankrut.php");
        // Генерируем и записываем в соединение тело запроса
        byte[] params = generateBody().getBytes();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params);
        out.flush();
        out.close();
        // Получаем ответ на запрос
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String temp;
        String result = "";
        while ((temp = bf.readLine())!=null){
            result+=temp;
        }

        // Конвертируем ответ в тип BankruptsResponse
        Gson gson = new Gson();
        BankruptsResponse bankruptsResponse = gson.fromJson( result.replace("<br \\/>", "\", \""), BankruptsResponse.class);


        // Удаляем пробелы в начале и в конце строк
        for (List<String> row : bankruptsResponse.aaData){
            for(String cell : row){
                cell = cell.trim();
            }
        }


        return bankruptsResponse;

    }

    public BankruptsResponse findByDate(String from, String to) throws IOException {
        this.pdate = from+"~"+to;

        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setRequestProperty("Referer", "https//ovsb.ics.gov.ua/view/vgsu/bankrut.php");
        // Генерируем и записываем в соединение тело запроса
        byte[] params = generateBody().getBytes();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params);
        out.flush();
        out.close();
        // Получаем ответ на запрос
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String temp;
        String result = "";
        while ((temp = bf.readLine())!=null){
            result+=temp;
        }

        // Конвертируем ответ в тип BankruptsResponse
        Gson gson = new Gson();
        BankruptsResponse bankruptsResponse = gson.fromJson( result.replace("<br \\/>", "\", \""), BankruptsResponse.class);


        // Удаляем пробелы в начале и в конце строк
        for (List<String> row : bankruptsResponse.aaData){
            for(String cell : row){
                cell = cell.trim();
            }
        }


        return bankruptsResponse;

    }



}
