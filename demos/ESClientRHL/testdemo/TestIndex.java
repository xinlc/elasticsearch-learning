import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zxp.esclientrhl.index.ElasticsearchIndex;

/**
 * @program: esdemo
 * @description: ${description}
 * @author: X-Pacific zhang
 * @create: 2019-02-25 14:13
 **/
public class TestIndex extends EsdemoApplicationTests{
    @Autowired
    ElasticsearchIndex<Main2> elasticsearchIndex;
    @Test
    public void testIndex() throws Exception {
        if(!elasticsearchIndex.exists(Main2.class)){
            elasticsearchIndex.dropIndex(Main2.class);
            elasticsearchIndex.createIndex(Main2.class);
        }
    }
}
