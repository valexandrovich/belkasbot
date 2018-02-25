package modules.rates;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import services.LocalizationService;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class RatesService {
    private static  final String link = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private static final SimpleDateFormat nbuFormat = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");
    final static  String username = "belkabot.tg@gmail.com";
    final static  String password = "dfkthf2301";
    private static final Object lock = new Object();

    public static Map<Integer, String> currencyPriority = new HashMap<>();


    static {
        currencyPriority.put(0, "USD");
        currencyPriority.put(1, "EUR");
        currencyPriority.put(2, "CHF");
        currencyPriority.put(3, "GBP");
        currencyPriority.put(4, "RUB");
        currencyPriority.put(5, "PLN");
        currencyPriority.put(6, "HUF");
        currencyPriority.put(7, "CZK");
        currencyPriority.put(8, "KZT");
        currencyPriority.put(9, "HRK");
        currencyPriority.put(10, "JPY");
        currencyPriority.put(11, "BYN");
        currencyPriority.put(12, "AUD");
        currencyPriority.put(13, "THB");
        currencyPriority.put(14, "BGN");
        currencyPriority.put(15, "KRW");
        currencyPriority.put(16, "HKD");
        currencyPriority.put(17, "DKK");
        currencyPriority.put(18, "EGP");
        currencyPriority.put(19, "INR");
        currencyPriority.put(20, "IRR");
        currencyPriority.put(21, "CAD");
        currencyPriority.put(22, "MXN");
        currencyPriority.put(23, "MDL");
        currencyPriority.put(24, "ILS");
        currencyPriority.put(25, "NZD");
        currencyPriority.put(26, "NOK");
        currencyPriority.put(27, "RON");
        currencyPriority.put(28, "IDR");
        currencyPriority.put(29, "SGD");
        currencyPriority.put(30, "XDR");
        currencyPriority.put(31, "TRY");
        currencyPriority.put(32, "SEK");
        currencyPriority.put(33, "CNY");
    }

    private  static LinkedList<RatesResponse> getRatesBase(Date date) throws IOException {
        synchronized (lock) {
            URL url;
            HttpURLConnection connection;
            String dateRequest = nbuFormat.format(date);

            url = new URL(link + "&date=" + dateRequest);
            connection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = "";
            String tempBuffer;

            while ((tempBuffer = bufferedReader.readLine()) != null) {
                response += tempBuffer;
            }

            if (response.length() < 10) throw new IOException("Курсы не найдены. JSON = " + response);
            Gson gson = new Gson();
            Type type = new TypeToken<LinkedList<RatesResponse>>() {
            }.getType();
            LinkedList<RatesResponse> ratesResponses = gson.fromJson(response, type);
            return ratesResponses;
        }
    }

    public static String getRatesMessage (Date date, int userID) throws IOException {
        ArrayList<String> userCurrencies = RatesDB.getUserCurrencies(userID);
        LinkedList<RatesResponse> responseList = getRatesBase(date);
        String answer =  "      "+ LocalizationService.getString("1_RatesOn", userID) +"  "+ shortDate.format(date)
                +"\n" + new String(new char[28]).replace('\0', '¯');

        if (responseList.size()<1) {throw new IOException(LocalizationService.getString("1_NoRates", userID));}

        for (RatesResponse response : responseList){
            if (userCurrencies.contains(response.getCc())){
                answer+="\n"+getFlag(response.getCc())+" "+response.getCc()+ " : " + response.getRate();
            }
        }

        return answer;


    }

    private static String getFlag(String ccy) {
        switch (ccy) {
            case "USD":
                return "\uD83C\uDDFA\uD83C\uDDF8";
            case "EUR":
                return "\uD83C\uDDEA\uD83C\uDDFA";
            case "CHF":
                return "\uD83C\uDDE8\uD83C\uDDED";
            case "RUB":
                return "\uD83C\uDDF7\uD83C\uDDFA";
            case "GBP":
                return "\uD83C\uDDEC\uD83C\uDDE7";
            case "PLN":
                return "\uD83C\uDDF5\uD83C\uDDF1";
            case "HUF":
                return "\uD83C\uDDED\uD83C\uDDFA";
            case "CZK":
                return "\uD83C\uDDE8\uD83C\uDDFF";
            case "KZT":
                return "\uD83C\uDDF0\uD83C\uDDFF";
            case "HRK":
                return "\uD83C\uDDED\uD83C\uDDF7";
            case "JPY":
                return "\uD83C\uDDEF\uD83C\uDDF5";
            case "BYN":
                return "\uD83C\uDDE7\uD83C\uDDFE";
            case "AUD":
                return "\uD83C\uDDE6\uD83C\uDDF9";
            case "THB":
                return "\uD83C\uDDF9\uD83C\uDDED";
            case "BGN":
                return "\uD83C\uDDE7\uD83C\uDDEC";
            case "KRW":
                return "\uD83C\uDDF0\uD83C\uDDF7";
            case "HKD":
                return "\uD83C\uDDED\uD83C\uDDF0";
            case "DKK":
                return "\uD83C\uDDE9\uD83C\uDDF0";
            case "EGP":
                return "\uD83C\uDDEA\uD83C\uDDEC";
            case "INR":
                return "\uD83C\uDDEE\uD83C\uDDF3";
            case "IRR":
                return "\uD83C\uDDEE\uD83C\uDDF7";
            case "CAD":
                return "\uD83C\uDDE8\uD83C\uDDE6";
            case "MXN":
                return "\uD83C\uDDF2\uD83C\uDDFD";
            case "MDL":
                return "\uD83C\uDDF2\uD83C\uDDE9";
            case "ILS":
                return "\uD83C\uDDEE\uD83C\uDDF1";
            case "NZD":
                return "\uD83C\uDDF3\uD83C\uDDFF";
            case "NOK":
                return "\uD83C\uDDF3\uD83C\uDDF4";
            case "RON":
                return "\uD83C\uDDF7\uD83C\uDDF4";
            case "IDR":
                return "\uD83C\uDDEE\uD83C\uDDE9";
            case "SGD":
                return "\uD83C\uDDF8\uD83C\uDDEC";
            case "XDR":
                return "\uD83D\uDCB1";
            case "TRY":
                return "\uD83C\uDDF9\uD83C\uDDF7";
            case "SEK":
                return "\uD83C\uDDF8\uD83C\uDDEA";
            case "CNY":
                return "\uD83C\uDDE8\uD83C\uDDF3";


            default:
                return "";
        }
    }

    public static File getXls (Date date){
        try {
            LinkedList<RatesResponse> rates = getRatesBase(date);
            SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");
            String filename = "export/Rates_"+shortDate.format(date)+".xls";

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet();
            HSSFRow rowhead = sheet.createRow(0);
            rowhead.createCell(0).setCellValue("RDATE");
            rowhead.createCell(1).setCellValue("CCY");
            rowhead.createCell(2).setCellValue("CODE");
            rowhead.createCell(3).setCellValue("RATE");
            String exchangeDate="";

            int counter =1;
            for (RatesResponse response : rates){
                HSSFRow row = sheet.createRow(counter);
                exchangeDate = response.getExchangedate();
                row.createCell(0).setCellValue(response.getExchangedate());
                row.createCell(1).setCellValue(response.getCc());
                row.createCell(2).setCellValue(response.getR030());
                row.createCell(3).setCellValue(response.getRate());

                counter++;
            }

            HSSFRow row = sheet.createRow(counter);

            row.createCell(0).setCellValue(exchangeDate);
            row.createCell(1).setCellValue("UAH");
            row.createCell(2).setCellValue("980");
            row.createCell(3).setCellValue(1);

            File xls = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(xls);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return xls;





        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void sendFile (File file, String email){
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });


        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("belkabot.tg@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(file.getName());
            message.setText("Created by @belkatest_bot");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String fileName = file.getName();
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message);


        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }




}
