package modules.bankrupts;

import java.text.ParseException;

public class BankruptRow {
    private String publicDate;
    private String number;
    private String docType;
    private String okpo;
    private String name;
    private String docNumber;
    private String courtName;
    private String auDate;
    private String finalDate;

    public String getPublicDate() {
        return publicDate;
    }
    public void setPublicDate(String publicDate) {
        this.publicDate = publicDate;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getDocType() {
        return docType;
    }
    public void setDocType(String docType) {
        this.docType = docType;
    }
    public String getOkpo() {
        return okpo;
    }
    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDocNumber() {
        return docNumber;
    }
    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }
    public String getCourtName() {
        return courtName;
    }
    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }
    public String getAuDate() {
        return auDate;
    }
    public void setAuDate(String auDate) {
        this.auDate = auDate;
    }
    public String getFinalDate() {
        return finalDate;
    }
    public void setFinalDate(String finalDate) {
        this.finalDate = finalDate;
    }

    public BankruptRow(String publicDate, String number, String docType, String okpo, String name, String docNumber, String courtName, String auDate, String finalDate) throws ParseException {


        if (nvl(publicDate).equals("01.01.1970")){

        }else {
            this.publicDate = nvl(publicDate);
        }

        this.number = nvl(number);

        switch (docType){
            case "Оголошення про проведення загальних зборів кредиторів": this.docType = "Аукціон"; break;
            case "Повідомлення про скасування аукціону з продажу майна (додаткове)": this.docType = "Аукціон"; break;
            case "Повідомлення про результати проведення аукціону з продажу майна (додаткове)": this.docType = "Аукціон"; break;
            case "Повідомлення про прийняття до розгляду заяви про затвердження плану санації боржника": this.docType = "Санація"; break;
            case "Оголошення про порушення справи про банкрутство": this.docType = "Порушено справу про банкрутство"; break;
            case "Повідомлення про введення процедури санації": this.docType = "Санація"; break;
            case "Повідомлення про визнання боржника банкрутом і відкриття ліквідаційної процедури": this.docType = "Визнано банкрутом"; break;
            case "Повідомлення про поновлення провадження у справі про банкрутство у зв’язку з визнанням мирової угоди недійсною або її розірвання": this.docType = "Поновлено провадження у справі про банкрутство"; break;
            case "Оголошення про проведення аукціону з продажу майна": this.docType = "Аукціон"; break;
            case "Повідомлення про результати проведення аукціону з продажу майна": this.docType = "Аукціон"; break;
            case "Оголошення про порушення справи про банкрутство і відкриття процедури санації": this.docType = "Порушено справу про банкрутство"; break;
            default: this.docType = nvl(docType); break;

        }




        this.okpo = nvl(okpo);
        this.name = nvl(name);
        this.docNumber = nvl(docNumber);
        this.courtName = nvl(courtName);
        this.auDate = nvl(auDate);
        this.finalDate = nvl(finalDate);
    }

    private String nvl(String text){
        if (text == null || text.length()<1){
            return "";
        } else {
            return text;
        }
    }


    @Deprecated
    public void print() {
        System.out.println(publicDate+" - "+docType+" - "+okpo+" - "+name);
    }
}
