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
package ai.koryki.h2.northwind;

import ai.koryki.antlr.AbstractReader;
import ai.koryki.antlr.Bag;
import ai.koryki.antlr.KQLParser;
import ai.koryki.antlr.kql.KQLReader;
import ai.koryki.iql.Bean2Sql;
import ai.koryki.iql.RelationResolver;
import ai.koryki.iql.query.Out;
import ai.koryki.iql.query.Query;
import ai.koryki.kql.KQL2Bean;
import ai.koryki.kql.KQLFormatter;
import ai.koryki.model.JsonUtil;
import ai.koryki.model.schema.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NorthwindService {

    public static final String LINKS = "/ai/koryki/databases/northwind/links.json";
    public static final String MODEL = "/ai/koryki/databases/northwind/model.json";
    public static final String SCHEMA = "/ai/koryki/databases/northwind/schema.json";

    private NorthwindDatabase database;
    private Schema schema;
    private RelationResolver resolver;

    public NorthwindService() {
        try {
            this.schema = readDatabaseSchema();
            Map<String, List<String>> links = readLinks();
            this.resolver = new RelationResolver(schema, links);
            this.resolver.setStrict(true);
            this.database = new NorthwindDatabase("nw");
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public NorthwindService(NorthwindDatabase database, Schema schema, RelationResolver resolver) {
        this.database = database;
        this.schema = schema;
        this.resolver = resolver;
    }

    public String executeSQL(String sql) {
        try {
            Bag<Map<Integer, Map<Integer, String>>> result = new Bag<>(new LinkedHashMap<>());
            database.run(s -> {
                try (ResultSet r = s.executeQuery(sql)) {

                    ResultSetMetaData meta = r.getMetaData();
                    int columnCount = meta.getColumnCount();
                    int idx = 0;
                    while (r.next()) {

                        Map<Integer, String> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String value = r.getString(i);
                            row.put(i, value);
                        }
                        result.getItem().put(idx, row);
                    }
                }
            });
            return printResult(result.getItem());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String executeKQL(String kqlquery) {
        try {
            Query script = toQuery(kqlquery);
            String sql = toSql(script);
            Map<Integer, Map<Integer, String>> result = runWithResult(sql, script);
            return printResult(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String validateLQL(String kqlquery) {

        try {

            KQLReader r = new KQLReader(kqlquery, true);
            KQLParser.QueryContext query = r.getQuery();
            KQL2Bean l = new KQL2Bean(query, r.getDescription());
            Query script = l.toBean();
            // generate sql, do not execute
            toSql(script);

            return new KQLFormatter(query, r.getDescription()).format();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String convertToSql(String kqlquery) {

        try {
            Query query = toQuery(kqlquery);
            return toSql(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String descriptionOfEntitymodel() {
        return AbstractReader.read(MODEL);
    }

    public String descriptionOfDatabaseschema() {
        return AbstractReader.read(SCHEMA);
    }

    public String descriptionOfLinks() {
        return AbstractReader.read(LINKS);
    }

    private String toSql(Query query) throws IOException {

        Bean2Sql k = new Bean2Sql(resolver, query);
        return k.toEnhancedSql();
    }

    private static Query toQuery(String kql) throws IOException {
        KQLReader r = new KQLReader(kql, true);
        KQL2Bean l = new KQL2Bean(r.getQuery(), r.getDescription());
        return l.toBean();
    }

    private String printResult(Map<Integer, Map<Integer, String>> result) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(result);
    }

    private Map<Integer, Map<Integer, String>> runWithResult(String sql, Query query) throws SQLException {
        List<Out> out = Bean2Sql.collectOut(query.getSet());

        Map<Integer, Map<Integer, String>> result = new LinkedHashMap<>();
        database.run(s -> {
            try (ResultSet r = s.executeQuery(sql)) {
                int idx = 0;
                while (r.next()) {

                    Map<Integer, String> row = new LinkedHashMap<>();
                    for (int i = 0; i < out.size(); i++) {
                        String cell = r.getString(i + 1);
                        row.put(i, cell);
                    }
                    result.put(idx, row);
                    idx++;
                }
            }
        });
        return result;
    }

    public static HashMap<String, List<String>> readLinks() throws IOException {
        return JsonUtil.readHashSetFromResource(LINKS);
    }

    public static Schema readDatabaseSchema() throws IOException {

        try (InputStream in = NorthwindDatabase.class.getResourceAsStream(SCHEMA)) {
            return JsonUtil.readDatabaseJson(in);
        }
    }
}
