/*
 * Copyright 2025 Johannes Zemlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ai.koryki.h2;

import ai.koryki.antlr.kql.KQLReader;
import ai.koryki.h2.northwind.NorthwindDatabase;
import ai.koryki.h2.northwind.NorthwindService;
import ai.koryki.iql.Bean2Iql;
import ai.koryki.iql.Bean2Sql;
import ai.koryki.iql.RelationResolver;
import ai.koryki.iql.query.Out;
import ai.koryki.iql.query.Query;
import ai.koryki.model.schema.Schema;
import ai.koryki.kql.KQL2Bean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NorthwindDatabaseTest {

    private static NorthwindDatabase n;
    private static RelationResolver resolver;


    @BeforeAll
    public static void startup() throws IOException, SQLException {

        Schema schema = NorthwindService.readDatabaseSchema();
        Map<String, List<String>> links = NorthwindService.readLinks();
        resolver = new RelationResolver(schema, links);
        resolver.setStrict(true);
        n = new NorthwindDatabase("nw");
    }

    @AfterAll
    public static void shutdown() throws SQLException {
        if (n != null) {
            n.close();
        }
    }

    @Test
    public void customersmorethan10ordersin2023() throws IOException, SQLException {

        String name ="customersmorethan10ordersin2023";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/demo/" + name + ".kql");

        test(in, name);
    }

    @Test
    public void ordertimerange() throws IOException, SQLException {

        String name ="ordertimerange";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/demo/" + name + ".kql");

        test(in, name);
    }

    @Test
    public void unorderedproducts() throws IOException, SQLException {

        String name ="unorderedproductsin012023";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/demo/" + name + ".kql");

        test(in, name);
    }

    @Test
    public void employeeranking() throws IOException, SQLException {

        String name ="employeeranking";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/demo/" + name + ".kql");

        test(in, name);
    }

    @Test
    public void employeeswithorders() throws IOException, SQLException {

        String name ="employeeswithorders";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/demo/" + name + ".kql");
        List<List<String>> result = test(in, name);
        assertEquals(9, result.size());

        assertEquals("4", result.get(0).get(2));
        assertEquals("2", result.get(1).get(2));
        assertEquals("2", result.get(2).get(2));
        assertEquals("2", result.get(3).get(2));
        assertEquals("1", result.get(4).get(2));
        assertEquals("0", result.get(5).get(2));
        assertEquals("0", result.get(6).get(2));
        assertEquals("0", result.get(7).get(2));
        assertEquals("0", result.get(8).get(2));
    }

    private List<List<String>> test(InputStream in, String name) throws IOException, SQLException {


        String lql = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        System.out.println("+++ kql ++++++++++++++++++");
        System.out.println(lql);

        Query query = toScript(lql);

        String sql = toSql(query);
        System.out.println("+++ sql ++++++++++++++++++");
        System.out.println(sql);

        System.out.println("+++ kql ++++++++++++++++++");
        System.out.println(new Bean2Iql(query).toString());

        List<List<String>> result = runWithResult(sql, query);

        System.out.println("++++++++++++++++++++++++++");
        printResult(result);
        return result;
    }

    private void printResult(List<List<String>> result) throws IOException {
        for (List<String> l : result) {
            System.out.println(l);
        }
    }

    private String toSql(Query query) throws IOException {

        Bean2Sql k = new Bean2Sql(resolver, query);
        return k.toEnhancedSql();
    }

    private static Query toScript(String lql) throws IOException {
        KQLReader r = new KQLReader(lql, true);
        KQL2Bean l = new KQL2Bean(r.getQuery(), r.getDescription());
        Query query = l.toBean();
        return query;
    }


    private static List<List<String>> runWithResult(String sql, Query query) throws SQLException {
        List<Out> out = Bean2Sql.collectOut(query.getSet().getSelect());

        List<List<String>> result = new ArrayList<>();
        n.run(s -> {
            try (ResultSet r = s.executeQuery(sql)) {

                int idx = 0;
                while (r.next()) {

                    List<String> row = new ArrayList<>();

                    for (int i = 0; i < out.size(); i++) {
                        String cell = r.getString(i + 1);
                        row.add(cell);
                    }

                    result.add(row);
                    idx++;
                }
                System.out.println(idx + " rows");
            }
        });
        return result;
    }


}
