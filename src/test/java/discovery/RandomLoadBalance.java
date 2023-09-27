package discovery;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {

    private Random random = new Random();

    @Override
    public String select(List<String> urls) {
        int idx = random.nextInt(urls.size());
        return urls.get(idx);
    }
}
