package discovery;

import java.util.List;

/**
 * 也许实现不同的负载均衡策略，所以此处定义一个接口
 */
public interface LoadBalance {

    public String select(List<String> urls);
}
