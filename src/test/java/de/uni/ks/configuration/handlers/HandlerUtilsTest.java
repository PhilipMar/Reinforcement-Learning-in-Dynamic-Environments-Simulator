/*
    Copyright (C) 2020 Philip Martin and Timo Sturm

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, see <http://www.gnu.org/licenses/>.
*/
package de.uni.ks.configuration.handlers;

import de.uni.ks.agent.explorationPolicies.ExplorationPolicy;
import de.uni.ks.agent.explorationPolicies.GreedyPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HandlerUtilsTest {

    @Test
    void testCreateClass() {
        String packageName = "de.uni.ks.agent.explorationPolicies.";
        String className = "GreedyPolicy";
        // GreedyPolicy(int seed)
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("seed", "32");
        String interfaceName = ExplorationPolicy.class.getName();

        // simply works
        Object aClass = HandlerUtils.createClass(packageName, className, parameterMap, interfaceName);
        Assertions.assertTrue(aClass instanceof ExplorationPolicy);
        Assertions.assertTrue(aClass instanceof GreedyPolicy);
        Assertions.assertEquals(32, ((GreedyPolicy) aClass).getSeed());

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> HandlerUtils.createClass(packageName + "abc", className, parameterMap, interfaceName));
        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> HandlerUtils.createClass(packageName, className + "abc", parameterMap, interfaceName));
        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> HandlerUtils.createClass(packageName, className, parameterMap, interfaceName + "abc"));
    }

    @Test
    void testParseBool() {
        Assertions.assertTrue(HandlerUtils.parseBool("true"));
        Assertions.assertTrue(HandlerUtils.parseBool("True"));
        Assertions.assertFalse(HandlerUtils.parseBool("false"));
        Assertions.assertFalse(HandlerUtils.parseBool("False"));

        Assertions.assertThrows(NumberFormatException.class, () -> HandlerUtils.parseBool("thisIsCrap"));
    }

    @Test
    void testParseStringToType() {

        Assertions.assertTrue(HandlerUtils.parseStringToType("5", "int") instanceof Integer);
        Assertions.assertTrue(HandlerUtils.parseStringToType("22.2", "double") instanceof Double);
        Assertions.assertTrue(HandlerUtils.parseStringToType("true", "boolean") instanceof Boolean);

        Assertions.assertEquals(5, ((Integer) HandlerUtils.parseStringToType("5", "int")).intValue());
        Assertions.assertEquals(22.2, ((Double) HandlerUtils.parseStringToType("22.2", "double")).doubleValue());
        Assertions.assertTrue((Boolean) HandlerUtils.parseStringToType("true", "boolean"));

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> HandlerUtils.parseStringToType("55", "boolean"));
        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> HandlerUtils.parseStringToType("true", "schmoolean"));

    }

    @Test
    void testParseParameters() {
        // Syntax = CLASSNAME(p1=v1,p2=v2,...)
        String s1 = "ClassName(myInt=5,myDouble=22.2,myBool=True)";
        String s2 = "ClassName";
        String s3 = "ClassName()";
        String s4 = "ClassName ( myInt = 5 , myDouble = 22.2 , myBool = True )";

        Assertions.assertAll(() -> {
            Map<String, String> map = HandlerUtils.parseParameters(s1);
            Assertions.assertEquals("5", map.get("myInt"));
            Assertions.assertEquals("22.2", map.get("myDouble"));
            Assertions.assertEquals("True", map.get("myBool"));
        });

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class, () -> HandlerUtils.parseParameters(s2));

        Assertions.assertEquals(0, HandlerUtils.parseParameters(s3).size());

        Assertions.assertAll(() -> {
            Map<String, String> map = HandlerUtils.parseParameters(s4);
            Assertions.assertEquals("5", map.get("myInt"));
            Assertions.assertEquals("22.2", map.get("myDouble"));
            Assertions.assertEquals("True", map.get("myBool"));
        });
    }

    @Test
    void testParseClassName() {
        // Syntax = CLASSNAME(p1=v1,p2=v2,...)
        String string = " ThisIsMyClass (a=1) ";
        String string1 = "ThisIsMyClass (a=1) ";
        String string2 = " ThisIsMyClass (a=1) ";
        String string3 = " ThisIsMyClass";

        Assertions.assertEquals("ThisIsMyClass", HandlerUtils.parseClassName(string));
        Assertions.assertEquals("ThisIsMyClass", HandlerUtils.parseClassName(string1));
        Assertions.assertEquals("ThisIsMyClass", HandlerUtils.parseClassName(string2));
        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class, () -> HandlerUtils.parseClassName(string3));
    }

    @Test
    void testSplitStringAt() {
        String splitMe = "A,B,C,D";
        String splitMe2 = "A|B|C|D";
        String splitMe3 = "ABCD";

        List<String> strings = HandlerUtils.splitStringAt(splitMe, ",");
        List<String> strings1 = HandlerUtils.splitStringAt(splitMe2, "|");
        List<String> strings2 = HandlerUtils.splitStringAt(splitMe3, "");

        Assertions.assertAll(() -> {
            Assertions.assertTrue(strings.contains("A"));
            Assertions.assertTrue(strings.contains("B"));
            Assertions.assertTrue(strings.contains("C"));
            Assertions.assertTrue(strings.contains("D"));
        });

        Assertions.assertAll(() -> {
            Assertions.assertTrue(strings1.contains("A"));
            Assertions.assertTrue(strings1.contains("B"));
            Assertions.assertTrue(strings1.contains("C"));
            Assertions.assertTrue(strings1.contains("D"));
        });

        Assertions.assertAll(() -> {
            Assertions.assertTrue(strings2.contains("A"));
            Assertions.assertTrue(strings2.contains("B"));
            Assertions.assertTrue(strings2.contains("C"));
            Assertions.assertTrue(strings2.contains("D"));
        });
    }
}
