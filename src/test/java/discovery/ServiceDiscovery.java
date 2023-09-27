package discovery;

import java.util.List;

/**
 * 两种获取微服务列表的方式
 */
public interface ServiceDiscovery {

    List<String> discover(String serviceName);

    void watchAndDiscover(String serviceName);

}
