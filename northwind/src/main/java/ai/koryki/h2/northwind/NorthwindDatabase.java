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

import ai.koryki.databases.Database;
import ai.koryki.databases.StatementConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * This project includes a modified version of the Microsoft Northwind sample database.
 * Modifications by Johannes Zemlin, 2025.
 *
 * Original database © Microsoft Corporation, available at:
 * https://github.com/microsoft/sql-server-samples
 */
public class NorthwindDatabase implements Database, AutoCloseable {

    public static final String TABLES = "/ai/koryki/h2/northwind/tables.sql";
    public static final String DATA = "/ai/koryki/h2/northwind/data.sql";
    public static final String CONSTRAINTS = "/ai/koryki/h2/northwind/constraints.sql";
    private String name;
    private Connection conn;

    public NorthwindDatabase() throws IOException, SQLException {
        this("northwind");
    }

    public NorthwindDatabase(String name) throws IOException, SQLException {
        this.name = name;
        conn = DriverManager.getConnection("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1", "sa", "");

        runSqlScript(conn, TABLES);
        runSqlScript(conn, DATA);
        runSqlScript(conn, CONSTRAINTS);
    }

    public DatabaseMetaData getMetadata() throws SQLException {
        return conn.getMetaData();
    }


    public void run(StatementConsumer c) throws  SQLException {
        try (Statement stmt = conn.createStatement()) {
            c.accept(stmt);
        }
    }

    private static void runSqlScript(Connection conn, String resourcePath) throws IOException, SQLException {
        try (InputStream in = NorthwindDatabase.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Could not find " + resourcePath);
            }
            String sql = new BufferedReader(new InputStreamReader(in))
                    .lines().filter(l -> !l.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            // Split on semicolons if multiple statements
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }


    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public String getName() {
        return name;
    }
}
