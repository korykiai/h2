package ai.koryki.h2;

import ai.koryki.antlr.AbstractReader;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NorthwindDatabaseSamplesTest {

    private static NorthwindDatabase database;
    private static Schema schema;
    private static RelationResolver resolver;


    @BeforeAll
    public static void startup() throws IOException, SQLException {

        schema = NorthwindService.readDatabaseSchema();
        Map<String, List<String>> links = NorthwindService.readLinks();
        resolver = new RelationResolver(schema, links);
        resolver.setStrict(true);

        long start = System.currentTimeMillis();
        database = new NorthwindDatabase("samples");
        System.out.println("loading h2: " + (System.currentTimeMillis() - start));
    }

    @AfterAll
    public static void shutdown() throws SQLException {
        if (database != null) {
            database.close();
        }
    }

    @Test
    public void products() throws IOException, SQLException {

        String name ="products";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/samples/" + name + ".kql");
        List<List<String>> result = test(in, name);
        assertEquals(9, result.size());
    }

    @Test
    public void customerswithorders() throws IOException, SQLException {

        String name ="customerswithorders";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/samples/" + name + ".kql");
        List<List<String>> result = test(in, name);
        assertEquals(270, result.size());
    }

    @Test
    public void orderanalysis() throws IOException, SQLException {

        String name ="orderanalysis";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/samples/" + name + ".kql");
        List<List<String>> result = test(in, name);
        assertEquals(50, result.size());
    }

    @Test
    public void supplychainriskmanagement() throws IOException, SQLException {

        String name ="supplychainriskmanagement";
        InputStream in = NorthwindDatabase.class.getResourceAsStream("/ai/koryki/databases/northwind/samples/" + name + ".kql");
        List<List<String>> result = test(in, name);
        assertEquals(1, result.size());
    }

    private List<List<String>> test(InputStream in, String name) throws IOException, SQLException {
        String lql = AbstractReader.convert(in);

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

        List<Out> out = Bean2Sql.collectOut(query.getSet());

        List<List<String>> result = new ArrayList<>();
        database.run(s -> {
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
