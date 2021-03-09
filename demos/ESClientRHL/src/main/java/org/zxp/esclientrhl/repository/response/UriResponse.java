package org.zxp.esclientrhl.repository.response;

import java.util.List;

/**
 * @program: esclientrhl
 * @description: uri返回json串反序列化对象
 * @author: X-Pacific zhang
 * @create: 2019-10-10 10:45
 **/
public class UriResponse {


    /**
     * took : 9
     * timed_out : false
     * _shards : {"total":5,"successful":5,"skipped":0,"failed":0}
     * hits : {"total":{"value":1,"relation":"eq"},"max_score":0.9808292,"hits":[{"_index":"index","_type":"main4","_id":"aaa","_score":0.9808292,"_source":{"end_date":null,"proposal_no":"aaa","sum_amount":0,"business_nature_name":"aaaaaa2","operate_date":null,"sum_premium":0,"appli_name":null,"business_nature":null,"insured_code":null,"appli_code":null,"serialVersionUID":1,"operate_date_format":null,"insured_name":null,"com_code":null,"risk_code":null,"risk_name":null,"start_date":null}}]}
     */

    private int took;
    private boolean timed_out;
    private ShardsBean _shards;
    private HitsBeanX hits;

    public int getTook() {
        return took;
    }

    public void setTook(int took) {
        this.took = took;
    }

    public boolean isTimed_out() {
        return timed_out;
    }

    public void setTimed_out(boolean timed_out) {
        this.timed_out = timed_out;
    }

    public ShardsBean get_shards() {
        return _shards;
    }

    public void set_shards(ShardsBean _shards) {
        this._shards = _shards;
    }

    public HitsBeanX getHits() {
        return hits;
    }

    public void setHits(HitsBeanX hits) {
        this.hits = hits;
    }

    public static class ShardsBean {
        /**
         * total : 5
         * successful : 5
         * skipped : 0
         * failed : 0
         */

        private int total;
        private int successful;
        private int skipped;
        private int failed;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getSuccessful() {
            return successful;
        }

        public void setSuccessful(int successful) {
            this.successful = successful;
        }

        public int getSkipped() {
            return skipped;
        }

        public void setSkipped(int skipped) {
            this.skipped = skipped;
        }

        public int getFailed() {
            return failed;
        }

        public void setFailed(int failed) {
            this.failed = failed;
        }
    }

    public static class HitsBeanX {
        /**
         * total : {"value":1,"relation":"eq"}
         * max_score : 0.9808292
         * hits : [{"_index":"index","_type":"main4","_id":"aaa","_score":0.9808292,"_source":{"end_date":null,"proposal_no":"aaa","sum_amount":0,"business_nature_name":"aaaaaa2","operate_date":null,"sum_premium":0,"appli_name":null,"business_nature":null,"insured_code":null,"appli_code":null,"serialVersionUID":1,"operate_date_format":null,"insured_name":null,"com_code":null,"risk_code":null,"risk_name":null,"start_date":null}}]
         */

        private TotalBean total;
        private double max_score;
        private List<HitsBean> hits;

        public TotalBean getTotal() {
            return total;
        }

        public void setTotal(TotalBean total) {
            this.total = total;
        }

        public double getMax_score() {
            return max_score;
        }

        public void setMax_score(double max_score) {
            this.max_score = max_score;
        }

        public List<HitsBean> getHits() {
            return hits;
        }

        public void setHits(List<HitsBean> hits) {
            this.hits = hits;
        }

        public static class TotalBean {
            /**
             * value : 1
             * relation : eq
             */

            private int value;
            private String relation;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }

            public String getRelation() {
                return relation;
            }

            public void setRelation(String relation) {
                this.relation = relation;
            }
        }

        public static class HitsBean {
            /**
             * _index : index
             * _type : main4
             * _id : aaa
             * _score : 0.9808292
             * _source : {"end_date":null,"proposal_no":"aaa","sum_amount":0,"business_nature_name":"aaaaaa2","operate_date":null,"sum_premium":0,"appli_name":null,"business_nature":null,"insured_code":null,"appli_code":null,"serialVersionUID":1,"operate_date_format":null,"insured_name":null,"com_code":null,"risk_code":null,"risk_name":null,"start_date":null}
             */

            private String _index;
            private String _type;
            private String _id;
            private double _score;
            private Object _source;

            public String get_index() {
                return _index;
            }

            public void set_index(String _index) {
                this._index = _index;
            }

            public String get_type() {
                return _type;
            }

            public void set_type(String _type) {
                this._type = _type;
            }

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public double get_score() {
                return _score;
            }

            public void set_score(double _score) {
                this._score = _score;
            }

            public Object get_source() {
                return _source;
            }

            public void set_source(Object _source) {
                this._source = _source;
            }
        }
    }
}
