import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;

/**
 * @program: esdemo
 * @description: ${description}
 * @author: X-Pacific zhang
 * @create: 2019-02-25 16:18
 **/
public class TestLowLevelClient extends EsdemoApplicationTests {

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testLow() throws Exception {
        Request request = new Request("GET","/esdemo");
        request.setEntity(new NStringEntity(
                "{\"query\":{\"match_all\":{\"boost\":1.0}}}",
                ContentType.APPLICATION_JSON));
        Response response = elasticsearchTemplate.request(request);
        RequestLine requestLine = response.getRequestLine();
        HttpHost host = response.getHost();
        int statusCode = response.getStatusLine().getStatusCode();
        Header[] headers = response.getHeaders();
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
    }
}
