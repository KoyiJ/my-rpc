package register;

/**
 * 定义一个服务注册的接口，如果以后有其他服务注册的策略，实现该接口即可
 */
public interface ServiceRegistry {

    void register();

}
