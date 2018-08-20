/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.util;

import com.groundwork.collage.test.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * TestAutocomplete
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestAutocomplete extends TestCase {

    public TestAutocomplete(String x) {
        super(x);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // run all tests
        suite = new TestSuite(TestAutocomplete.class);

        // or a subset thereoff
        //suite.addTest(new TestAutocomplete("testAutocomplete"));

        return suite;
    }

    /**
     * Test autocomplete functionality.
     */
    public void testAutocomplete() throws Exception {
        // check test assumptions
        assert Autocomplete.DEFAULT_NAMES_LIMIT == 10;

        // test autocomplete names
        final int [] openNamesIteratorCalls = new int[1];
        final int [] closeNamesIteratorCalls = new int[1];
        AutocompleteNames testNames = new AutocompleteNames() {
            @Override
            public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
                openNamesIteratorCalls[0]++;
                List<AutocompleteName> names = new ArrayList<AutocompleteName>();
                for (String name : new String[]{"a", "ab", "abc", "ba", "bb", "bc", "bd", "be", "bf", "bg", "bh", "bi", "bj",
                        "bk", "x", "xy", "xyz"}) {
                    names.add(new AutocompleteName(name));
                };
                return names.iterator();
            }

            @Override
            public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
                closeNamesIteratorCalls[0]++;
            }
        };

        // create and initialize autocomplete
        Autocomplete autocomplete = new Autocomplete(testNames, getClass().getSimpleName());
        autocomplete.initialize();
        // wait for initialization
        Thread.sleep(Autocomplete.REFRESH_SETTLE_MIN_WAIT*2);
        // check autocomplete names access
        assert openNamesIteratorCalls[0] == 1;
        assert closeNamesIteratorCalls[0] == 1;

        // test names
        List<AutocompleteName> names = autocomplete.autocomplete("a");
        assert names != null;
        assert names.size() == 3;
        assert "a".equals(names.get(0).getName());
        assert "ab".equals(names.get(1).getName());
        assert "abc".equals(names.get(2).getName());
        names = autocomplete.autocomplete("ab");
        assert names != null;
        assert names.size() == 2;
        assert "ab".equals(names.get(0).getName());
        assert "abc".equals(names.get(1).getName());
        names = autocomplete.autocomplete("az");
        assert names != null;
        assert names.size() == 0;
        names = autocomplete.autocomplete("b");
        assert names != null;
        assert names.size() == 10;
        assert "ba".equals(names.get(0).getName());
        assert "bb".equals(names.get(1).getName());
        assert "bi".equals(names.get(8).getName());
        assert "bj".equals(names.get(9).getName());
        names = autocomplete.autocomplete("z");
        assert names != null;
        assert names.size() == 0;
        names = autocomplete.autocomplete(null);
        assert names != null;
        assert names.size() == 10;
        assert "a".equals(names.get(0).getName());
        assert "bg".equals(names.get(9).getName());
        names = autocomplete.autocomplete(Autocomplete.WILDCARD_PREFIX, 5);
        assert names != null;
        assert names.size() == 5;
        assert "a".equals(names.get(0).getName());
        assert "bb".equals(names.get(4).getName());

        // refresh autocomplete
        autocomplete.refresh();
        // wait for refresh
        Thread.sleep(Autocomplete.REFRESH_SETTLE_MIN_WAIT*2);
        // check autocomplete names access
        assert openNamesIteratorCalls[0] == 2;
        assert closeNamesIteratorCalls[0] == 2;

        // test refreshed names
        names = autocomplete.autocomplete("a");
        assert names != null;
        assert names.size() == 3;
        assert "a".equals(names.get(0).getName());
        assert "ab".equals(names.get(1).getName());
        assert "abc".equals(names.get(2).getName());

        // terminate autocomplete
        autocomplete.terminate();
    }

    /**
     * Test alias autocomplete functionality.
     */
    public void testAliasAutocomplete() throws Exception {
        // test alias autocomplete names
        AutocompleteNames testNames = new AutocompleteNames() {
            @Override
            public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
                List<AutocompleteName> names = Arrays.asList(new AutocompleteName[]{
                        new AutocompleteName("alias01", "name0"),
                        new AutocompleteName("alias02", "name0"),
                        new AutocompleteName("alias1", "name1"),
                        new AutocompleteName("name0", "name0"),
                        new AutocompleteName("name1", "name1"),
                        new AutocompleteName("name2", "name2")
                });
                return names.iterator();
            }

            @Override
            public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
            }
        };

        // create and initialize alias autocomplete
        Autocomplete autocomplete = new Autocomplete(testNames, getClass().getSimpleName());
        autocomplete.initialize();
        // wait for initialization
        Thread.sleep(Autocomplete.REFRESH_SETTLE_MIN_WAIT*2);

        // test alias names
        List<AutocompleteName> names = autocomplete.autocomplete("a", -1);
        assert names != null;
        assert names.size() == 3;
        assert "alias01".equals(names.get(0).getName());
        assert "name0".equals(names.get(0).getCanonicalName());
        assert "alias02".equals(names.get(1).getName());
        assert "name0".equals(names.get(1).getCanonicalName());
        assert "alias1".equals(names.get(2).getName());
        assert "name1".equals(names.get(2).getCanonicalName());
        names = autocomplete.autocomplete(Autocomplete.WILDCARD_PREFIX, 2);
        assert names != null;
        assert names.size() == 5;
        assert "alias01".equals(names.get(0).getName());
        assert "name0".equals(names.get(0).getCanonicalName());
        assert "alias02".equals(names.get(1).getName());
        assert "name0".equals(names.get(1).getCanonicalName());
        assert "alias1".equals(names.get(2).getName());
        assert "name1".equals(names.get(2).getCanonicalName());
        assert "name0".equals(names.get(3).getName());
        assert "name0".equals(names.get(3).getCanonicalName());
        assert "name1".equals(names.get(4).getName());
        assert "name1".equals(names.get(4).getCanonicalName());

        // terminate autocomplete
        autocomplete.terminate();
    }
}
