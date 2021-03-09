package com.sinosoft.esdemo.domain;

import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.io.Serializable;

/**
 * @program: esdemo
 * @description:
 * @author: X-Pacific zhang
 * @create: 2019-10-11 12:54
 **/
@ESMetaData(indexName = "sugg",number_of_shards = 1,number_of_replicas = 0,printLog = true)
public class Sugg implements Serializable {
    private static final long serialVersionUID = 1L;
    @ESID
    private String uid;

    @ESMapping(suggest = true)
    private String appno;

    @ESMapping(datatype = DataType.text_type)
    private String body;

    @ESMapping(ngram = true)
    private String msg;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAppno() {
        return appno;
    }

    public void setAppno(String appno) {
        this.appno = appno;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Sugg{" +
                "uid='" + uid + '\'' +
                ", appno='" + appno + '\'' +
                ", body='" + body + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
