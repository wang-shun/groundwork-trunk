package org.groundwork.rs.it;

import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;
import org.groundwork.rs.dto.DtoHost;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.assertj.core.util.Lists.newArrayList;

public class AssertJSampleTest {

    class Name {
        private String first;
        private String last;

        public Name(String first, String last) {
            this.first = first;
            this.last = last;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

//        @Override
//        public boolean equals(Object obj) {
//            Name other = (Name)obj;
//            return last.equals(other.getLast()) && first.equals(other.getFirst());
//        }
        
    }

    class CompareHosts implements Comparator<DtoHost>  {
        @Override
        public int compare(DtoHost d1, DtoHost d2) {
            return d1.getHostName().compareTo(d2.getHostName());
        }
    }

    public class CompareNames implements Comparator<Name>  {
        @Override
        public int compare(Name d1, Name d2) {
            return d1.getFirst().compareTo(d2.getFirst());
        }
    }

    @Test
    public void MyTester() {
        Integer size = 3;
        Integer exp = 3;
        String frodo = "Frodo";
        String imposter = "frodo";

        List<String> source = newArrayList("one", "two", "three" );
        List<String> ditto = newArrayList("one", "two", "three" );
        List<String> unordered = newArrayList("three", "two", "one" );
        List<String> different = newArrayList("three", "two", "one", "four" );
        assertThat(ditto).containsOnlyElementsOf(source);
        assertThat(unordered).containsOnlyElementsOf(source);
     //   assertThat(different).containsOnlyElementsOf(source);
        assertThat(source).doesNotContainSequence(different);


        List<Name> names = newArrayList(new Name("Frodo", "Baggins"), new Name("JR", "Tolkien"), new Name("Bilbo", "Baggins"));
        List<Name> names2 = newArrayList(new Name("Frodo", "Baggins"), new Name("JR", "Tolkien"), new Name("Bilbo", "Baggins"));
        assertThat(names).usingElementComparator(new CompareNames()).hasSameElementsAs(names2); /// isEqualTo(names2);
        
        //List<String> fellows = new ArrayList<>(Arrays.asList("aragorn, frodo, legolas, boromir"));
        List<String> fellows = newArrayList("aragon", "frodo", "legolas", "boromir");
        assertThat(frodo).as("Expecting %s here", frodo).isEqualToIgnoringCase(imposter);
        assertThat(size).as("expected bulk host count of %d", exp).isEqualTo(exp);
        assertThat(fellows).contains("frodo", "boromir");
        assertThat(fellows).containsOnlyOnce("frodo", "boromir");
        //assertThat(fellows).containsOnly("frodo", "boromir");
        assertThat(names).filteredOn("last", in("Baggins")).hasSize(2);

        assertThat(names).extracting("last")
                .contains("Baggins")
                .doesNotContain("Sauron", "Elrond");


        Name dave = new Name("Dave", "Taylor");
        Name clone = new Name("Dave", "Taylor");

        assertThat(dave).isEqualToComparingFieldByField(clone);

        try {
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(1).isEqualTo(2);
            softly.assertThat(2).isEqualTo(3);
            softly.assertAll();
        }
        catch (SoftAssertionError e) {
            assertThat(e).hasMessageContaining("expected:<[2]> but was:<[1]>");
        }

//        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
//
//        }

    }

    @Test
    public void testProperties() throws Exception {
        DtoHost h1 = new DtoHost();
        h1.setHostName("host1");
        h1.setAgentId("agent007");
        h1.getProperties().put("LastPluginOutput", "host is down!");
        h1.putProperty("PerformanceData", "90 MPH");
        DtoHost h2 = new DtoHost();
        h2.setHostName("host2");
        h2.setAgentId("agent007");
        h2.getProperties().put("LastPluginOutput", "host is down!");
        h2.getProperties().put("LastPluginOutput", "host is down!");

        DtoHost h3 = new DtoHost();
        h3.setHostName("host1");
        h3.setAgentId("agent007");
        h3.getProperties().put("LastPluginOutput", "host is down!");
        h3.putProperty("PerformanceData", "90 MPH");

        assertThat(h1.getProperties()).contains(javaMapEntry("LastPluginOutput", "host is down!"), javaMapEntry("PerformanceData", "90 MPH"));

        assertThat(h1).isEqualToIgnoringNullFields(h3);
        assertThat(h1).isEqualToComparingOnlyGivenFields(h2, "agentId");
        // can't seem to compare Maps by introspection
        //assertThat(h1).isEqualToComparingOnlyGivenFields(h2, "agentId", "properties.LastPluginOutput");
    }

    private static <K, V> Map.Entry<K, V> javaMapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}

