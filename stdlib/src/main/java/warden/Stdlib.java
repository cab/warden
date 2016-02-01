package warden;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.option.Options;

import java.util.*;

public class Stdlib {


    private static Optional<Consul> consul = Optional.empty();



    private static class StringUser implements User {

        private String name;

        public StringUser(String name) {
            this.name = name;
        }

        @Override
        public String getId() {
            return name;
        }
    }

    public static <T> Set set(T value, T... values) {
        Set<T> set = new HashSet<T>(values.length + 1);
        set.add(value);
        set.addAll(Arrays.asList(values));
        return Collections.unmodifiableSet(set);
    }

    public static boolean connect() {
        consul = Optional.of(Consul.builder().build()); //TODO allow connection setup
        return true;
    }

    public static void log(String message) {
        System.out.println(message);
    }

    public static boolean NOT(boolean value) {
        return !value;
    }

    public static <T> boolean in(Collection<T> collection, T element) {
        return collection.contains(element);
    }

    public static User user(String name) {
        return new StringUser(name);
    }

    public static List list(String name) {
        return new ArrayList<>(0);
    }

    public static <T> Optional<String> read(String key) {
       return consul.map(consul -> {
            KeyValueClient kvClient = consul.keyValueClient();
            kvClient.putValue("foo", "bar");
            return kvClient.getValueAsString(key).get();
        });
    }

    private Stdlib() {

    }

}
