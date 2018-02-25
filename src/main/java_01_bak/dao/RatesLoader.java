package dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.emoji.EmojiParser;
import model.RatesResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class RatesLoader {
    private final String link = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    LinkedList<String> needCurrency = new LinkedList<>();

    public RatesLoader() throws IOException {
        // NEED CURENCIES
        needCurrency.add("USD");
        needCurrency.add("EUR");
        needCurrency.add("CHF");
        needCurrency.add("RUB");
    }
////
    public String getCurrentRates() throws IOException {

        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result="";
        String temp;

        while ((temp = bf.readLine())!=null){
            result+=temp;
        }
////
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<RatesResponse>>(){}.getType();
        LinkedList<RatesResponse> ratesResponses =gson.fromJson(result, type);

        result = EmojiParser.parseToUnicode("Курсы на "+ratesResponses.getFirst().getExchangedate()+"\n-------------------");

        for (int i = 0; i < needCurrency.size(); i++) {
            for (RatesResponse ratesResponse : ratesResponses){
                if (ratesResponse.getCc().equals(needCurrency.get(i))){
                    result+="\n"+emojiLoader(ratesResponse.getCc())+ratesResponse.getCc()+" = "+ ratesResponse.getRate();
                }
            }
        }

        return result;


    }
    public String getRatesOnDate(Date date) throws IOException {

        SimpleDateFormat nbuFormat = new SimpleDateFormat("yyyyMMdd");
        String date_request = nbuFormat.format(date).toString();

        URL url = new URL(link+"&date="+date_request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result="";
        String temp;

        while ((temp = bf.readLine())!=null){
            result+=temp;
        }

        if (result.length() < 10) throw  new IOException( "Курсы не найдены. JSON = "+result);

        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<RatesResponse>>(){}.getType();
        LinkedList<RatesResponse> ratesResponses =gson.fromJson(result, type);

        result = "Курсы на "+ratesResponses.getFirst().getExchangedate()+"\n-------------------";

        for (int i = 0; i < needCurrency.size(); i++) {
            for (RatesResponse ratesResponse : ratesResponses){
                if (ratesResponse.getCc().equals(needCurrency.get(i))){
                    result+="\n"+emojiLoader(ratesResponse.getCc())+ratesResponse.getCc()+" = "+ ratesResponse.getRate();
                }
            }
        }


        return result;
    }

    private String emojiLoader(String cc){
        if (cc.equals("USD")){return "\uD83C\uDDFA\uD83C\uDDF8";}
        if (cc.equals("EUR")){return "\uD83C\uDDEA\uD83C\uDDFA";}
        if (cc.equals("RUB")){return "\uD83C\uDDF7\uD83C\uDDFA";}
        if (cc.equals("CHF")){return "\uD83C\uDDE8\uD83C\uDDED";}
        return "";
    }
}
