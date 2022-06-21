package io.jtest.utils.clients.sql;

import io.jtest.utils.clients.database.SqlClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*" })
public class SqlClientTest {

    @Mock
    private ResultSet rs;
    @Mock
    private PreparedStatement pst;
    @Mock
    private Connection connection;
    @Mock
    private ResultSetMetaData rsMetaData;
    @Spy
    private final SqlClient sqlClient = new SqlClient("", "", "", "com.mysql.cj.jdbc.Driver");

    @Before
    public void mockSqlClient() throws Exception {
        Whitebox.setInternalState(sqlClient, connection);
        doNothing().when(sqlClient).connect();
        doNothing().when(sqlClient).close();
        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeQuery()).thenReturn(rs);
        when(pst.executeUpdate()).thenReturn(100);
        when(rs.getMetaData()).thenReturn(rsMetaData);

        when(rsMetaData.getColumnCount()).thenReturn(3);
        when(rsMetaData.getColumnLabel(1)).thenReturn("first_name");
        when(rsMetaData.getColumnLabel(2)).thenReturn("last_name");
        when(rsMetaData.getColumnLabel(3)).thenReturn("address");
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getObject(1)).thenReturn("David", "Andrew", "Lara");
        when(rs.getObject(2)).thenReturn("Jones", "Sputnik", "Croft");
        when(rs.getObject(3)).thenReturn("Hamilton 16", "Liberty 1", "Liberty 2");

        sqlClient.connect();
    }

    @After
    public void destroy() throws SQLException {
        sqlClient.close();
    }

    @Test
    public void testQueryResultAsList() throws SQLException {
        List<Map<String, Object>> expected = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("first_name", "David");
        row1.put("last_name", "Jones");
        row1.put("address", "Hamilton 16");
        Map<String, Object> row2 = new HashMap<>();
        row2.put("first_name", "Andrew");
        row2.put("last_name", "Sputnik");
        row2.put("address", "Liberty 1");
        Map<String, Object> row3 = new HashMap<>();
        row3.put("first_name", "Lara");
        row3.put("last_name", "Croft");
        row3.put("address", "Liberty 2");
        expected.add(row1);
        expected.add(row2);
        expected.add(row3);

        sqlClient.prepareStatement("test");
        assertEquals(expected, sqlClient.executeQueryAndGetRsAsList());
    }

    @Test
    public void testQueryResult() throws SQLException {
        sqlClient.prepareStatement("test");
        sqlClient.prepareStatement("test");
        ResultSet resultSet = sqlClient.executeQuery();
        resultSet.next();
        assertEquals("David", resultSet.getObject(1));
        assertEquals("Jones", resultSet.getObject(2));
        assertEquals("Hamilton 16", resultSet.getObject(3));
        resultSet.next();
        assertEquals("Andrew", resultSet.getObject(1));
        assertEquals("Sputnik", resultSet.getObject(2));
        assertEquals("Liberty 1", resultSet.getObject(3));
        resultSet.next();
        assertEquals("Lara", resultSet.getObject(1));
        assertEquals("Croft", resultSet.getObject(2));
        assertEquals("Liberty 2", resultSet.getObject(3));
        assertFalse(resultSet.next());
    }

    @Test
    public void testExecuteUpdate() throws SQLException {
        sqlClient.prepareStatement("test");
        assertEquals(100, sqlClient.executeUpdate());
    }
}
