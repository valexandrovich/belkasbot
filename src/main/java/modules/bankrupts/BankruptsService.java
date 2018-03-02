package modules.bankrupts;


import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class BankruptsService {








    public static LinkedList<BankruptRow> findByCode(String code) throws IOException, InterruptedException, ParseException {
        int foundRows = 0;
        int insertedRows = 0;
        int offset = 0;

        LinkedList<BankruptRow> result = new LinkedList<>();

        do{

            BankruptsRequest bankruptsRequest = new BankruptsRequest();
            bankruptsRequest.setiDisplayStart(String.valueOf(offset));
            BankruptsResponse bankruptsResponse = bankruptsRequest.findByCode(code);
            foundRows = Integer.parseInt(bankruptsResponse.iTotalDisplayRecords);
            if (foundRows == 0 ) {
                return null;
            } else if (bankruptsResponse.aaData.size()>0){
                for(BankruptRow row : bankruptsResponse.getRowsFromResponse()){
                    result.add(row);
                    //row.print();
                    insertedRows++;
                }
                offset+=10;
            } else {
                break;
            }

        } while (insertedRows<=foundRows);
        return result;

    }

}