import com.ivmiku.mikumq.utils.PasswordUtil;

public class GuestTest {
    public static void main(String[] args) {
        String salt = PasswordUtil.getSalt(10);
        String p = PasswordUtil.encrypt("guest", salt);
        System.out.println(salt);
        System.out.println(p);
    }
}
