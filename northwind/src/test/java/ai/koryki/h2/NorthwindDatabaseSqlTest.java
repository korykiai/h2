package ai.koryki.h2;

import ai.koryki.antlr.AbstractReader;
import ai.koryki.h2.northwind.NorthwindDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NorthwindDatabaseSqlTest {

    private static NorthwindDatabase n;

    @BeforeAll
    public static void readNorthwindDB() throws IOException, SQLException {
        long start = System.currentTimeMillis();
        n = new NorthwindDatabase();
        System.out.println("loading h2: " + (System.currentTimeMillis() - start));
    }

    @AfterAll
    public static void closeNorthwindDB() throws SQLException {
        if (n != null) {
            n.close();
        }
    }

    @Test
    public void onedown() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/link/onedown.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runAndPrint(sql);
    }

    @Test
    public void twodown() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/link/twodown.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void fourdown() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/link/fourjoin.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void outerjoinandfilter() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/link/outerjoinandfilter.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void expression1() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/expression/expression1.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void unsold_products1() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/expression/unsold_products1.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void complex1() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/complex/complex1.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void cte1() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/cte/cte1.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void cte2() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/cte/cte2.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void entity1() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/entity/entity1.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void entity2() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/entity/entity2.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void entity3() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/entity/entity3.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void entity4() throws SQLException, IOException {

        String expected = "/ai/koryki/databases/northwind/sql/entity/entity4.sql";
        InputStream e = NorthwindDatabase.class.getResourceAsStream(expected);
        String sql = AbstractReader.convert(e);

        runOnly(sql);
    }

    @Test
    public void having()  throws SQLException, IOException {
        String sql = "SELECT c.company_name, COUNT(o.Order_ID) AS OrderCount\n" +
                "FROM Customers c\n" +
                "JOIN Orders o ON c.Customer_ID = o.Customer_ID\n" +
                "GROUP BY c.Customer_ID\n" +
                "HAVING COUNT(o.Order_ID) > 5\n" +
                "ORDER BY COUNT(o.Order_ID) DESC\n" +
                ";"
                ;


        runAndPrint(sql);

    }

    @Test
    public void categoriesAndPrice()  throws SQLException, IOException {
        String sql = "SELECT c.Category_Name, AVG(p.Unit_Price) AS AvgPrice\n" +
                "FROM Categories c\n" +
                "JOIN Products p ON c.Category_ID = p.Category_ID\n" +
                "GROUP BY c.Category_Name\n" +
                "HAVING AVG(p.Unit_Price) > 30\n" +
                "ORDER BY c.Category_Name ASC\n" +
                ";"
                ;


        runAndPrint(sql);

    }

    private static void runAndPrint(String sql) throws SQLException {
        n.run(s -> {
            try (ResultSet r = s.executeQuery(sql)) {

                int idx = 0;
                while (r.next()) {
                    String col1 = r.getString(1);
                    String col2 = r.getString(2);
                    System.out.println(idx + " " + col1 + " " + col2);
                    idx++;
                }
            }
        });
    }

    private static void runOnly(String sql) throws SQLException {
        n.run(s -> {
            try (ResultSet r = s.executeQuery(sql)) {

                int idx = 0;
                while (r.next()) {
                    idx++;
                }
                System.out.println(idx + " rows");
            }
        });
    }
}
