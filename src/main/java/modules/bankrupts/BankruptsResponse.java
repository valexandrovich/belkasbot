package modules.bankrupts;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/* Класс описывает структуру ответа от сервера для парсинга в формат JSON */

public class BankruptsResponse {
    public String sEcho;
    public String iTotalRecords;
    public String iTotalDisplayRecords;
    public List<List<String>> aaData;
    public String[][] ptype;
    public String[][] pkind;


    @Deprecated
    public void Print(){
        for (List<String> row : aaData){
            for (String cell : row){
                System.out.print(row);
            }
            System.out.println("-------------------");
        }
    }


    public List<BankruptRow> getRowsFromResponse() throws ParseException {
        List<BankruptRow> list = new LinkedList<>();

        for (List<String> record : aaData){
            list.add(new BankruptRow(record.get(0)
                    , record.get(1)
                    , record.get(2)
                    , record.get(3)
                    , record.get(4).trim()  // Образаем пробелы у имени
                    , record.get(5)
                    , record.get(6)
                    , record.get(7)
                    , record.get(8)));
        }

        return list;

    }


}
