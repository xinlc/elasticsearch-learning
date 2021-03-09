package org.zxp.esclientrhl.util;

import org.elasticsearch.common.unit.ByteSizeUnit;

import java.util.concurrent.TimeUnit;

/**
 * 元数据载体类
 */
public class MetaData{
    private String indexname = "";
    private String indextype = "";
    private String[] searchIndexNames;
    private int number_of_shards;
    private int number_of_replicas;
    private boolean printLog = false;
    private boolean alias;
    private String[] aliasIndex;
    private String writeIndex;
    private boolean rollover;
    private long rolloverMaxIndexAgeCondition;
    private TimeUnit rolloverMaxIndexAgeTimeUnit;
    private long rolloverMaxIndexDocsCondition;
    private long rolloverMaxIndexSizeCondition;
    private ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit;
    private boolean autoRollover;
    private long autoRolloverInitialDelay;
    private long autoRolloverPeriod;
    private TimeUnit  autoRolloverTimeUnit;
    //indexName的后缀，一般用于配置中环境的区分
    private String suffix;
    private boolean autoCreateIndex;
    private long maxResultWindow;

    public MetaData(String indexname, String indextype) {
        this.indexname = indexname;
        this.indextype = indextype;
    }

    public MetaData(String indexname, String indextype, int number_of_shards, int number_of_replicas) {
        this.indexname = indexname;
        this.indextype = indextype;
        this.number_of_shards = number_of_shards;
        this.number_of_replicas = number_of_replicas;
    }

    public MetaData(int number_of_shards, int number_of_replicas) {
        this.number_of_shards = number_of_shards;
        this.number_of_replicas = number_of_replicas;
    }

    public String[] getSearchIndexNames() {
        return searchIndexNames;
    }
    public void setSearchIndexNames(String[] searchIndexNames) {
        this.searchIndexNames = searchIndexNames;
    }
    public boolean isPrintLog() {
        return printLog;
    }
    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }
    public String getIndexname() {
        return indexname;
    }
    public void setIndexname(String indexname) {
        this.indexname = indexname;
    }
    public String getIndextype() {
        return indextype;
    }
    public void setIndextype(String indextype) {
        this.indextype = indextype;
    }
    public int getNumber_of_shards() {
        return number_of_shards;
    }
    public void setNumber_of_shards(int number_of_shards) {
        this.number_of_shards = number_of_shards;
    }
    public int getNumber_of_replicas() {
        return number_of_replicas;
    }
    public void setNumber_of_replicas(int number_of_replicas) {
        this.number_of_replicas = number_of_replicas;
    }
    public long getMaxResultWindow() {
        return maxResultWindow;
    }
    public void setMaxResultWindow(long maxResultWindow) {
        this.maxResultWindow = maxResultWindow;
    }
    public boolean isAlias() {
        return alias;
    }
    public void setAlias(boolean alias) {
        this.alias = alias;
    }
    public String[] getAliasIndex() {
        return aliasIndex;
    }
    public void setAliasIndex(String[] aliasIndex) {
        this.aliasIndex = aliasIndex;
    }
    public String getWriteIndex() {
        return writeIndex;
    }
    public void setWriteIndex(String writeIndex) {
        this.writeIndex = writeIndex;
    }
    public boolean isRollover() {
        return rollover;
    }
    public void setRollover(boolean rollover) {
        this.rollover = rollover;
    }
    public long getRolloverMaxIndexAgeCondition() {
        return rolloverMaxIndexAgeCondition;
    }
    public void setRolloverMaxIndexAgeCondition(long rolloverMaxIndexAgeCondition) {
        this.rolloverMaxIndexAgeCondition = rolloverMaxIndexAgeCondition;
    }
    public TimeUnit getRolloverMaxIndexAgeTimeUnit() {
        return rolloverMaxIndexAgeTimeUnit;
    }
    public void setRolloverMaxIndexAgeTimeUnit(TimeUnit rolloverMaxIndexAgeTimeUnit) {
        this.rolloverMaxIndexAgeTimeUnit = rolloverMaxIndexAgeTimeUnit;
    }
    public long getRolloverMaxIndexDocsCondition() {
        return rolloverMaxIndexDocsCondition;
    }
    public void setRolloverMaxIndexDocsCondition(long rolloverMaxIndexDocsCondition) {
        this.rolloverMaxIndexDocsCondition = rolloverMaxIndexDocsCondition;
    }
    public long getRolloverMaxIndexSizeCondition() {
        return rolloverMaxIndexSizeCondition;
    }
    public void setRolloverMaxIndexSizeCondition(long rolloverMaxIndexSizeCondition) {
        this.rolloverMaxIndexSizeCondition = rolloverMaxIndexSizeCondition;
    }
    public ByteSizeUnit getRolloverMaxIndexSizeByteSizeUnit() {
        return rolloverMaxIndexSizeByteSizeUnit;
    }
    public void setRolloverMaxIndexSizeByteSizeUnit(ByteSizeUnit rolloverMaxIndexSizeByteSizeUnit) {
        this.rolloverMaxIndexSizeByteSizeUnit = rolloverMaxIndexSizeByteSizeUnit;
    }
    public boolean isAutoRollover() {
        return autoRollover;
    }
    public void setAutoRollover(boolean autoRollover) {
        this.autoRollover = autoRollover;
    }
    public long getAutoRolloverInitialDelay() {
        return autoRolloverInitialDelay;
    }
    public void setAutoRolloverInitialDelay(long autoRolloverInitialDelay) {
        this.autoRolloverInitialDelay = autoRolloverInitialDelay;
    }

    public long getAutoRolloverPeriod() {
        return autoRolloverPeriod;
    }

    public void setAutoRolloverPeriod(long autoRolloverPeriod) {
        this.autoRolloverPeriod = autoRolloverPeriod;
    }

    public TimeUnit getAutoRolloverTimeUnit() {
        return autoRolloverTimeUnit;
    }

    public void setAutoRolloverTimeUnit(TimeUnit autoRolloverTimeUnit) {
        this.autoRolloverTimeUnit = autoRolloverTimeUnit;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isAutoCreateIndex() {
        return autoCreateIndex;
    }

    public void setAutoCreateIndex(boolean autoCreateIndex) {
        this.autoCreateIndex = autoCreateIndex;
    }
}