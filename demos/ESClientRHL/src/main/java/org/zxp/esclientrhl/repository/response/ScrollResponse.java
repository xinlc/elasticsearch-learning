package org.zxp.esclientrhl.repository.response;

import java.util.List;

/**
 * @program: esclientrhl
 * @description:
 * @author: X-Pacific zhang
 * @create: 2019-10-15 17:58
 **/
public class ScrollResponse<T> {
    private List<T> list;
    private String scrollId;

    public ScrollResponse(List<T> list, String scrollId) {
        this.list = list;
        this.scrollId = scrollId;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
