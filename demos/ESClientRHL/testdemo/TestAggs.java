import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.repository.Down;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;

import java.util.List;
import java.util.Map;

/**
 * @program: esdemo
 * @description: ${description}
 * @author: X-Pacific zhang
 * @create: 2019-02-27 10:09
 **/
public class TestAggs extends  EsdemoApplicationTests {
    @Autowired
    ElasticsearchTemplate<Main2,String> elasticsearchTemplate;

    @Test
    public void testOri() throws Exception {
        //https://www.elastic.co/guide/en/elasticsearch/client/java-api/6.6/_metrics_aggregations.html
        SumAggregationBuilder aggregation = AggregationBuilders.sum("agg").field("sum_amount");
        Aggregations aggregations = elasticsearchTemplate.aggs(aggregation,null,Main2.class);
        Sum agg = aggregations.get("agg");
        double value = agg.getValue();
        System.out.println(value);
    }


    @Test
    public void testAggs() throws Exception {
        double sum = elasticsearchTemplate.aggs("sum_premium", AggsType.sum,null,Main2.class);
        double count = elasticsearchTemplate.aggs("sum_premium", AggsType.count,null,Main2.class);
        double avg = elasticsearchTemplate.aggs("sum_premium", AggsType.avg,null,Main2.class);
        double min = elasticsearchTemplate.aggs("sum_premium", AggsType.min,null,Main2.class);
        double max = elasticsearchTemplate.aggs("sum_premium", AggsType.max,null,Main2.class);


        System.out.println("sum===="+sum);
        System.out.println("count===="+count);
        System.out.println("avg===="+avg);
        System.out.println("min===="+min);
        System.out.println("max===="+max);
    }


    @Test
    public void testAggs2() throws Exception {
        Map map = elasticsearchTemplate.aggs("sum_premium", AggsType.sum,null,Main2.class,"appli_name");
        map.forEach((k,v) -> System.out.println(k+"     "+v));
    }

    @Test
    public void testAggs2level() throws Exception {
        String[] strs = {"appli_name","risk_code"};
        List<Down> list = elasticsearchTemplate.aggswith2level("sum_premium", AggsType.sum,null,Main2.class,strs);
        list.forEach(down ->
            {
                System.out.println("1:"+down.getLevel_1_key());
                System.out.println("2:"+down.getLevel_2_key() + "    "+ down.getValue());
            }
        );
    }

    @Test
    public void testAggsStats() throws Exception {
        Stats stats = elasticsearchTemplate.statsAggs("sum_premium",null,Main2.class);
        System.out.println("max:"+stats.getMax());
        System.out.println("min:"+stats.getMin());
        System.out.println("sum:"+stats.getSum());
        System.out.println("count:"+stats.getCount());
        System.out.println("avg:"+stats.getAvg());

    }


    @Test
    public void testAggsStats2() throws Exception {
        Map<String, Stats> stats = elasticsearchTemplate.statsAggs("sum_premium",null,Main2.class,"risk_code");
        stats.forEach((k,v) ->
            {
                System.out.println(k+"    count:"+v.getCount()+" sum:"+v.getSum()+"...");
            }
        );
    }

    @Test
    public void testCardinality() throws Exception {
        long value = elasticsearchTemplate.cardinality("proposal_no",null,Main2.class);
        System.out.println(value);
    }


    @Test
    public void testPercentiles() throws Exception {
        Map map = elasticsearchTemplate.percentilesAggs("sum_premium",null,Main2.class);
        map.forEach((k,v) ->
                {
                    System.out.println(k+"     "+v);
                }
        );
        double[] dbs = {10.0,20.0,30.0,50.0,60.0,90.0,99.0};
        Map map2 = elasticsearchTemplate.percentilesAggs("sum_premium",null,Main2.class,dbs);
    }

    @Test
    public void testPercentilesRank() throws Exception {
        double[] dbs = {1,4,5,9};
        Map map = elasticsearchTemplate.percentileRanksAggs("sum_premium",null,Main2.class,dbs);
        map.forEach((k,v) ->
                {
                    System.out.println(k+"     "+v);
                }
        );
    }

    @Test
    public void testFilterAggs() throws Exception {
        FiltersAggregator.KeyedFilter[] filters = {new FiltersAggregator.KeyedFilter("0101", QueryBuilders.matchPhraseQuery("risk_code", "0101")),
                new FiltersAggregator.KeyedFilter("0103", QueryBuilders.matchQuery("risk_code", "0103"))};
        Map map = elasticsearchTemplate.filterAggs("sum_premium", AggsType.sum, null,Main2.class,filters);
        map.forEach((k, v) ->
                System.out.println(k + "    " + v)
        );
    }

    @Test
    public void testHistogramAggs() throws Exception {
        Map map = elasticsearchTemplate.histogramAggs("proposal_no", AggsType.count, null,Main2.class,"sum_premium",3);
        map.forEach((k, v) ->
                System.out.println(k + "    " + v)
        );
    }


    @Test
    public void testDateHistogramAggs() throws Exception {
        Map map = elasticsearchTemplate.dateHistogramAggs("sum_premium", AggsType.sum, null,Main2.class,"input_date", DateHistogramInterval.hours(2));
        map.forEach((k, v) ->
                System.out.println(k + "    " + v)
        );
    }

    @Test
    public void testRangeAggs() throws Exception {
        AggregationBuilder aggregation =
                AggregationBuilders.range("range").field("sum_premium").addUnboundedTo(1).addRange(1,4).addRange(4,100).addUnboundedFrom(100);
        aggregation.subAggregation(AggregationBuilders.count("agg").field("proposal_no.keyword"));
        Aggregations aggregations = elasticsearchTemplate.aggs(aggregation,null,Main2.class);
        Range range = aggregations.get("range");
        for (Range.Bucket entry : range.getBuckets()) {
            ValueCount count = entry.getAggregations().get("agg");
            long value = count.getValue();
            System.out.println(entry.getKey() + "    " + value);
        }
    }


}
