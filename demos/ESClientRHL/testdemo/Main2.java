import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: esdemo
 * @description: ${description}
 * @author: X-Pacific zhang
 * @create: 2019-01-30 09:22
 **/
@ESMetaData(indexName = "index",indexType = "main4", number_of_shards = 5,number_of_replicas = 0,printLog = false)
public class Main2 implements Serializable {
    private static final long serialVersionUID = 1L;
    @ESID
    private String proposal_no;
    @ESMapping(datatype = DataType.keyword_type)
    private String risk_code;
    @ESMapping(datatype = DataType.text_type)
    private String risk_name;
    @ESMapping(keyword = true)
    private String business_nature;
    @ESMapping(datatype = DataType.text_type)
    private String business_nature_name;
    private String appli_code;//可以用默认值，这样会有appli_code.keyword可以直接搜
    @ESMapping(suggest = true)
    private String appli_name;
    private String insured_code;
    @ESMapping(autocomplete = true)
    private String insured_name;
    @ESMapping(datatype = DataType.date_type)
    private Date operate_date;
    @ESMapping(datatype = DataType.text_type)
    private String operate_date_format;
    @ESMapping(datatype = DataType.date_type)
    private Date start_date;
    @ESMapping(datatype = DataType.date_type)
    private Date end_date;
    @ESMapping(datatype = DataType.double_type)
    private double sum_amount;
    @ESMapping(datatype = DataType.double_type)
    private double sum_premium;
    @ESMapping(datatype = DataType.keyword_type)
    private String com_code;

    public String getProposal_no() {
        return proposal_no;
    }

    public void setProposal_no(String proposal_no) {
        this.proposal_no = proposal_no;
    }

    public String getRisk_code() {
        return risk_code;
    }

    public void setRisk_code(String risk_code) {
        this.risk_code = risk_code;
    }

    public String getRisk_name() {
        return risk_name;
    }

    public void setRisk_name(String risk_name) {
        this.risk_name = risk_name;
    }

    public String getBusiness_nature() {
        return business_nature;
    }

    public void setBusiness_nature(String business_nature) {
        this.business_nature = business_nature;
    }

    public String getBusiness_nature_name() {
        return business_nature_name;
    }

    public void setBusiness_nature_name(String business_nature_name) {
        this.business_nature_name = business_nature_name;
    }

    public String getAppli_code() {
        return appli_code;
    }

    public void setAppli_code(String appli_code) {
        this.appli_code = appli_code;
    }

    public String getAppli_name() {
        return appli_name;
    }

    public void setAppli_name(String appli_name) {
        this.appli_name = appli_name;
    }

    public String getInsured_code() {
        return insured_code;
    }

    public void setInsured_code(String insured_code) {
        this.insured_code = insured_code;
    }

    public String getInsured_name() {
        return insured_name;
    }

    public void setInsured_name(String insured_name) {
        this.insured_name = insured_name;
    }

    public Date getOperate_date() {
        return operate_date;
    }

    public void setOperate_date(Date operate_date) {
        this.operate_date = operate_date;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public double getSum_amount() {
        return sum_amount;
    }

    public void setSum_amount(double sum_amount) {
        this.sum_amount = sum_amount;
    }

    public double getSum_premium() {
        return sum_premium;
    }

    public void setSum_premium(double sum_premium) {
        this.sum_premium = sum_premium;
    }

    public String getCom_code() {
        return com_code;
    }

    public void setCom_code(String com_code) {
        this.com_code = com_code;
    }

    public String getOperate_date_format() {
        return operate_date_format;
    }

    public void setOperate_date_format(String operate_date_format) {
        this.operate_date_format = operate_date_format;
    }

    @Override
    public String toString() {
        return "Main2{" +
                ", proposal_no='" + proposal_no + '\'' +
                ", risk_code='" + risk_code + '\'' +
                ", risk_name='" + risk_name + '\'' +
                ", business_nature='" + business_nature + '\'' +
                ", business_nature_name='" + business_nature_name + '\'' +
                ", appli_code='" + appli_code + '\'' +
                ", appli_name='" + appli_name + '\'' +
                ", insured_code='" + insured_code + '\'' +
                ", insured_name='" + insured_name + '\'' +
                ", operate_date=" + operate_date +
                ", operate_date_format='" + operate_date_format + '\'' +
                ", start_date=" + start_date +
                ", end_date=" + end_date +
                ", sum_amount=" + sum_amount +
                ", sum_premium=" + sum_premium +
                ", com_code='" + com_code + '\'' +
                '}';
    }
}
