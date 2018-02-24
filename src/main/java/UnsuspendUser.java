import com.google.common.base.Stopwatch;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Role;
import org.zendesk.client.v2.model.User;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author rbolles on 2/15/18.
 */
public class UnsuspendUser {

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        properties.load(new FileReader("zendesk.properties"));

        String zendeskUrl = properties.getProperty("zendeskUrl");
        String zendeskUsername = properties.getProperty("zendeskUsername");
        String zendeskToken = properties.getProperty("zendeskToken");

        Zendesk zendesk = new Zendesk.Builder(zendeskUrl)
                .setUsername(zendeskUsername)
                .setToken(zendeskToken)
                .build();

        String email = "suspend.delay@partner.com";
        Iterable<User> users = zendesk.lookupUserByEmail(email);

        Iterator<User> iterator = users.iterator();

        User user;

        if (iterator.hasNext()) {
            user = iterator.next();
        } else {
            user = new User();
            user.setName("Suspend User Test");
            user.setEmail(email);
            user.setRole(Role.END_USER);

            user = zendesk.createUser(user);
        }

        if (user.getSuspended()) {
            throw new RuntimeException("unsuspend user in ui first");
        }

        user.setSuspended(true);
        System.out.println("Suspending user");
        zendesk.updateUser(user);

        Stopwatch stopwatch = Stopwatch.createStarted();

        while (stopwatch.elapsed(TimeUnit.SECONDS) < 300) {

            user = zendesk.getUser(user.getId());
            if (!user.getSuspended()) {
                System.out.println("Not yet suspended at " + stopwatch.toString());
                Thread.sleep(1000);
            } else {
                System.out.println("Suspended at " + stopwatch.toString());
                break;
            }
        }

        stopwatch.reset();

        System.out.println("Unsuspend the user");
        user.setSuspended(false);
        zendesk.updateUser(user);

        stopwatch.start();

        while (stopwatch.elapsed(TimeUnit.SECONDS) < 300) {

            user = zendesk.getUser(user.getId());
            if (user.getSuspended()) {
                System.out.println("Not yet un-suspended at " + stopwatch.toString());
                Thread.sleep(1000);
            } else {
                System.out.println("Un-suspended at " + stopwatch.toString());
                break;
            }
        }
    }
}
